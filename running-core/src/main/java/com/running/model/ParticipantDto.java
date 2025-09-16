package com.running.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParticipantDto {
    private String uid;
    private String name;
    private String email;
}
