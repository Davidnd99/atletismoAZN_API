package com.running.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReassignedClubDto {
    private Long id;
    private String name;
    private String province;
    private String place;
    private Integer members;
    private String photo;

    private String reassignedFromEmail;
    private LocalDateTime reassignedAt;
}