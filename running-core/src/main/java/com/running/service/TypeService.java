package com.running.service;

import com.running.model.Type;
import com.running.model.TypeDto;
import com.running.repository.TypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TypeService {

    private final TypeRepository typeRepository;

    public Type save(TypeDto dto) {
        Type type = Type.builder()
                .name(dto.getName())
                .build();
        return typeRepository.save(type);
    }

    public List<Type> findAll() {
        return typeRepository.findAll();
    }

    public Optional<Type> findById(Long id) {
        return typeRepository.findById(id);
    }
}
