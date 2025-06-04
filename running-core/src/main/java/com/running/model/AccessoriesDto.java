package com.running.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessoriesDto {
    private Long id;
    private String title;
    private String description;
    private String photo;
    private List<String> features;
    private List<BrandDto> brands;
}

