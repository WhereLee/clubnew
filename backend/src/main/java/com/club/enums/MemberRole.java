package com.club.enums;

/**
 * 成员角色枚举
 */
public enum MemberRole {
    PRESIDENT("社长"),
    VICE("副社长"),
    MEMBER("普通成员");

    private final String desc;

    MemberRole(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
