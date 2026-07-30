package com.banking.demo.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserRequestDTO {
    private String username;
    private String password;
    private String email;
    private Long customerId;
}
