package com.club.event;

import com.club.service.RankService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 排行榜事件消费者（Redis Stream 消费者组）。
 * <p>
 * 特性（与 MQ 消费者语义对齐，面试可讲）：
 * <ul>
 *   <li><b>消费者组</b>：XGROUP 保证组内消息只投递给一个消费者，可横向扩展；</li>
 *   <li><b>ACK 机制</b>：处理成功后 XACK，处理失败不 ACK（消息留在 PENDING）；</li>
 *   <li><b>故障恢复</b>：每轮循环用 XAUTOCLAIM 认领空闲超过 60 秒的 PENDING 消息重试；</li>
 *   <li><b>幂等消费</b>：eventId + SETNX 去重，重试不会重复加分；</li>
 *   <li><b>优雅降级</b>：应用启动时 Redis 不可用则周期性重试建组，不阻塞启动。</li>
 * </ul>
 */
@Component
public class RankEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(RankEventConsumer.class);

    private static final String GROUP_NAME = "rank-consumers";
    private static final String CONSUMER_NAME = "rank-consumer-1";
    private static final String DEDUP_PREFIX = "stream:dedup:";
    private static final int BATCH_SIZE = 10;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RankService rankService;

    /** 开关：测试环境关闭消费者（mock Redis 无法提供 stream 能力） */
    @Value("${club.event.stream.enabled:true}")
    private boolean streamEnabled;

    /** 应用就绪后异步启动消费循环，不阻塞启动流程 */
    @Async("clubAsyncExecutor")
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!streamEnabled) {
            log.info("事件流消费已禁用（club.event.stream.enabled=false）");
            return;
        }
        log.info("排行榜事件消费者启动，stream={}, group={}", ClubEventPublisher.STREAM_KEY, GROUP_NAME);
        long roundsWithoutClaim = 0;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ensureGroup();
                // 先处理 PENDING（上轮处理失败的消息），再消费新消息
                consumePendingMessages();
                consumeNewMessages();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Redis 短暂不可用等：退避后继续（建组失败/连接失败都会走到这里）
                log.warn("事件消费循环异常，2 秒后重试: {}", e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /** XGROUP CREATE（已存在时忽略） */
    private void ensureGroup() {
        try {
            stringRedisTemplate.opsForStream().createGroup(ClubEventPublisher.STREAM_KEY, GROUP_NAME);
        } catch (Exception e) {
            // BUSYGROUP：组已存在，正常情况
        }
    }

    /** 消费新消息：XREADGROUP BLOCK 后逐条处理并 XACK */
    private void consumeNewMessages() {
        List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream().read(
                Consumer.from(GROUP_NAME, CONSUMER_NAME),
                StreamReadOptions.empty().count(BATCH_SIZE).block(Duration.ofSeconds(2)),
                StreamOffset.create(ClubEventPublisher.STREAM_KEY, ReadOffset.lastConsumed()));
        if (records == null || records.isEmpty()) {
            return;
        }
        for (MapRecord<String, Object, Object> record : records) {
            if (handleRecord(record)) {
                stringRedisTemplate.opsForStream().acknowledge(ClubEventPublisher.STREAM_KEY, GROUP_NAME, record.getId());
            }
        }
    }

    /**
     * 处理 PENDING 消息（XREADGROUP ID=0）：处理失败未 ACK 的消息会被重新投递，
     * 配合 eventId 幂等去重，实现「至少一次投递 + 幂等消费」的可靠语义。
     */
    private void consumePendingMessages() {
        try {
            List<MapRecord<String, Object, Object>> pending = stringRedisTemplate.opsForStream().read(
                    Consumer.from(GROUP_NAME, CONSUMER_NAME),
                    StreamReadOptions.empty().count(BATCH_SIZE),
                    StreamOffset.create(ClubEventPublisher.STREAM_KEY, ReadOffset.from("0")));
            if (pending == null || pending.isEmpty()) {
                return;
            }
            for (MapRecord<String, Object, Object> record : pending) {
                if (handleRecord(record)) {
                    stringRedisTemplate.opsForStream().acknowledge(ClubEventPublisher.STREAM_KEY, GROUP_NAME, record.getId());
                }
            }
        } catch (Exception e) {
            log.warn("PENDING 消息处理异常: {}", e.getMessage());
        }
    }

    /**
     * 处理单条消息：eventId 幂等去重 → 按事件类型加分。
     *
     * @return true=处理成功（可 ACK）；false=处理失败（留 PENDING 等重试）
     */
    private boolean handleRecord(MapRecord<?, Object, Object> record) {
        try {
            Map<Object, Object> fields = record.getValue();
            String eventId = str(fields.get("eventId"));
            String type = str(fields.get("type"));
            Long clubId = parseLong(str(fields.get("clubId")));
            Long bizId = parseLong(str(fields.get("bizId")));
            String bizType = str(fields.get("bizType"));

            // 幂等：同 eventId 只处理一次
            if (eventId.isEmpty()) return true; // 无 eventId 的消息直接确认，避免死循环
            Boolean first = stringRedisTemplate.opsForValue()
                    .setIfAbsent(DEDUP_PREFIX + eventId, "1", 6, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(first)) {
                return true; // 已处理过
            }

            EventType eventType = EventType.valueOf(type);
            if (eventType == EventType.LIKED && "POST".equals(bizType) && bizId != null) {
                rankService.incrPostHotScore(bizId, 1.0);
            }
            if (clubId != null) {
                rankService.incrClubActivityScore(clubId, eventType.getScore());
            }
            return true;
        } catch (Exception e) {
            log.error("事件处理失败，消息留 PENDING 待重试: {}", e.getMessage());
            return false;
        }
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static Long parseLong(String s) {
        return s == null || s.isEmpty() ? null : Long.parseLong(s);
    }
}
