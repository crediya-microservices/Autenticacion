package com.crediya.r2dbc.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "`user`")
@Data
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String name;
    private String lastName;
    private String email;
    private String bornDate;
    private String address;
    private String phoneNumber;
    private String baseSalary;
}
