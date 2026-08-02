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

    // Optional: when customerId is absent, create a customer from these fields.
    private String firstName;
    private String lastName;
    private String mobileNumber;
}
