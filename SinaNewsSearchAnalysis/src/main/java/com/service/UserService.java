package com.service;

import com.models.User;

public interface UserService {

    void save(User user);

    User findByUsername(String username);

    User findByEmail(String email);
}
