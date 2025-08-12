package com.running.service;

import com.running.model.*;
import com.running.repository.ClubRepository;
import com.running.repository.RoleRepository;
import com.running.repository.UserRaceRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.FirebaseAuthException;

import jakarta.transaction.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final UserRaceRepository userRaceRepository;
    private final RoleRepository roleRepository;

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

    public RoleDto getUserRoleByUID(String uid) {
        User user = userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        RoleDto dto = new RoleDto();
        dto.setName(user.getRole().getName());
        return dto;
    }

    /**
     * Actualiza el nombre y apellido del usuario.
     *
     * @param uid Identificador único del usuario.
     * @param dto Objeto que contiene el nuevo nombre y apellido.
     * @return El usuario actualizado.
     */
    public User updateNameAndSurname(String uid, UserDto dto) {
        User user = userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        return userRepository.save(user);
    }

    /**
     * Obtiene una lista de todos los nombres de roles disponibles.
     *
     * @return Lista de nombres de roles.
     */
    public List<String> getAllRoleNames() {
        return roleRepository.findAll()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    /**
     * Guarda un nuevo usuario desde el administrador.
     * Asigna un rol y un club por defecto.
     *
     * @param dto Objeto que contiene los datos del usuario.
     * @return El usuario guardado.
     */
    public User saveFromAdminDto(UserDto dto) {
        Role role = roleRepository.findByName(dto.getRole())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        Club defaultClub = clubRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Default club not found"));

        User newUser = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .surname(dto.getSurname())
                .UID(dto.getUid())
                .role(role)
                .clubs(List.of(defaultClub))
                .build();

        return userRepository.save(newUser);
    }


    public User createUserWithFirebase(UserDto dto) {
        try {
            // ✅ Usa la contraseña generada en el frontend
            String password = dto.getPassword(); // debe estar en el DTO

            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(dto.getEmail())
                    .setPassword(password)
                    .setDisplayName(dto.getName() + " " + dto.getSurname());

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

            dto.setUid(userRecord.getUid());
            return saveFromAdminDto(dto); // esta NO guarda el password
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error creando usuario en Firebase: " + e.getMessage());
        }
    }


    public void deleteUserWithFirebase(String uid) {
        try {
            // 1. Eliminar en Firebase
            FirebaseAuth.getInstance().deleteUser(uid);

            // 2. Eliminar en base de datos
            deleteByUID(uid);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error deleting user in Firebase: " + e.getMessage());
        }
    }

    private String generateStrongPassword(int length) {
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        String allChars = lowercase + uppercase + numbers;

        StringBuilder password = new StringBuilder();
        password.append(uppercase.charAt((int) (Math.random() * uppercase.length())));
        password.append(numbers.charAt((int) (Math.random() * numbers.length())));
        password.append(lowercase.charAt((int) (Math.random() * lowercase.length())));

        for (int i = 3; i < length; i++) {
            password.append(allChars.charAt((int) (Math.random() * allChars.length())));
        }

        return password.toString();
    }

    public UserDto findByEmailDto(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        UserDto dto = new UserDto();
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setUid(user.getUID());
        dto.setRole(user.getRole() != null ? user.getRole().getName() : null);
        return dto;
    }

}
