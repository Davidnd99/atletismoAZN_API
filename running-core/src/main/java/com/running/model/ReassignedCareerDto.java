package com.running.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReassignedCareerDto {
    private Long id;
    private String name;
    private String place;
    private Double distanceKm;
    private String photo;
    private java.time.LocalDateTime date;

    private String reassignedFromEmail;
    private LocalDateTime reassignedAt;
}