package com.yzm.shiro10.config;

import org.apache.shiro.authc.UsernamePasswordToken;

public class CustomToken extends UsernamePasswordToken {
    private static final long serialVersionUID = 2496490278578779482L;
    private String loginType;

    public CustomToken(final String username, final String password) {
        super(username, password);
    }

    public CustomToken(final String username, final String password, String loginType) {
        super(username, password);
        this.loginType = loginType;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }
}
