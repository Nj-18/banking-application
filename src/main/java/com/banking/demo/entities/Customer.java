package com.banking.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

@Entity
@Table(name="customers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String mobileNumber;

    private String customerStatus;

    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"customer", "transactions"})
    private List<BankAccount> bankAccounts;

    @OneToOne(mappedBy = "customer")
    @JsonIgnore
    private User user;

}
