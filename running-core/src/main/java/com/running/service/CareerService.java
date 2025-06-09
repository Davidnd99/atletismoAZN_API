package com.running.service;

import com.running.model.Career;
import com.running.model.CareerDto;
import com.running.repository.CareerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CareerService {

    private final CareerRepository careerRepository;

    public Career save(CareerDto request) {
        Career career = Career.builder()
                .photo(request.getPhoto())
                .name(request.getName())
                .place(request.getPlace())
                .distance_km(request.getDistance_km())
                .date(request.getDate())
                .province(request.getProvince())
                .type(request.getType())
                .build();
        return careerRepository.save(career);
    }

    public List<Career> findAll() {
        return careerRepository.findAll();
    }

    public List<Career> findByProvince(String province) {
        return careerRepository.findByProvince(province);
    }

    public List<Career> findByType(int type) {
        return careerRepository.findByType(type);
    }
}
