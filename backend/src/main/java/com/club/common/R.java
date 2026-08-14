package com.club.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回体
 */
@Data
public class R<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    public static <T> R<T> success() {
        return restResult(null, ResultCode.SUCCESS, "操作成功");
    }

    public static <T> R<T> success(T data) {
        return restResult(data, ResultCode.SUCCESS, "操作成功");
    }

    public static <T> R<T> success(String msg, T data) {
        return restResult(data, ResultCode.SUCCESS, msg);
    }

    public static <T> R<T> fail(String msg) {
        return restResult(null, ResultCode.FAIL, msg);
    }

    public static <T> R<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    private static <T> R<T> restResult(T data, int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}
