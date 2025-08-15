package com.running.service;

import com.running.model.Club;
import com.running.model.TrainingPlan;
import com.running.model.TrainingPlanDto;
import com.running.model.User;
import com.running.repository.ClubRepository;
import com.running.repository.TrainingPlanRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    /* =========================
       Helpers de autorización
       ========================= */
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

    /* ==========
       CREATE
       ========== */
    /** NUEVO: creación con control de permisos (admin / club-administrator). */
    public TrainingPlan save(String uid, TrainingPlanDto dto) {
        requireAdminOrClubAdmin(uid);

        Club club = clubRepository.findById(dto.getIdClub())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found"));

        TrainingPlan plan = TrainingPlan.builder()
                .club(club)
                .name(dto.getName())
                .contentJson(dto.getContentJson())
                .build();

        return trainingPlanRepository.save(plan);
    }

    /** (Mantengo tu método antiguo por compatibilidad interna; si no se usa, puedes borrarlo) */
    @Deprecated
    public TrainingPlan save(TrainingPlanDto dto) {
        Club club = clubRepository.findById(dto.getIdClub())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found"));

        TrainingPlan plan = TrainingPlan.builder()
                .club(club)
                .name(dto.getName())
                .contentJson(dto.getContentJson())
                .build();

        return trainingPlanRepository.save(plan);
    }

    /* ==========
       READ
       ========== */
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

    /* ==========
       UPDATE
       ========== */
    public TrainingPlan update(String uid, Long id, TrainingPlanDto dto) {
        requireAdminOrClubAdmin(uid);

        TrainingPlan plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training plan not found"));

        // Si llega un idClub, actualizamos el club destino:
        if (dto.getIdClub() != null) {
            Club club = clubRepository.findById(dto.getIdClub())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found"));
            plan.setClub(club);
        }

        if (dto.getName() != null) {
            plan.setName(dto.getName());
        }

        if (dto.getContentJson() != null) {
            plan.setContentJson(dto.getContentJson());
        }

        // Si en el futuro agregas campos como description o date en la entidad:
        // if (dto.getDescription() != null) plan.setDescription(dto.getDescription());
        // if (dto.getDate() != null) plan.setDate(dto.getDate());

        return trainingPlanRepository.save(plan);
    }

    /* ==========
       DELETE
       ========== */
    public void delete(String uid, Long id) {
        requireAdminOrClubAdmin(uid);

        TrainingPlan plan = trainingPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training plan not found"));

        trainingPlanRepository.delete(plan);
    }
}
