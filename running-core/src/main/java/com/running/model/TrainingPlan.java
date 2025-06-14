package com.running.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "training_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_club", nullable = false)
    @JsonBackReference
    private Club club;

    private String name;
    private String pathPdf;
}