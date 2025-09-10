package com.running.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "race")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String photo;
    private String name;
    private String place;
    private Double distance_km;
    private LocalDateTime date;
    private String province;
    private Integer slope;
    private Integer registered;
    @Column(name = "url")
    private String url;
    @ManyToOne
    @JoinColumn(name = "id_type", nullable = false)
    private Type type;
    @ManyToOne
    @JoinColumn(name = "iddifficulty", nullable = false)
    private Difficulty difficulty;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organizer_user_id")
    private User organizer;
}
