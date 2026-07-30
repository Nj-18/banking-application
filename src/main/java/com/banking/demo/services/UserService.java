package com.banking.demo.services;

import com.banking.demo.dtos.RegisterUserRequestDTO;
import com.banking.demo.dtos.RegisterUserResponseDTO;
import com.banking.demo.entities.User;

public interface UserService {
    RegisterUserResponseDTO registerUser(RegisterUserRequestDTO request);
}
