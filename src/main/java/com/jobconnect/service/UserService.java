package com.jobconnect.service;

import com.jobconnect.dto.UserRegisterDto;
import com.jobconnect.model.User;

public interface UserService {
    User registerUser(UserRegisterDto registrationDto);
    User findByEmail(String email);
}
