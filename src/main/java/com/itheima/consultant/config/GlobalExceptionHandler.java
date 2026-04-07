package com.itheima.consultant.config;

import cn.dev33.satoken.exception.NotLoginException;
import com.itheima.consultant.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLogin(NotLoginException e) {
        return Result.error(401, "未登录，请先登录");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace(); // 开发阶段打印错误
        return Result.error(500, "服务器异常：" + e.getMessage());
    }
}