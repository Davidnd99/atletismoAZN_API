package com.running.service;

import com.running.model.*;
import com.running.repository.CareerRepository;
import com.running.repository.UserRaceRepository;
import com.running.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<UserRaceResponseDto> getUserRacesByStatus(String uid, String status) {
        User user = userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<UserRace> userRaces = userRaceRepository.findByUserIdAndStatus(user.getId(), status);

        return userRaces.stream().map(ur -> UserRaceResponseDto.builder()
                .raceId(ur.getRace().getId())
                .raceName(ur.getRace().getName())
                .place(ur.getRace().getPlace())
                .distanceKm(ur.getRace().getDistance_km())
                .raceDate(ur.getRace().getDate())
                .registrationDate(ur.getRegistrationDate())
                .status(ur.getStatus())
                .photo(ur.getRace().getPhoto())
                .build()
        ).toList();
    }



    /**
     * 1️⃣ Listar TODAS las carreras confirmadas (aunque tiempo sea null)
     */
    public List<MarcaDto> obtenerMarcas(String uid) {
        return userRaceRepository.findByUser_UID(uid)
                .stream()
                .filter(ur -> "confirmada".equalsIgnoreCase(ur.getStatus()))
                .map(ur -> {
                    MarcaDto dto = new MarcaDto();
                    dto.setRaceId(ur.getRace().getId());
                    dto.setRaceDate(ur.getRace().getDate());
                    dto.setTiempo(ur.getTiempo() != null ? ur.getTiempo().toString() : null);
                    dto.setPosicion(ur.getPosicion());
                    dto.setComentarios(ur.getComentarios());
                    return dto;
                })
                .collect(Collectors.toList());
    }



    /**
     * 2️⃣ Obtener una marca concreta (aunque tiempo sea null)
     */
    public MarcaDto obtenerMarcaPorCarrera(String uid, Long raceId) {
        UserRace ur = userRaceRepository.findByUser_UIDAndRace_Id(uid, raceId)
                .orElseThrow(() -> new RuntimeException("No existe inscripción para esa carrera"));

        if (!"confirmada".equalsIgnoreCase(ur.getStatus())) {
            throw new RuntimeException("La carrera no está confirmada");
        }

        MarcaDto dto = new MarcaDto();
        dto.setRaceId(ur.getRace().getId());
        dto.setTiempo(ur.getTiempo() != null ? ur.getTiempo().toString() : null);
        dto.setPosicion(ur.getPosicion());
        dto.setComentarios(ur.getComentarios());
        return dto;
    }

    /**
     * 3️⃣ Registrar o actualizar una marca
     */
    @Transactional
    public void actualizarMarca(String uid, Long raceId, MarcaDto dto) {
        UserRace ur = userRaceRepository.findByUser_UIDAndRace_Id(uid, raceId)
                .orElseThrow(() -> new RuntimeException("No existe inscripción para esa carrera"));

        if (!"confirmada".equalsIgnoreCase(ur.getStatus())) {
            throw new RuntimeException("Solo se pueden registrar marcas de carreras confirmadas");
        }

        LocalTime parsedTime = parseFlexibleTime(dto.getTiempo());

        ur.setTiempo(parsedTime);
        ur.setPosicion(dto.getPosicion());
        ur.setComentarios(dto.getComentarios());

        userRaceRepository.save(ur);
    }

    /**
     * 4️⃣ Eliminar una marca (dejarla en blanco)
     */
    @Transactional
    public void eliminarMarca(String uid, Long raceId) {
        UserRace ur = userRaceRepository.findByUser_UIDAndRace_Id(uid, raceId)
                .orElseThrow(() -> new RuntimeException("No existe inscripción para esa carrera"));

        if (!"confirmada".equalsIgnoreCase(ur.getStatus())) {
            throw new RuntimeException("Solo se pueden eliminar marcas de carreras confirmadas");
        }

        ur.setTiempo(null);
        ur.setPosicion(null);
        ur.setComentarios(null);

        userRaceRepository.save(ur);
    }


    private LocalTime parseFlexibleTime(String input) {
        if (input == null || input.isEmpty()) {
            throw new RuntimeException("El tiempo no puede estar vacío");
        }

        String[] parts = input.split(":");

        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        try {
            if (parts.length == 3) {
                // HH:mm:ss
                hours = Integer.parseInt(parts[0]);
                minutes = Integer.parseInt(parts[1]);
                seconds = Integer.parseInt(parts[2]);
            } else if (parts.length == 2) {
                // HH:mm
                hours = Integer.parseInt(parts[0]);
                minutes = Integer.parseInt(parts[1]);
            } else if (parts.length == 1) {
                // HH
                hours = Integer.parseInt(parts[0]);
            } else {
                throw new RuntimeException("Formato de tiempo inválido. Usa HH:mm:ss, HH:mm o HH.");
            }

            return LocalTime.of(hours, minutes, seconds);

        } catch (NumberFormatException | DateTimeException e) {
            throw new RuntimeException(
                    "El formato del tiempo debe ser HH:mm:ss, HH:mm o HH. Ejemplo: 01:23:45 o 15:45",
                    e
            );
        }
    }


}

