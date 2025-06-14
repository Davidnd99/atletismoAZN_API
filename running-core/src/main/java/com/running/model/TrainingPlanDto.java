package com.running.model;

import lombok.Data;

@Data
public class TrainingPlanDto {

    private Long idClub;
    private String name;
    private String pathPdf;
    private String uidUsuario; // UID del usuario que hace la petición
}
