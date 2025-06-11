package com.running.service;

import com.running.model.Career;
import com.running.model.CareerDto;
import com.running.model.Difficulty;
import com.running.model.Type;
import com.running.repository.CareerRepository;
import com.running.repository.DifficultyRepository;
import com.running.repository.TypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CareerService {

    private final CareerRepository careerRepository;
    private final DifficultyRepository difficultyRepository;
    private final TypeRepository typeRepository;

    public Career save(CareerDto request) {
        Difficulty difficulty = difficultyRepository.findById(request.getIddifficulty().getIddifficulty())
                .orElseThrow(() -> new RuntimeException("Difficulty not found with id: " + request.getIddifficulty()));

        Type type = typeRepository.findById(request.getType().getId_type())
                .orElseThrow(() -> new RuntimeException("Type not found with id: " + request.getType().getId_type()));

        Career career = Career.builder()
                .photo(request.getPhoto())
                .name(request.getName())
                .place(request.getPlace())
                .distance_km(request.getDistance_km())
                .date(request.getDate())
                .province(request.getProvince())
                .difficulty(difficulty)    // relación JPA con objeto completo
                .type(type)                // relación JPA con objeto completo
                .build();

        return careerRepository.save(career);
    }

    public Career findById(Long id) {
        return careerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Career not found with id: " + id));
    }

    public List<Career> findAll() {
        return careerRepository.findAll();
    }

    public List<Career> findByProvince(String province) {
        return careerRepository.findByProvince(province);
    }

    public List<Career> findByType(Type type) {
        return careerRepository.findByType(type);
    }

    public List<Career> findByDifficulty(Difficulty difficulty) {
        return careerRepository.findByDifficulty(difficulty);
    }
}
