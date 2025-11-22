package com.example.virtual_exchange.domain;

public class User {
    private Long id;
    private String email;
    private String password;
    private String name;

    public User(Long id, String email, String password, String name) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
    }
}
