package com.running.model;

import lombok.Data;

@Data
public class MarcaDto {
    private Long raceId;
    private String tiempo; // Ej: "01:23:45"
    private Integer posicion;
    private String comentarios;
}
