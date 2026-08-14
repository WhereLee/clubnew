package com.club;

import com.club.event.ClubEventPublisher;
import com.club.event.EventType;
import com.club.service.RankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StreamOperations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 事件发布器测试：验证 Redis Stream 不可用时的降级路径——
 * 发布失败必须同步加分，保证排行榜数据不因中间件故障丢失。
 */
class ClubEventPublisherTest {

    private StringRedisTemplate stringRedisTemplate;
    private RankService rankService;
    private ClubEventPublisher publisher;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        rankService = Mockito.mock(RankService.class);
        publisher = new ClubEventPublisher();
        // 手动注入（避免起 Spring 上下文，单测更轻）
        org.springframework.test.util.ReflectionTestUtils.setField(publisher, "stringRedisTemplate", stringRedisTemplate);
        org.springframework.test.util.ReflectionTestUtils.setField(publisher, "rankService", rankService);
    }

    @Test
    void publish_streamAvailable_sendsToStream_noSyncFallback() {
        StreamOperations<String, Object, Object> ops = Mockito.mock(StreamOperations.class);
        when(stringRedisTemplate.opsForStream()).thenReturn(ops);

        publisher.publish(EventType.POST_CREATED, 10L, 1L, 100L, null);

        verify(ops).add(any());
        // 正常路径不触发同步兜底
        verify(rankService, never()).incrClubActivityScore(anyLong(), anyDouble());
    }

    @Test
    void publish_streamDown_fallsBackToSyncScore() {
        StreamOperations<String, Object, Object> ops = Mockito.mock(StreamOperations.class);
        when(stringRedisTemplate.opsForStream()).thenReturn(ops);
        doThrow(new RuntimeException("connection refused")).when(ops).add(any());

        publisher.publish(EventType.POST_CREATED, 10L, 1L, 100L, null);

        // 降级：同步加分，社团活跃度 +1.0（POST_CREATED 分值）
        verify(rankService, times(1)).incrClubActivityScore(10L, 1.0);
    }

    @Test
    void publish_likeOnPost_fallsBackAddsPostHotScore() {
        StreamOperations<String, Object, Object> ops = Mockito.mock(StreamOperations.class);
        when(stringRedisTemplate.opsForStream()).thenReturn(ops);
        doThrow(new RuntimeException("connection refused")).when(ops).add(any());

        publisher.publish(EventType.LIKED, 10L, 1L, 100L, "POST");

        // 点赞动态：热度 +1 且社团活跃度 +0.2
        verify(rankService, times(1)).incrPostHotScore(100L, 1.0);
        verify(rankService, times(1)).incrClubActivityScore(10L, 0.2);
    }

    @Test
    void publish_nullClubId_skipsClubScore() {
        StreamOperations<String, Object, Object> ops = Mockito.mock(StreamOperations.class);
        when(stringRedisTemplate.opsForStream()).thenReturn(ops);
        doThrow(new RuntimeException("connection refused")).when(ops).add(any());

        publisher.publish(EventType.POST_CREATED, null, 1L, 100L, null);

        verify(rankService, never()).incrClubActivityScore(anyLong(), anyDouble());
    }
}
