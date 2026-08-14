package com.club.common;

/**
 * 统一错误码
 */
public interface ResultCode {
    int SUCCESS = 200;
    int FAIL = 500;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int BAD_REQUEST = 400;
}
