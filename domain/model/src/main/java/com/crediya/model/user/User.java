package com.crediya.model.user;


import lombok.Data;

@Data
public class User {
    private String id;
    private String name;
    private String lastName;
    private String email;
    private String bornDate;
    private String address;
    private String phoneNumber;
    private String baseSalary;
}
