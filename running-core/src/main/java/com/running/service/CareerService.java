package com.running.service;

import com.running.model.*;
import com.running.repository.CareerRepository;
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
public class CareerService {

    private final CareerRepository careerRepository;
    private final DifficultyRepository difficultyRepository;
    private final TypeRepository typeRepository;
    private final UserRepository userRepository; // <-- lo puedes mantener si lo usas en otros métodos


    private boolean isAdmin(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "admin");
    }


    public Career save(CareerDto request) {
        Difficulty difficulty = difficultyRepository.findById(request.getIddifficulty().getIddifficulty())
                .orElseThrow(() -> new RuntimeException("Difficulty not found with id: " + request.getIddifficulty().getIddifficulty()));

        Type type = typeRepository.findById(request.getType().getId_type())
                .orElseThrow(() -> new RuntimeException("Type not found with id: " + request.getType().getId_type()));

        Career career = Career.builder()
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
        return careerRepository.save(career);
    }

    public List<Career> filterCareers(
            String province,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Long typeId,
            Long difficultyId,
            Boolean finalizada
    ) {
        return careerRepository.filterCareers(
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

    public List<Career> findByOrganizerUid(String organizerUid) {
        return careerRepository.findByOrganizer_UIDOrderByDateDesc(organizerUid);
    }

    public Career findById(Long id) {
        return careerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Career not found with id: " + id));
    }

    public List<Career> findAll() { return careerRepository.findAll(); }

    public List<Career> findByProvince(String province) { return careerRepository.findByProvince(province); }

    public List<Career> findByType(Type type) { return careerRepository.findByType(type); }

    public List<Career> findByDifficulty(Difficulty difficulty) { return careerRepository.findByDifficulty(difficulty); }

    public List<Career> findByOrganizer(Long organizerUserId) {
        return careerRepository.findByOrganizer_IdOrderByDateDesc(organizerUserId);
    }

    /* ===== NUEVO: GET/PUT organizer de una carrera ===== */

    public OrganizerDto getOrganizerOfRace(Long raceId) {
        Career c = careerRepository.findByIdWithOrganizer(raceId)
                .orElseThrow(() -> new RuntimeException("Career not found"));
        if (c.getOrganizer() == null) return null;
        var u = c.getOrganizer();
        return new OrganizerDto(u.getUID(), u.getName(), u.getEmail());
    }

    @Transactional
    public OrganizerDto updateRaceOrganizer(Long raceId, String organizerEmail) {
        if (organizerEmail == null || organizerEmail.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        Career c = careerRepository.findById(raceId)
                .orElseThrow(() -> new RuntimeException("Career not found"));

        var u = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new RuntimeException("User not found by email"));

        // ✅ ACEPTA organizator O admin
        boolean isOrganizator = userRepository.existsByIdAndRole_Name(u.getId(), "organizator");
        if (!isOrganizator && !isAdmin(u)) {
            throw new RuntimeException("El usuario debe ser 'organizator' o 'admin'");
        }

        c.setOrganizer(u);
        careerRepository.save(c);
        return new OrganizerDto(u.getUID(), u.getName(), u.getEmail());
    }
}
