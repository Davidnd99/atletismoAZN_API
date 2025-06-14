package com.running.service;

import com.running.model.*;
import com.running.repository.CareerRepository;
import com.running.repository.UserRaceRepository;
import com.running.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserRaceService {

    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final UserRaceRepository userRaceRepository;

    @Transactional
    public void preRegister(String uid, Long raceId) {
        User user = userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Career race = careerRepository.findById(raceId)
                .orElseThrow(() -> new RuntimeException("Carrera no encontrada"));

        UserRace userRace = UserRace.builder()
                .user(user)
                .race(race)
                .registrationDate(LocalDateTime.now())
                .status("pendiente")
                .build();

        userRaceRepository.save(userRace);
    }

    @Transactional
    public void confirmRegistration(String uid, Long raceId) {
        User user = userRepository.findByUID(uid).orElseThrow();
        UserRaceId id = new UserRaceId(user.getId(), raceId);

        UserRace ur = userRaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        ur.setStatus("confirmada");
        userRaceRepository.save(ur);
    }

    @Transactional
    public void cancelRegistration(String uid, Long raceId) {
        User user = userRepository.findByUID(uid).orElseThrow();
        UserRaceId id = new UserRaceId(user.getId(), raceId);

        UserRace ur = userRaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscripción no encontrada"));

        ur.setStatus("cancelada");
        userRaceRepository.save(ur);
    }

    public List<UserRaceResponseDto> getUserRaceDtos(String uid) {
        List<UserRace> userRaces = userRaceRepository.findByUser_UID(uid);

        return userRaces.stream().map(ur -> {
            return UserRaceResponseDto.builder()
                    .raceId(ur.getRace().getId())
                    .raceName(ur.getRace().getName())
                    .place(ur.getRace().getPlace())
                    .distanceKm(ur.getRace().getDistance_km())
                    .raceDate(ur.getRace().getDate())
                    .registrationDate(ur.getRegistrationDate())
                    .status(ur.getStatus())
                    .build();
        }).toList();
    }

    public String getStatus(String uid, Long raceId) {
        Optional<User> userOpt = userRepository.findByUID(uid);
        if (userOpt.isEmpty()) return null;

        Optional<UserRace> inscription = userRaceRepository.findByUserIdAndRaceId(userOpt.get().getId(), raceId);
        return inscription.map(UserRace::getStatus).orElse(null);
    }


}

