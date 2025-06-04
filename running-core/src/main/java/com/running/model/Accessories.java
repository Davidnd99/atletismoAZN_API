package com.running.model;

import jakarta.persistence.*;
import lombok.*;
import com.running.util.StringListJsonConverter;
import com.running.util.BrandListJsonConverter;
import java.util.List;

@Entity
@Table(name = "accessories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Accessories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private String photo;

    @Convert(converter = StringListJsonConverter.class)
    @Column(columnDefinition = "json")
    private List<String> features;

    @Convert(converter = BrandListJsonConverter.class)
    @Column(columnDefinition = "json")
    private List<Brand> brands;
}