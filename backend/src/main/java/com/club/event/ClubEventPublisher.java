package com.club.event;

import com.club.service.RankService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 互动事件发布器（Redis Stream 生产者）。
 * <p>
 * 写操作（发动态/评论/点赞/报名/签到）成功后调用 {@link #publish} 将事件写入
 * stream {@code club:events}，由 {@code RankEventConsumer} 异步消费加分，主流程不被拖慢。
 * <p>
 * 故障策略：Redis 不可用或 stream 写入失败时，<b>降级为同步加分</b>——排行榜数据
 * 不允许因中间件故障丢失（与「防重复提交/限流 fail-open、防超卖 DB 兜底」同一原则：
 * 核心数据一致性强于旁路组件可用性）。
 */
@Component
public class ClubEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ClubEventPublisher.class);

    public static final String STREAM_KEY = "club:events";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RankService rankService;

    /**
     * 发布事件。Redis 不可用时降级为同步加分。
     *
     * @param bizType 业务类型（LIKED 时为 POST/COMMENT，其余为 null）
     */
    public void publish(EventType type, Long clubId, Long userId, Long bizId, String bizType) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("eventId", UUID.randomUUID().toString());
        fields.put("type", type.name());
        fields.put("clubId", clubId == null ? "" : String.valueOf(clubId));
        fields.put("userId", userId == null ? "" : String.valueOf(userId));
        fields.put("bizId", bizId == null ? "" : String.valueOf(bizId));
        fields.put("bizType", bizType == null ? "" : bizType);
        fields.put("score", String.valueOf(type.getScore()));
        fields.put("timestamp", String.valueOf(System.currentTimeMillis()));
        try {
            MapRecord<String, String, String> record = StreamRecords.newRecord()
                    .ofMap(fields)
                    .withStreamKey(STREAM_KEY);
            RecordId recordId = stringRedisTemplate.opsForStream().add(record);
            log.debug("事件已发布: type={}, eventId={}", type, recordId);
        } catch (Exception e) {
            // 降级：同步加分，保证排行榜不丢分
            log.warn("Stream 发布失败，降级为同步加分: type={}, err={}", type, e.getMessage());
            applyScoreSync(type, clubId, bizId, bizType);
        }
    }

    /** 同步加分（与消费者逻辑一致，作为 stream 不可用时的兜底路径） */
    private void applyScoreSync(EventType type, Long clubId, Long bizId, String bizType) {
        if (type == EventType.LIKED && "POST".equals(bizType) && bizId != null) {
            rankService.incrPostHotScore(bizId, 1.0);
        }
        if (clubId != null) {
            rankService.incrClubActivityScore(clubId, type.getScore());
        }
    }
}
