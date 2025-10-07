package com.running.service;

import com.running.model.Club;
import com.running.model.TrainingPlan;
import com.running.model.TrainingPlanDto;
import com.running.model.User;
import com.running.repository.ClubRepository;
import com.running.repository.TrainingPlanRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    private boolean isAdmin(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "admin")
                || userRepository.existsByIdAndRole_Name(u.getId(), "administrator");
    }

    private boolean isClubAdmin(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "club-administrator");
    }

    private User requireAdminOrClubAdmin(String uid) {
        User u = userRepository.findByUID(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found by uid"));
        if (isAdmin(u) || isClubAdmin(u)) return u;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User must be admin or club-administrator");
    }

    /* ========== CREATE ========== */
    /** Unicidad: (club_id, name) case-insensitive */
    public TrainingPlan save(String uid, TrainingPlanDto dto) {
        requireAdminOrClubAdmin(uid);

        String name = dto.getName() == null ? null : dto.getName().trim();
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del plan es obligatorio");
        }

        Club club = clubRepository.findById(dto.getIdClub())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found"));

        if (trainingPlanRepository.existsByClub_IdAndNameIgnoreCase(club.getId(), name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese club ya tiene un plan con ese nombre");
        }

        TrainingPlan plan = TrainingPlan.builder()
                .club(club)
                .name(name)
                .contentJson(dto.getContentJson())
                .build();

        try {
            return trainingPlanRepository.save(plan);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese club ya tiene un plan con ese nombre", e);
        }
    }

    /** Compatibilidad (mismo criterio) */
    @Deprecated
    public TrainingPlan save(TrainingPlanDto dto) {
        String name = dto.getName() == null ? null : dto.getName().trim();
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del plan es obligatorio");
        }

        Club club = clubRepository.findById(dto.getIdClub())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found"));

        if (trainingPlanRepository.existsByClub_IdAndNameIgnoreCase(club.getId(), name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese club ya tiene un plan con ese nombre");
        }

        TrainingPlan plan = TrainingPlan.builder()
                .club(club)
                .name(name)
                .contentJson(dto.getContentJson())
                .build();

        try {
            return trainingPlanRepository.save(plan);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese club ya tiene un plan con ese nombre", e);
        }
    }

    /* ========== READ ========== */
    public TrainingPlan getById(Long id) {
        return trainingPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training plan not found"));
    }

    public List<TrainingPlan> findByClubId(Long idClub) {
        Club club = clubRepository.findById(idClub)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found"));
        return trainingPlanRepository.findByClub(club);
    }

    public List<TrainingPlan> findAll() {
        return trainingPlanRepository.findAll();
    }

    /* ========== UPDATE ========== */
    public TrainingPlan update(String uid, Long id, TrainingPlanDto dto) {
        requireAdminOrClubAdmin(uid);

        TrainingPlan plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training plan not found"));

        // Determina club objetivo y nombre objetivo tras la actualización
        Club targetClub = plan.getClub();
        if (dto.getIdClub() != null) {
            targetClub = clubRepository.findById(dto.getIdClub())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found"));
        }

        String targetName = plan.getName();
        if (dto.getName() != null) {
            targetName = dto.getName().trim();
            if (targetName.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del plan es obligatorio");
            }
        }

        boolean clubChanged = !targetClub.getId().equals(plan.getClub().getId());
        boolean nameChanged = !targetName.equalsIgnoreCase(plan.getName());
        if (clubChanged || nameChanged) {
            if (trainingPlanRepository.existsByClub_IdAndNameIgnoreCaseAndIdNot(targetClub.getId(), targetName, id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese club ya tiene un plan con ese nombre");
            }
        }

        // Aplica cambios
        plan.setClub(targetClub);
        plan.setName(targetName);
        if (dto.getContentJson() != null) {
            plan.setContentJson(dto.getContentJson());
        }

        try {
            return trainingPlanRepository.save(plan);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese club ya tiene un plan con ese nombre", e);
        }
    }

    /* ========== DELETE ========== */
    public void delete(String uid, Long id) {
        requireAdminOrClubAdmin(uid);

        TrainingPlan plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training plan not found"));

        trainingPlanRepository.delete(plan);
    }
}
