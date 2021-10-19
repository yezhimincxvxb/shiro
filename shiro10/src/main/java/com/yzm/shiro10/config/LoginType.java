package com.yzm.shiro10.config;

public enum LoginType {
    USER("User"), ADMIN("Admin");

    private final String type;

    LoginType(String type) {
        this.type = type;
    }

    public String type() {
        return this.type;
    }

}
