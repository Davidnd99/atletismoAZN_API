package com.running.mapper;

import com.running.model.User;
import com.running.model.UserDto;

import java.util.List;

public class UserMapper {

    public static User toEntity(UserDto dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEnabled(dto.isEnabled());
        user.setPassword(dto.getPasword());
        user.setUsername(dto.getUsername());

        return user;
    }

    public static UserDto toDto(User entity) {
        UserDto dto = new UserDto();
        dto.setEmail(entity.getEmail());
        dto.setName(entity.getName());
        dto.setSurname(entity.getSurname());
        dto.setEnabled(entity.isEnabled());
        dto.setPasword(entity.getPassword());
        dto.setUsername(entity.getUsername());

        return dto;
    }

    public static List<UserDto> toDtoList(List<User> users) {
        return users.stream().map(UserMapper::toDto).toList();
    }
}
