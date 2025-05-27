package com.itjay.service.impl;

import com.itjay.mapper.UserMapper;
import com.itjay.pojo.User;
import com.itjay.security.JwtUtils;
import com.itjay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }
        
        // 如果密码不是BCrypt格式，先进行更新
        if (!user.getPassword().startsWith("$2a$")) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            userMapper.updatePassword(user);
        }
        
        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwtUtils.generateToken(username);
        }
        throw new RuntimeException("密码错误");
    }

    @Override
    public User register(User user) {
        // Check if username already exists
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());

        // Insert the user into database
        userMapper.insert(user);
        
        return user;
    }

    @Override
    public User getCurrentUser(String token) {
        if (!jwtUtils.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        String username = jwtUtils.getUsernameFromToken(token);
        return userMapper.findByUsername(username);
    }
} 