package com.club.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private long expiresIn;
}
