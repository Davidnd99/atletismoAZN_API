package com.running.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandDto {
    private String img;
    private String name;
    private String url;
}

