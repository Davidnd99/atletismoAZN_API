package com.running.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "user_race")
@IdClass(UserRaceId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRace {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "race_id")
    private Career race;

    private LocalDateTime registrationDate;

    private String status; // pendiente | confirmada | cancelada

    private LocalTime tiempo;

    private Integer posicion;

    private String comentarios;

    private LocalTime pace;
}

