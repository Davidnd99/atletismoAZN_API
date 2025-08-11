package com.running.service;

import com.running.model.*;
import com.running.repository.CareerRepository;
import com.running.repository.DifficultyRepository;
import com.running.repository.TypeRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// OrganizerService.java
@Service
@RequiredArgsConstructor
public class OrganizerService {

    private final CareerRepository careerRepository;
    private final DifficultyRepository difficultyRepository;
    private final TypeRepository typeRepository;
    private final UserRepository userRepository;

    public User requireOrganizerByUid(String uid) {
        User u = userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("User not found by uid"));
        if (!userRepository.existsByIdAndRole_Name(u.getId(), "organizator")) {
            throw new RuntimeException("User is not an organizator");
        }
        return u;
    }

    public List<Career> listMyRaces(String organizerUid) {
        return careerRepository.findByOrganizer_UIDOrderByDateDesc(organizerUid);
    }

    public Career createAsOrganizer(String organizerUid, CareerDto dto) {
        User me = requireOrganizerByUid(organizerUid);

        Difficulty difficulty = difficultyRepository.findById(dto.getIddifficulty().getIddifficulty())
                .orElseThrow(() -> new RuntimeException("Difficulty not found"));
        Type type = typeRepository.findById(dto.getType().getId_type())
                .orElseThrow(() -> new RuntimeException("Type not found"));

        Career c = Career.builder()
                .photo(dto.getPhoto())
                .name(dto.getName())
                .place(dto.getPlace())
                .distance_km(dto.getDistance_km())
                .date(dto.getDate())
                .province(dto.getProvince())
                .url(dto.getUrl())
                .difficulty(difficulty)
                .type(type)
                .slope(dto.getSlope())
                .registered(dto.getRegistered())
                .organizer(me) // clave
                .build();

        return careerRepository.save(c);
    }

    public Career updateMyRace(String organizerUid, Long raceId, CareerDto dto) {
        User me = requireOrganizerByUid(organizerUid);

        Career c = careerRepository.findById(raceId)
                .orElseThrow(() -> new RuntimeException("Career not found"));

        if (c.getOrganizer() == null || !c.getOrganizer().getId().equals(me.getId())) {
            throw new RuntimeException("No puedes gestionar esta carrera");
        }

        // actualiza campos permitidos:
        if (dto.getPhoto() != null) c.setPhoto(dto.getPhoto());
        if (dto.getName() != null) c.setName(dto.getName());
        if (dto.getPlace() != null) c.setPlace(dto.getPlace());
        if (dto.getDistance_km() != null) c.setDistance_km(dto.getDistance_km());
        if (dto.getDate() != null) c.setDate(dto.getDate());
        if (dto.getProvince() != null) c.setProvince(dto.getProvince());
        if (dto.getUrl() != null) c.setUrl(dto.getUrl());
        if (dto.getSlope() != null) c.setSlope(dto.getSlope());
        if (dto.getRegistered() != null) c.setRegistered(dto.getRegistered());
        if (dto.getType() != null) {
            Type t = typeRepository.findById(dto.getType().getId_type())
                    .orElseThrow(() -> new RuntimeException("Type not found"));
            c.setType(t);
        }
        if (dto.getIddifficulty() != null) {
            Difficulty d = difficultyRepository.findById(dto.getIddifficulty().getIddifficulty())
                    .orElseThrow(() -> new RuntimeException("Difficulty not found"));
            c.setDifficulty(d);
        }

        return careerRepository.save(c);
    }

    public void deleteMyRace(String organizerUid, Long raceId) {
        User me = requireOrganizerByUid(organizerUid);
        Career c = careerRepository.findById(raceId)
                .orElseThrow(() -> new RuntimeException("Career not found"));

        if (c.getOrganizer() == null || !c.getOrganizer().getId().equals(me.getId())) {
            throw new IllegalStateException("No puedes gestionar esta carrera");
        }
        careerRepository.deleteById(raceId);
    }
}

