package com.running.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MarcaDto {

    private Long raceId;
    private LocalDateTime raceDate;
    private String tiempo; // Lo devolveremos como String en formato HH:mm:ss
    private Integer posicion;
    private String comentarios;

    public MarcaDto(String tiempo, Integer posicion, String comentarios) {
        this.tiempo = tiempo;
        this.posicion = posicion;
        this.comentarios = comentarios;
    }

}
