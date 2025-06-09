package com.running.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CareerDto {
    private String photo;
    private String name;
    private String place;
    private Double distance_km;
    private Date date;
    private String province;
    private int type;
}
