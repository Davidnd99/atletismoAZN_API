package com.running.service;

import com.running.model.*;
import com.running.repository.CareerRepository;
import com.running.repository.DifficultyRepository;
import com.running.repository.TypeRepository;
import com.running.repository.UserRepository;
import com.running.repository.UserRaceRepository; // 👈 NUEVO
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizerService {

    private final CareerRepository careerRepository;
    private final DifficultyRepository difficultyRepository;
    private final TypeRepository typeRepository;
    private final UserRepository userRepository;
    private final UserRaceRepository userRaceRepository; // 👈 NUEVO

    private boolean isAdmin(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "admin")
                || userRepository.existsByIdAndRole_Name(u.getId(), "administrator");
    }

    private boolean isOrganizator(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "organizator");
    }

    private User requireAdminOrOrganizator(String uid) {
        User u = userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("User not found by uid"));
        if (isAdmin(u) || isOrganizator(u)) return u;
        throw new RuntimeException("User must be admin or organizator");
    }

    /** ADMIN => todas; ORGANIZATOR => las suyas */
    public List<Career> listMyRaces(String uid) {
        User me = requireAdminOrOrganizator(uid);
        if (isAdmin(me)) return careerRepository.findAll();
        return careerRepository.findByOrganizer_UIDOrderByDateDesc(uid);
    }

    /** Crea carrera; el organizador será el propio caller (admin u organizator). */
    public Career createAsOrganizer(String uid, CareerDto dto) {
        User me = requireAdminOrOrganizator(uid);

        Difficulty diff = difficultyRepository.findById(dto.getIddifficulty().getIddifficulty())
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
                .difficulty(diff)
                .type(type)
                .slope(dto.getSlope())
                .registered(dto.getRegistered())
                .organizer(me)
                .build();

        return careerRepository.save(c);
    }

    /** ADMIN => puede editar cualquiera; ORGANIZATOR => solo si es suya. */
    public Career updateMyRace(String uid, Long raceId, CareerDto dto) {
        User me = requireAdminOrOrganizator(uid);
        Career c = careerRepository.findById(raceId)
                .orElseThrow(() -> new RuntimeException("Career not found"));

        if (!isAdmin(me)) {
            if (c.getOrganizer() == null || !c.getOrganizer().getId().equals(me.getId())) {
                throw new RuntimeException("No puedes gestionar esta carrera");
            }
        }

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

        // === Reasignar organizador (solo ADMIN)
        if (dto.getOrganizerUserId() != null) {
            if (!isAdmin(me)) {
                throw new RuntimeException("Solo un ADMIN puede cambiar el organizador de la carrera");
            }
            User newOrganizer = userRepository.findById(dto.getOrganizerUserId())
                    .orElseThrow(() -> new RuntimeException("Organizer user not found by id: " + dto.getOrganizerUserId()));
            if (!userRepository.existsByIdAndRole_Name(newOrganizer.getId(), "organizator")) {
                throw new RuntimeException("El usuario destino no tiene rol 'organizator'");
            }
            if (c.getOrganizer() == null || !c.getOrganizer().getId().equals(newOrganizer.getId())) {
                c.setOrganizer(newOrganizer);
            }
        }

        return careerRepository.save(c);
    }

    /** ADMIN => puede borrar cualquiera; ORGANIZATOR => solo si es suya. */
    public void deleteMyRace(String uid, Long raceId) {
        User me = requireAdminOrOrganizator(uid);
        Career c = careerRepository.findById(raceId)
                .orElseThrow(() -> new RuntimeException("Career not found"));

        if (!isAdmin(me)) {
            if (c.getOrganizer() == null || !c.getOrganizer().getId().equals(me.getId())) {
                throw new RuntimeException("No puedes gestionar esta carrera");
            }
        }
        careerRepository.deleteById(raceId);
    }

    // === NUEVO: cancelar una inscripción PENDIENTE de un usuario en una carrera ===
    public void cancelPendingRegistration(String organizerUid, Long raceId, String targetUserUid) {
        User organizer = requireAdminOrOrganizator(organizerUid);
        Career c = careerRepository.findById(raceId)
                .orElseThrow(() -> new RuntimeException("Career not found"));

        // Si no es admin, debe ser el organizador de la carrera
        if (!isAdmin(organizer)) {
            if (c.getOrganizer() == null || !c.getOrganizer().getId().equals(organizer.getId())) {
                throw new RuntimeException("No autorizado para cancelar inscripciones en esta carrera");
            }
        }

        var ur = userRaceRepository.findPendingByRaceIdAndUserUid(raceId, targetUserUid)
                .orElseThrow(() -> new RuntimeException("No existe inscripción PENDIENTE para ese usuario en esta carrera"));

        // Política: marcar como cancelada (si prefieres eliminar, usa delete)
        ur.setStatus("cancelada");
        userRaceRepository.save(ur);

        // Si quieres eliminar el registro en lugar de marcar cancelada:
        // userRaceRepository.delete(ur);
    }
}
