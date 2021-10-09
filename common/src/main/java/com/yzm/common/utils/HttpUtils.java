package com.yzm.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.yzm.common.entity.HttpResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * HTTP工具类
 */
public class HttpUtils {

    /**
     * 获取HttpServletRequest对象
     */
    public static HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    /**
     * 输出信息到浏览器
     */
    public static void successWrite(HttpServletResponse response, Object data) throws IOException {
        write(response, 200, "操作成功", data);
    }

    public static void errorWrite(HttpServletResponse response, String msg) throws IOException {
        write(response, 500, msg, null);
    }

    public static void errorWrite(HttpServletResponse response, int code, String msg) throws IOException {
        write(response, code, msg, null);
    }

    private static void write(HttpServletResponse response, int code, String msg, Object data) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        HttpResult result = new HttpResult(code, msg, data);
        response.getWriter().print(JSONObject.toJSONString(result, true));
        response.getWriter().flush();
        response.getWriter().close();
    }

}
