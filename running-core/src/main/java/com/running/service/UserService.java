package com.running.service;

import com.running.model.Club;
import com.running.model.User;
import com.running.model.UserDto;
import com.running.model.UserRace;
import com.running.repository.ClubRepository;
import com.running.repository.UserRaceRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final UserRaceRepository userRaceRepository;

    public User saveFromDto(UserDto dto) {
        Optional<User> existingUserOpt = userRepository.findByEmail(dto.getEmail());

        if (existingUserOpt.isPresent()) {
            User user = existingUserOpt.get();

            // Siempre actualizar UID con el de Firebase
            user.setUID(dto.getUid());

            return userRepository.save(user);
        }

        // Si no existe, lo creamos con UID y club default
        Club defaultClub = clubRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Default club not found"));

        User newUser = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .surname(dto.getSurname())
                .UID(dto.getUid())
                .clubs(List.of(defaultClub))
                .build();

        return userRepository.save(newUser);
    }

    public void deleteByUID(String uid) {
        User user = userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("User not found with UID: " + uid));

        // Eliminar relaciones con clubes
        user.getClubs().clear();

        // Eliminar inscripciones del usuario en carreras
        userRaceRepository.deleteAll(userRaceRepository.findByUser_UID(user.getUID()));

        userRepository.delete(user);
    }



    public User findByUID(String uid) {
        return userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("User not found with UID: " + uid));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ✅ Unirse a un nuevo club
    @Transactional
    public void joinClub(String uid, Long newClubId) {
        User user = userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Club newClub = clubRepository.findById(newClubId)
                .orElseThrow(() -> new RuntimeException("Club not found"));

        // Eliminar el club default si está
        user.getClubs().removeIf(club -> club.getId() == 1L);

        // Añadir el nuevo club si no lo tiene
        if (!user.getClubs().contains(newClub)) {
            user.getClubs().add(newClub);
        }

        newClub.setMembers(newClub.getMembers() + 1);

        clubRepository.save(newClub);
        userRepository.save(user);
    }

    // ✅ Darse de baja de un club
    @Transactional
    public void leaveClub(String uid, Long clubId) {
        User user = findByUID(uid);
        Club clubToRemove = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club not found"));

        if (user.getClubs().contains(clubToRemove)) {
            user.getClubs().remove(clubToRemove);

            // Decrementar miembros (evitar que baje de 0)
            if (clubToRemove.getMembers() != null && clubToRemove.getMembers() > 0) {
                clubToRemove.setMembers(clubToRemove.getMembers() - 1);
            }

            // Si se queda sin clubes, añadir club por defecto
            if (user.getClubs().isEmpty()) {
                Club defaultClub = clubRepository.findById(1L)
                        .orElseThrow(() -> new RuntimeException("Default club not found"));
                user.getClubs().add(defaultClub);
            }

            // Guardar cambios
            userRepository.save(user);
            clubRepository.save(clubToRemove); // guardar el decremento
        }
    }



}
