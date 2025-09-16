package com.running.model;

import lombok.Data;

@Data
public class UserDto {
    private String email;
    private String name;
    private String surname;
    private String uid;
    private String role;
    private String password;

}
