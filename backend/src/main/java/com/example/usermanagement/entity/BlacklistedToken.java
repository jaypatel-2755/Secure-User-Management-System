package com.example.usermanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class BlacklistedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false)
    private String token;

    private LocalDateTime blacklistedAt = LocalDateTime.now();

    // getters & setters
    public Long getId() { return id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
