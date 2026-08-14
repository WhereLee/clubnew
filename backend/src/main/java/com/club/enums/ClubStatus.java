package com.club.enums;

/**
 * 社团状态枚举
 */
public enum ClubStatus {
    PENDING("待审批"),
    APPROVED("正常运营"),
    SUSPENDED("已暂停"),
    DISSOLVED("已注销"),
    REJECTED("已驳回");

    private final String desc;

    ClubStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
