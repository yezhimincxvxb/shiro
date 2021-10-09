package com.yzm.common.entity;

public enum MessageEnum {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    NO_ACCESS(403, "无权访问"),
    NOT_FOUNT(404, "请求路径错误"),
    INTERNAL_SERVER_ERROR(500, "操作失败");

    private final int code;
    private final String message;

    MessageEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }

}
