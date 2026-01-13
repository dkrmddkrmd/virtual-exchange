package com.example.virtual_exchange.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberSignupDto {
    private String email;
    private String password;
    private String name;
}
