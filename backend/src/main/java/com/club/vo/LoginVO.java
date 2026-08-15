package com.club.vo;

import lombok.Data;

@Data
public class LoginVO {
    /** access token（JWT，短时有效） */
    private String token;
    /** access token 有效期（秒） */
    private long expiresIn;
    /** 用户类型（ADMIN / TECH_ADMIN / PRESIDENT / STUDENT，前端菜单分区渲染用） */
    private String userType;
    /** refresh token（不透明随机串，用于静默续期与轮换） */
    private String refreshToken;
    /** refresh token 有效期（秒） */
    private long refreshExpiresIn;
}
