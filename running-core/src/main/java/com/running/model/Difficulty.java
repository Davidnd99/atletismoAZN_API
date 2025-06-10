package com.running.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "difficulty")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Difficulty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long iddifficulty;
    private String name;
}
