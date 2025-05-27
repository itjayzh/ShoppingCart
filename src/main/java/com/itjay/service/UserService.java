package com.itjay.service;

import com.itjay.pojo.User;

public interface UserService {
    String login(String username, String password);
    User register(User user);
    User getCurrentUser(String token);
} 