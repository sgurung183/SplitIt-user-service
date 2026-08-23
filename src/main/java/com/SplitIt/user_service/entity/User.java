package com.SplitIt.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
## Entity: User
- id (Long, auto-generated)
- firstName, lastName (String)
- email (String, unique)
- phoneNumber (String, unique)
- passwordHash (String)
- stripeConnectedAccountId (String, nullable)
- createdAt (LocalDateTime)
 */
@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    private String passwordHash;

    private String stripeConnectedAccountId; // nullable by default

    private LocalDateTime createdAt;

}
