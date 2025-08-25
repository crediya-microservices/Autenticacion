package com.crediya.model.user;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class User {
    private String id;
    private String name;
    private String lastName;
    private String email;
    private LocalDate bornDate;
    private String address;
    private String phoneNumber;
    private BigDecimal baseSalary;
    private String identityDocument;
    private String roleName;
}