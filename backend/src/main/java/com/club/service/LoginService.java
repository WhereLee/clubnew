package com.club.service;

import com.club.vo.LoginVO;

public interface LoginService {

    LoginVO login(String username, String password);

    /** 注销 access 会话 */
    void logout(String token);

    /** 注销 access 会话 + 吊销 refresh token（前端登出完整路径） */
    void logout(String token, String refreshToken);
}
