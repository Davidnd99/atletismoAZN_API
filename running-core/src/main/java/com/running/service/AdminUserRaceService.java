package com.running.service;

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

    // ✅ Listar inscripciones PENDIENTE de todas las carreras de un organizador (por UID)
    public List<UserRaceResponseDto> listPendingByOrganizer(String organizerUid) {
        var organizator = userRepository.findByUID(organizerUid)
                .orElseThrow(() -> new RuntimeException("Organizador no encontrado"));

        // (Opcional) validar rol
        if (organizator.getRole() == null ||
                !"organizator".equalsIgnoreCase(organizator.getRole().getName())) {
            throw new RuntimeException("El UID no corresponde a un usuario con rol 'organizator'");
        }

        List<UserRace> urs = userRaceRepository
                .findByRace_Organizer_IdAndStatus(organizator.getId(), "pendiente");

        return urs.stream().map(this::toResponse).toList();
    }

    // ✅ Listar inscripciones PENDIENTE de una carrera concreta
    public List<UserRaceResponseDto> listPendingByRace(Long raceId) {
        List<UserRace> urs = userRaceRepository.findByRace_IdAndStatus(raceId, "pendiente");
        return urs.stream().map(this::toResponse).toList();
    }

    // ✅ Cancelar TODAS las PENDIENTE de un organizador (por UID)
    @Transactional
    public int cancelAllPendingByOrganizer(String organizerUid) {
        var organizator = userRepository.findByUID(organizerUid)
                .orElseThrow(() -> new RuntimeException("Organizador no encontrado"));

        if (organizator.getRole() == null ||
                !"organizator".equalsIgnoreCase(organizator.getRole().getName())) {
            throw new RuntimeException("El UID no corresponde a un usuario con rol 'organizator'");
        }

        return userRaceRepository.cancelAllPendingByOrganizerId(organizator.getId());
    }

    // ✅ Cancelar TODAS las PENDIENTE de una carrera concreta
    @Transactional
    public int cancelAllPendingByRace(Long raceId) {
        return userRaceRepository.cancelAllPendingByRace(raceId);
    }

    // ---- mapper a DTO de respuesta (reutiliza tu formato) ----
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

