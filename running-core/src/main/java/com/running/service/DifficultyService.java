// running-core/src/main/java/com/running/service/DifficultyService.java
package com.running.service;

import com.running.model.Difficulty;
import com.running.model.DifficultyDto;
import com.running.repository.DifficultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DifficultyService {

    private final DifficultyRepository difficultyRepository;

    public Difficulty save(DifficultyDto dto) {
        Difficulty difficulty = Difficulty.builder()
                .name(dto.getName())
                .build();
        return difficultyRepository.save(difficulty);
    }

    public List<Difficulty> findAll() {
        return difficultyRepository.findAll();
    }

    public Optional<Difficulty> findById(Long id) {
        return difficultyRepository.findById(id);
    }
}
