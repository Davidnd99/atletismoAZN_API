package com.running.service;

import com.running.model.Accessories;
import com.running.model.AccessoriesDto;
import com.running.model.Brand;
import com.running.model.BrandDto;
import com.running.repository.AccessoriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
                        ? dto.getBrands().stream().map(this::toBrand).collect(Collectors.toList())
                        : null)
                .build();

        Accessories saved = repository.save(entity);
        return toDto(saved);
    }

    public List<Accessories> getAll() {
        return repository.findAll();
    }

    // === NUEVOS ===

    public Accessories getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Accessory not found"));
    }

    /** Actualización parcial: solo campos != null se aplican. */
    public AccessoriesDto update(Long id, AccessoriesDto dto) {
        Accessories acc = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Accessory not found"));

        if (dto.getTitle() != null)       acc.setTitle(dto.getTitle());
        if (dto.getDescription() != null) acc.setDescription(dto.getDescription());
        if (dto.getPhoto() != null)       acc.setPhoto(dto.getPhoto());
        if (dto.getFeatures() != null)    acc.setFeatures(dto.getFeatures());
        if (dto.getBrands() != null) {
            acc.setBrands(dto.getBrands().stream().map(this::toBrand).collect(Collectors.toList()));
        }

        Accessories saved = repository.save(acc);
        return toDto(saved);
    }

    public void delete(Long id) {
        Accessories acc = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Accessory not found"));
        repository.delete(acc);
    }

    // === Mappers ===

    private AccessoriesDto toDto(Accessories a) {
        return AccessoriesDto.builder()
                .id(a.getId())
                .title(a.getTitle())
                .description(a.getDescription())
                .photo(a.getPhoto())
                .features(a.getFeatures())
                .brands(a.getBrands() != null
                        ? a.getBrands().stream().map(this::toBrandDto).collect(Collectors.toList())
                        : null)
                .build();
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
