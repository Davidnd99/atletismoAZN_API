package com.running.service;

import com.running.model.*;
import com.running.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizerService {

    private final RaceRepository raceRepository;
    private final DifficultyRepository difficultyRepository;
    private final TypeRepository typeRepository;
    private final UserRepository userRepository;
    private final UserRaceRepository userRaceRepository;

    private boolean isAdmin(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "admin")
                || userRepository.existsByIdAndRole_Name(u.getId(), "administrator");
    }

    private boolean isOrganizator(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "organizator");
    }

    private User requireAdminOrOrganizator(String uid) {
        User u = userRepository.findByUID(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found by uid"));
        if (isAdmin(u) || isOrganizator(u)) return u;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User must be admin or organizator");
    }

    public List<Race> listMyRaces(String uid) {
        User me = requireAdminOrOrganizator(uid);
        if (isAdmin(me)) return raceRepository.findAll();
        return raceRepository.findByOrganizer_UIDOrderByDateDesc(uid);
    }

    /** Crea carrera; impide duplicar nombre globalmente (case-insensitive). */
    public Race createAsOrganizer(String uid, RaceDto dto) {
        User me = requireAdminOrOrganizator(uid);

        String name = dto.getName() == null ? null : dto.getName().trim();
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la carrera es obligatorio");
        }
        if (raceRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una carrera con ese nombre");
        }

        Difficulty diff = difficultyRepository.findById(dto.getIddifficulty().getIddifficulty())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Difficulty not found"));
        Type type = typeRepository.findById(dto.getType().getId_type())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Type not found"));

        Race c = Race.builder()
                .photo(dto.getPhoto())
                .name(name)
                .place(dto.getPlace())
                .distance_km(dto.getDistance_km())
                .date(dto.getDate())
                .province(dto.getProvince())
                .url(dto.getUrl())
                .difficulty(diff)
                .type(type)
                .slope(dto.getSlope())
                .registered(dto.getRegistered())
                .organizer(me)
                .build();

        try {
            return raceRepository.save(c);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una carrera con ese nombre", e);
        }
    }

    /** Edita carrera; bloquea cambio de nombre a uno ya usado. */
    public Race updateMyRace(String uid, Long raceId, RaceDto dto) {
        User me = requireAdminOrOrganizator(uid);
        Race c = raceRepository.findById(raceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Race not found"));

        if (!isAdmin(me)) {
            if (c.getOrganizer() == null || !c.getOrganizer().getId().equals(me.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes gestionar esta carrera");
            }
        }

        if (dto.getName() != null) {
            String newName = dto.getName().trim();
            if (!newName.equalsIgnoreCase(c.getName())) {
                if (raceRepository.existsByNameIgnoreCaseAndIdNot(newName, c.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una carrera con ese nombre");
                }
                c.setName(newName);
            }
        }

        if (dto.getPhoto() != null) c.setPhoto(dto.getPhoto());
        if (dto.getPlace() != null) c.setPlace(dto.getPlace());
        if (dto.getDistance_km() != null) c.setDistance_km(dto.getDistance_km());
        if (dto.getDate() != null) c.setDate(dto.getDate());
        if (dto.getProvince() != null) c.setProvince(dto.getProvince());
        if (dto.getUrl() != null) c.setUrl(dto.getUrl());
        if (dto.getSlope() != null) c.setSlope(dto.getSlope());
        if (dto.getRegistered() != null) c.setRegistered(dto.getRegistered());

        if (dto.getType() != null) {
            Type t = typeRepository.findById(dto.getType().getId_type())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Type not found"));
            c.setType(t);
        }
        if (dto.getIddifficulty() != null) {
            Difficulty d = difficultyRepository.findById(dto.getIddifficulty().getIddifficulty())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Difficulty not found"));
            c.setDifficulty(d);
        }

        if (dto.getOrganizerUserId() != null) {
            if (!isAdmin(me)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un ADMIN puede cambiar el organizador de la carrera");
            }
            User newOrganizer = userRepository.findById(dto.getOrganizerUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizer user not found by id: " + dto.getOrganizerUserId()));
            if (!userRepository.existsByIdAndRole_Name(newOrganizer.getId(), "organizator")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario destino no tiene rol 'organizator'");
            }
            if (c.getOrganizer() == null || !c.getOrganizer().getId().equals(newOrganizer.getId())) {
                c.setOrganizer(newOrganizer);
            }
        }

        try {
            return raceRepository.save(c);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una carrera con ese nombre", e);
        }
    }

    /** ADMIN => puede borrar cualquiera; ORGANIZATOR => solo si es suya. */
    public void deleteMyRace(String uid, Long raceId) {
        User me = requireAdminOrOrganizator(uid);
        Race c = raceRepository.findById(raceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Race not found"));

        if (!isAdmin(me)) {
            if (c.getOrganizer() == null || !c.getOrganizer().getId().equals(me.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes gestionar esta carrera");
            }
        }
        raceRepository.deleteById(raceId);
    }

    // Cancelar una inscripción PENDIENTE de un usuario en una carrera
    public void cancelPendingRegistration(String organizerUid, Long raceId, String targetUserUid) {
        User organizer = requireAdminOrOrganizator(organizerUid);
        Race c = raceRepository.findById(raceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Race not found"));

        if (!isAdmin(organizer)) {
            if (c.getOrganizer() == null || !c.getOrganizer().getId().equals(organizer.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para cancelar inscripciones en esta carrera");
            }
        }

        var ur = userRaceRepository.findPendingByRaceIdAndUserUid(raceId, targetUserUid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe inscripción PENDIENTE para ese usuario en esta carrera"));

        ur.setStatus("cancelada");
        userRaceRepository.save(ur);
    }
}
