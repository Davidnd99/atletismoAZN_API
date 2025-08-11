package com.running.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CareerDto {
    private String photo;
    private String name;
    private String place;
    private Double distance_km;
    private LocalDateTime date;
    private String province;
    private Type type;
    private Difficulty iddifficulty;
    private Integer slope;
    private Integer registered;
    @Column(name = "url")
    private String url;
    private Long organizerUserId;
}
