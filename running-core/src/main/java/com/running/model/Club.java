package com.running.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.running.model.TrainingPlan;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "club")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String province;
    private String photo;
    private String place;
    private Integer members;
    private String contact;

    @OneToMany(mappedBy = "club")
    @JsonManagedReference
    private List<TrainingPlan> trainingPlans;

    @ManyToMany(mappedBy = "clubs")
    @JsonBackReference
    private List<User> users;

    // NUEVO: gestor del club (un único user con rol "club-administrator")
    @ManyToOne(fetch = FetchType.EAGER)        // EAGER para evitar el problema del proxy
    @JoinColumn(name = "manager_user_id")
    private User manager;
}