package com.running.service;

import com.running.model.User;
import com.running.model.UserRace;
import com.running.model.UserRaceResponseDto;
import com.running.repository.UserRaceRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserRaceService {

    private final UserRaceRepository userRaceRepository;
    private final UserRepository userRepository;

    private boolean isAdmin(User u) {
        if (u == null || u.getRole() == null || u.getRole().getName() == null) return false;
        String r = u.getRole().getName();
        return "admin".equalsIgnoreCase(r) || "administrator".equalsIgnoreCase(r);
    }

    private boolean isOrganizator(User u) {
        if (u == null || u.getRole() == null || u.getRole().getName() == null) return false;
        return "organizator".equalsIgnoreCase(u.getRole().getName());
    }

    // ✅ Listar inscripciones PENDIENTE de todas las carreras de un organizer (por UID)
    public List<UserRaceResponseDto> listPendingByOrganizer(String organizerUid) {
        User actor = userRepository.findByUID(organizerUid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!isOrganizator(actor) && !isAdmin(actor)) {
            return List.of();
        }

        List<UserRace> urs = userRaceRepository
                .findByRace_Organizer_IdAndStatus(actor.getId(), "pendiente");

        return urs.stream().map(this::toResponse).toList();
    }

    // ✅ Listar inscripciones PENDIENTE de una carrera concreta
    public List<UserRaceResponseDto> listPendingByRace(Long raceId) {
        List<UserRace> urs = userRaceRepository.findByRace_IdAndStatus(raceId, "pendiente");
        return urs.stream().map(this::toResponse).toList();
    }

    // ✅ Cancelar TODAS las PENDIENTE de un organizer (por UID)
    @Transactional
    public int cancelAllPendingByOrganizer(String organizerUid) {
        User actor = userRepository.findByUID(organizerUid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!isOrganizator(actor) && !isAdmin(actor)) {
            return 0;
        }

        return userRaceRepository.cancelAllPendingByOrganizerId(actor.getId());
    }

    // ✅ Cancelar TODAS las PENDIENTE de una carrera concreta
    @Transactional
    public int cancelAllPendingByRace(Long raceId) {
        return userRaceRepository.cancelAllPendingByRace(raceId);
    }

    // ---- mapper a DTO de respuesta ----
    private UserRaceResponseDto toResponse(UserRace ur) {
        return UserRaceResponseDto.builder()
                .raceId(ur.getRace().getId())
                .raceName(ur.getRace().getName())
                .place(ur.getRace().getPlace())
                .distanceKm(ur.getRace().getDistance_km())
                .raceDate(ur.getRace().getDate())
                .registrationDate(ur.getRegistrationDate())
                .status(ur.getStatus())
                .photo(ur.getRace().getPhoto())
                .build();
    }
}
