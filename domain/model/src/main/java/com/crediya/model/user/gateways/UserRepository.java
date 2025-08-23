package com.crediya.model.user.gateways;

import com.crediya.model.user.User;

public interface UserRepository {
    User getUserByEmail(String email);
    User saveUser(User user);
}
