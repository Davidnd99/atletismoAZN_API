package com.running.service;

import com.running.model.*;
import com.running.repository.RaceRepository;
import com.running.repository.DifficultyRepository;
import com.running.repository.TypeRepository;
import com.running.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RaceService {

    private final RaceRepository raceRepository;
    private final DifficultyRepository difficultyRepository;
    private final TypeRepository typeRepository;
    private final UserRepository userRepository; // <-- lo puedes mantener si lo usas en otros métodos


    private boolean isAdmin(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "admin");
    }


    public Race save(RaceDto request) {
        Difficulty difficulty = difficultyRepository.findById(request.getIddifficulty().getIddifficulty())
                .orElseThrow(() -> new RuntimeException("Difficulty not found with id: " + request.getIddifficulty().getIddifficulty()));

        Type type = typeRepository.findById(request.getType().getId_type())
                .orElseThrow(() -> new RuntimeException("Type not found with id: " + request.getType().getId_type()));

        Race race = Race.builder()
                .photo(request.getPhoto())
                .name(request.getName())
                .place(request.getPlace())
                .distance_km(request.getDistance_km())
                .date(request.getDate())
                .province(request.getProvince())
                .url(request.getUrl())
                .difficulty(difficulty)
                .type(type)
                .slope(request.getSlope())
                .registered(request.getRegistered())
                .build();

        // 👇 Sin asignación de organizer aquí
        return raceRepository.save(race);
    }

    public List<Race> filterRaces(
            String province,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Long typeId,
            Long difficultyId,
            Boolean finalizada
    ) {
        return raceRepository.filterRaces(
                blankToNull(province),
                typeId,
                difficultyId,
                fechaDesde,
                fechaHasta,
                finalizada,
                LocalDateTime.now()  // comparación con "ahora" para finalizada
        );
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public List<Race> findByOrganizerUid(String organizerUid) {
        return raceRepository.findByOrganizer_UIDOrderByDateDesc(organizerUid);
    }

    public Race findById(Long id) {
        return raceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Race not found with id: " + id));
    }

    public List<Race> findAll() { return raceRepository.findAll(); }

    public List<Race> findByProvince(String province) { return raceRepository.findByProvince(province); }

    public List<Race> findByType(Type type) { return raceRepository.findByType(type); }

    public List<Race> findByDifficulty(Difficulty difficulty) { return raceRepository.findByDifficulty(difficulty); }

    public List<Race> findByOrganizer(Long organizerUserId) {
        return raceRepository.findByOrganizer_IdOrderByDateDesc(organizerUserId);
    }

    /* ===== NUEVO: GET/PUT organizer de una carrera ===== */

    public OrganizerDto getOrganizerOfRace(Long raceId) {
        Race c = raceRepository.findByIdWithOrganizer(raceId)
                .orElseThrow(() -> new RuntimeException("Race not found"));
        if (c.getOrganizer() == null) return null;
        var u = c.getOrganizer();
        return new OrganizerDto(u.getUID(), u.getName(), u.getEmail());
    }

    @Transactional
    public OrganizerDto updateRaceOrganizer(Long raceId, String organizerEmail) {
        if (organizerEmail == null || organizerEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        Race c = raceRepository.findById(raceId)
                .orElseThrow(() -> new RuntimeException("Race not found"));

        var u = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new RuntimeException("User not found by email"));

        // ✅ ACEPTA organizator O admin
        boolean isOrganizator = userRepository.existsByIdAndRole_Name(u.getId(), "organizator");
        if (!isOrganizator && !isAdmin(u)) {
            throw new RuntimeException("El usuario debe ser 'organizator' o 'admin'");
        }

        c.setOrganizer(u);
        raceRepository.save(c);
        return new OrganizerDto(u.getUID(), u.getName(), u.getEmail());
    }
}
