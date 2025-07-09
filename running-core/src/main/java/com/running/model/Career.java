package com.running.model;

import com.running.util.BrandListJsonConverter;
import com.running.util.StringListJsonConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "career")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Career {

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
}
