package com.itheima.consultant.controller;

import com.itheima.consultant.common.Result;
import com.itheima.consultant.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
//@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthService authService;

    /** 注册接口 POST /auth/register */
    @PostMapping("/register")
    public Result<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String nickname = body.get("nickname");

        System.out.println("注册请求: username=" + username + ", password=" + password);

        if (username == null || username.isBlank()) return Result.error("用户名不能为空");
        if (password == null || password.length() < 6) return Result.error("密码不能少于6位");

        try {
            authService.register(username, password, nickname);
            return Result.success();
        } catch (Exception e) {
            System.out.println("注册异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage() != null ? e.getMessage() : "注册失败，请检查数据库配置");
        }
    }

    /** 登录接口 POST /auth/login */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        boolean rememberMe = Boolean.TRUE.equals(body.get("rememberMe"));
        if (username == null || password == null) return Result.error("参数不能为空");
        try {
            Map<String, Object> data = authService.login(username, password, rememberMe);
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 登出接口 POST /auth/logout */
    @PostMapping("/logout")
    public Result<?> logout() {
        authService.logout();
        return Result.success();
    }
}