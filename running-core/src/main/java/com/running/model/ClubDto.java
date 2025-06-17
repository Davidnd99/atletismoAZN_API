package com.running.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubDto {
    private Long id;
    private String name;
    private String province;
    private String photo;
    private String place;
    private Integer members;
    private boolean joined;
    private String contact;
}
