package com.yzm.common.entity;


import lombok.Data;

import java.io.Serializable;

/**
 * HTTP结果封装
 */
@Data
public class HttpResult implements Serializable {

    private static final long serialVersionUID = -4661003546224340096L;

    private int code;
    private String msg;
    private Object data;

    public HttpResult(int code, String msg) {
        this(code, msg, null);
    }

    public HttpResult(MessageEnum message) {
        this(message.code(), message.message(), null);
    }

    public HttpResult(MessageEnum message, Object data) {
        this(message.code(), message.message(), data);
    }

    public HttpResult(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static HttpResult error() {
        return new HttpResult(MessageEnum.INTERNAL_SERVER_ERROR);
    }

    public static HttpResult error(String msg) {
        return new HttpResult(500, msg);
    }

    public static HttpResult error(int code, String msg) {
        return new HttpResult(code, msg);
    }

    public static HttpResult ok() {
        return new HttpResult(MessageEnum.SUCCESS);
    }

    public static HttpResult ok(String msg) {
        return new HttpResult(200, msg);
    }

    public static HttpResult ok(Object data) {
        return new HttpResult(MessageEnum.SUCCESS, data);
    }

}