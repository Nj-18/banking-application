package com.banking.demo.serviceImpl;

import com.banking.demo.enums.Role;
import com.banking.demo.dtos.RegisterUserRequestDTO;
import com.banking.demo.dtos.RegisterUserResponseDTO;
import com.banking.demo.entities.Customer;
import com.banking.demo.entities.User;
import com.banking.demo.exceptions.CustomerNotFoundException;
import com.banking.demo.exceptions.EmailAlreadyExistsException;
import com.banking.demo.exceptions.UsernameAlreadyExistsException;
import com.banking.demo.repositories.CustomerRepository;
import com.banking.demo.repositories.UserRepository;
import com.banking.demo.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegisterUserResponseDTO registerUser(RegisterUserRequestDTO request) {

        // Check username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException(
                    "Username already exists : " + request.getUsername());
        }

        // Check email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(
                    "Email already exists : " + request.getEmail());
        }

        // Find customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer not found with id : "
                                        + request.getCustomerId()));

        // Create User
        User user = new User();

        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());

        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        user.setCustomer(customer);

        // Save User
        User savedUser = userRepository.save(user);

        // Prepare Response
        RegisterUserResponseDTO response = new RegisterUserResponseDTO();

        response.setUserId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole().name());
        response.setMessage("User registered successfully.");

        return response;
    }
}