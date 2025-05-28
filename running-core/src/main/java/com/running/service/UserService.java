package com.running.service;

import com.running.mapper.UserMapper;
import com.running.model.User;
import com.running.model.UserDto;
import com.running.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUser(UserDto userDto) {
        User user = UserMapper.toEntity(userDto);
        userRepository.save(user);
    }

    public void updateUser(UserDto userDto) {
        User user = UserMapper.toEntity(userDto);
        userRepository.save(user);
    }

    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return UserMapper.toDtoList(users);
    }

    public UserDto getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void deleteUserByUsername(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteByUsername(username);
    }

    public void updateUserByUsername(String username, UserDto userDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(userDto.getEmail());
        user.setUsername(userDto.getUsername());
        user.setPassword(user.getPassword());
        user.setName(userDto.getName());
        user.setEnabled(userDto.isEnabled());
        user.setSurname(userDto.getSurname());

        userRepository.save(user);
    }
}
