package com.running.service;

import com.running.model.Club;
import com.running.model.TrainingPlan;
import com.running.model.TrainingPlanDto;
import com.running.model.User;
import com.running.repository.ClubRepository;
import com.running.repository.TrainingPlanRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    public TrainingPlan save(TrainingPlanDto dto) {
        Club club = clubRepository.findById(dto.getIdClub())
                .orElseThrow(() -> new RuntimeException("Club not found"));

        User user = userRepository.findByUID(dto.getUidUsuario())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean perteneceAlClub = user.getClubs().stream()
                .anyMatch(c -> c.getId().equals(club.getId()));

        if (!perteneceAlClub) {
            throw new RuntimeException("User does not belong to the club");
        }

        TrainingPlan plan = TrainingPlan.builder()
                .club(club)
                .name(dto.getName())
                .pathPdf(dto.getPathPdf())
                .build();

        return trainingPlanRepository.save(plan);
    }

    public List<TrainingPlan> findByClubId(Long idClub, String uidUsuario) {
        Club club = clubRepository.findById(idClub)
                .orElseThrow(() -> new RuntimeException("Club not found"));

        User user = userRepository.findByUID(uidUsuario)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean perteneceAlClub = user.getClubs().stream()
                .anyMatch(c -> c.getId().equals(club.getId()));

        if (!perteneceAlClub) {
            throw new RuntimeException("Access denied to this club's training plans");
        }

        return trainingPlanRepository.findByClub(club);
    }

    public List<TrainingPlan> findAll() {
        return trainingPlanRepository.findAll();
    }
}
