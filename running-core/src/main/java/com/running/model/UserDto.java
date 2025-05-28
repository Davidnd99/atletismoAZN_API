package com.running.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String username;
    private String pasword;
    private String email;
    private String name;
    private String surname;
    private boolean enabled;
}
