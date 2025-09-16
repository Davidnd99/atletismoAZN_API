package com.running.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "carousel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarouselItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcarousel")
    private Long id;

    @Column(name = "photo", length = 255, nullable = false)
    private String photo;
}
