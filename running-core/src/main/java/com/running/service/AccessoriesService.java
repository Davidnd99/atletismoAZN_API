package com.running.service;

import com.running.model.Accessories;
import com.running.model.AccessoriesDto;
import com.running.model.Brand;
import com.running.model.BrandDto;
import com.running.repository.AccessoriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessoriesService {

    private final AccessoriesRepository repository;

    public AccessoriesDto save(AccessoriesDto dto) {
        Accessories entity = Accessories.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .photo(dto.getPhoto())
                .features(dto.getFeatures())
                .brands(dto.getBrands() != null
                        ? dto.getBrands().stream()
                        .map(this::toBrand)
                        .collect(Collectors.toList())
                        : null)
                .build();

        Accessories saved = repository.save(entity);

        return AccessoriesDto.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .photo(saved.getPhoto())
                .features(saved.getFeatures())
                .brands(saved.getBrands() != null
                        ? saved.getBrands().stream()
                        .map(this::toBrandDto)
                        .collect(Collectors.toList())
                        : null)
                .build();
    }

    public List<Accessories> getAll() {
        return repository.findAll();
    }

    private Brand toBrand(BrandDto dto) {
        return Brand.builder()
                .name(dto.getName())
                .img(dto.getImg())
                .url(dto.getUrl())
                .build();
    }

    private BrandDto toBrandDto(Brand brand) {
        return BrandDto.builder()
                .name(brand.getName())
                .img(brand.getImg())
                .url(brand.getUrl())
                .build();
    }
}

