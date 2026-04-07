package com.itheima.consultant.service;

import cn.dev33.satoken.stp.StpUtil;
import com.itheima.consultant.entity.User;
import com.itheima.consultant.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    // MD5 加密（实际项目建议用 BCrypt，这里用 MD5 简单演示）
    private String md5(String raw) {
        return DigestUtils.md5DigestAsHex(raw.getBytes());
    }

    /** 注册 */
    public void register(String username, String password, String nickname) {
        // 校验用户名是否已存在
        if (userMapper.findByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(md5(password));
        user.setNickname(nickname == null || nickname.isBlank() ? username : nickname);
        userMapper.insert(user);
    }

    /** 登录，返回 token + 用户信息 */
    public Map<String, Object> login(String username, String password, boolean rememberMe) {
        User user = userMapper.findByUsername(username);
        if (user == null || !user.getPassword().equals(md5(password))) {
            throw new RuntimeException("用户名或密码错误");
        }
        // rememberMe=true 时，token 有效期延长到 30 天，否则跟随默认配置
        StpUtil.login(user.getId(), rememberMe);
        String token = StpUtil.getTokenValue();

        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("nickname", user.getNickname());
        map.put("username", user.getUsername());
        return map;
    }

    /** 登出 */
    public void logout() {
        StpUtil.logout();
    }
}