package com.example.ohtmon.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Integer id;
    private String username;
    private String password;        // BCrypt 해시
    private String name;
    private String role;            // ADMIN, ENGINEER, OPERATOR, VIEWER
    private LocalDateTime createdAt;
}
