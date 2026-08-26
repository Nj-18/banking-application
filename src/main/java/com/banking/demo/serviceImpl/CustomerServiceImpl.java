package com.banking.demo.serviceImpl;

import com.banking.demo.entities.Customer;
import com.banking.demo.exceptions.CustomerNotFoundException;
import com.banking.demo.repositories.CustomerRepository;
import com.banking.demo.services.CustomerService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @CircuitBreaker(name = "customerService")
    @RateLimiter(name = "accountOps")
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    @CircuitBreaker(name = "customerService")
    @Retry(name = "readRetry")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    @CircuitBreaker(name = "customerService")
    @Retry(name = "readRetry")
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer not found with id : " + id));
    }
}
