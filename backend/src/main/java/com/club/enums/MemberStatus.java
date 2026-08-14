package com.club.enums;

/**
 * 成员状态枚举
 */
public enum MemberStatus {
    PENDING("待审批"),
    ACTIVE("在社"),
    QUIT("已退出"),
    REMOVED("已移除");

    private final String desc;

    MemberStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
