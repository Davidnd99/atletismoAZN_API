package com.running.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class UserRaceResponseDto {
    private Long raceId;
    private String raceName;
    private String place;
    private Double distanceKm;
    private LocalDateTime raceDate;
    private LocalDateTime registrationDate;
    private String status;
}

