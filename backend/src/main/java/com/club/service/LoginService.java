package com.club.service;

import com.club.vo.LoginVO;

public interface LoginService {
    LoginVO login(String username, String password);
    void logout(String token);
}
