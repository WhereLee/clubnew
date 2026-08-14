package com.club.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 业务指标集中注册（Micrometer → /actuator/prometheus）。
 * <p>
 * 命名规范：{@code club_<业务>_<动作>_total}（Counter）/ {@code club_<业务>_<状态>}（Gauge），
 * 与 HTTP/JVM/连接池等框架自带指标互补，构成 RED 方法（Rate/Errors/Duration）的业务视角。
 */
@Component
public class ClubMetrics {

    // ---- Counter：报名流量与防超卖结果 ----
    private final Counter recruitApplyTotal;
    private final Counter activitySignupTotal;
    private final Counter stockPreDeductFailures;
    private final Counter rateLimitRejections;
    private final Counter repeatSubmitRejections;
    // ---- Counter：Stream 事件管道 ----
    private final Counter eventPublished;
    private final Counter eventPublishFallbacks;
    private final Counter eventConsumed;
    private final Counter eventConsumeFailures;
    // ---- Gauge：Stream 积压（PENDING 消息数） ----
    private final AtomicLong streamPendingGauge = new AtomicLong(0);

    public ClubMetrics(MeterRegistry meterRegistry) {
        this.recruitApplyTotal = Counter.builder("club_recruit_apply_total")
                .description("纳新报名请求总数").register(meterRegistry);
        this.activitySignupTotal = Counter.builder("club_activity_signup_total")
                .description("活动报名请求总数").register(meterRegistry);
        this.stockPreDeductFailures = Counter.builder("club_stock_prededuct_failures_total")
                .description("Redis 预扣失败次数（降级 DB 兜底）").register(meterRegistry);
        this.rateLimitRejections = Counter.builder("club_ratelimit_rejections_total")
                .description("接口限流拦截次数").register(meterRegistry);
        this.repeatSubmitRejections = Counter.builder("club_repeatsubmit_rejections_total")
                .description("防重复提交拦截次数").register(meterRegistry);
        this.eventPublished = Counter.builder("club_stream_event_published_total")
                .description("Stream 事件发布成功数").register(meterRegistry);
        this.eventPublishFallbacks = Counter.builder("club_stream_event_publish_fallbacks_total")
                .description("Stream 发布失败降级同步加分次数").register(meterRegistry);
        this.eventConsumed = Counter.builder("club_stream_event_consumed_total")
                .description("Stream 事件消费成功数").register(meterRegistry);
        this.eventConsumeFailures = Counter.builder("club_stream_event_consume_failures_total")
                .description("Stream 事件消费失败数（留 PENDING 重试）").register(meterRegistry);
        meterRegistry.gauge("club_stream_pending_messages", streamPendingGauge);
    }

    public void incrRecruitApply() { recruitApplyTotal.increment(); }
    public void incrActivitySignup() { activitySignupTotal.increment(); }
    public void incrStockPreDeductFailures() { stockPreDeductFailures.increment(); }
    public void incrRateLimitRejections() { rateLimitRejections.increment(); }
    public void incrRepeatSubmitRejections() { repeatSubmitRejections.increment(); }
    public void incrEventPublished() { eventPublished.increment(); }
    public void incrEventPublishFallbacks() { eventPublishFallbacks.increment(); }
    public void incrEventConsumed() { eventConsumed.increment(); }
    public void incrEventConsumeFailures() { eventConsumeFailures.increment(); }
    public void setStreamPending(long count) { streamPendingGauge.set(count); }
}
