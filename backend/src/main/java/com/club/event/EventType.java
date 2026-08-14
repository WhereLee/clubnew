package com.club.event;

/**
 * 互动事件类型（Redis Stream 事件管道）。
 * <p>
 * 每种事件携带固定的社团活跃度分值；消费者（{@code RankEventConsumer}）消费后
 * 对社团活跃度 ZSet 原子加分，动态类事件同时提升动态热度。
 */
public enum EventType {

    /** 发布动态 */
    POST_CREATED(1.0),

    /** 发表评论 */
    COMMENT_CREATED(0.5),

    /** 点赞（bizType=POST 时同时提升动态热度） */
    LIKED(0.2),

    /** 活动报名 */
    ACTIVITY_SIGNUP(2.0),

    /** 活动签到 */
    ACTIVITY_CHECKIN(2.0),

    /** 纳新报名 */
    RECRUIT_APPLY(2.0);

    private final double score;

    EventType(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }
}
