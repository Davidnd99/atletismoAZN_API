package com.running.service;

import com.running.model.*;
import com.running.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.FirebaseAuthException;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private final CareerRepository careerRepository;
    private final ReassignmentLogRepository reassignmentLogRepository;

    public User saveFromDto(UserDto dto) {
        Optional<User> existingUserOpt = userRepository.findByEmail(dto.getEmail());

        if (existingUserOpt.isPresent()) {
            User user = existingUserOpt.get();
            user.setUID(dto.getUid()); // siempre actualiza UID
            return userRepository.save(user);
        }

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

    @Transactional
    public void deleteByUID(String uid) {
        User user = userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("User not found with UID: " + uid));

        // ❌ ANTES: user.getClubs().clear();  (provocaba LazyInitialization)
        // ✅ AHORA: romper relaciones vía tabla puente y actualizar contador
        detachUserFromAllClubs(user);

        // Eliminar inscripciones del usuario en carreras
        userRaceRepository.deleteAll(userRaceRepository.findByUser_UID(user.getUID()));

        // Borrar usuario
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

        // quitar default si está
        user.getClubs().removeIf(club -> club.getId() == 1L);

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

            if (clubToRemove.getMembers() != null && clubToRemove.getMembers() > 0) {
                clubToRemove.setMembers(clubToRemove.getMembers() - 1);
            }

            if (user.getClubs().isEmpty()) {
                Club defaultClub = clubRepository.findById(1L)
                        .orElseThrow(() -> new RuntimeException("Default club not found"));
                user.getClubs().add(defaultClub);
            }

            userRepository.save(user);
            clubRepository.save(clubToRemove);
        }
    }

    public RoleDto getUserRoleByUID(String uid) {
        User user = userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        RoleDto dto = new RoleDto();
        dto.setName(user.getRole().getName());
        return dto;
    }

    public User updateNameAndSurname(String uid, UserDto dto) {
        User user = userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        return userRepository.save(user);
    }

    public List<String> getAllRoleNames() {
        return roleRepository.findAll()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

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
            String password = dto.getPassword();
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(dto.getEmail())
                    .setPassword(password)
                    .setDisplayName(dto.getName() + " " + dto.getSurname());

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

            dto.setUid(userRecord.getUid());
            return saveFromAdminDto(dto);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error creando usuario en Firebase: " + e.getMessage());
        }
    }

    public void deleteUserWithFirebase(String uid) {
        try {
            FirebaseAuth.getInstance().deleteUser(uid);
            deleteByUID(uid);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException("Error deleting user in Firebase: " + e.getMessage());
        }
    }

    @Transactional
    public void adminDeleteWithFirebase(String targetUid, String actingAdminUid) {
        adminDeleteAndReassign(targetUid, actingAdminUid);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                try {
                    FirebaseAuth.getInstance().deleteUser(targetUid);
                } catch (FirebaseAuthException ignored) {}
            }
        });
    }

    @Transactional
    public void adminDeleteAndReassign(String targetUid, String actingAdminUid) {
        User admin = userRepository.findByUID(actingAdminUid)
                .orElseThrow(() -> new RuntimeException("Acting admin not found"));
        if (!isAdmin(admin)) throw new RuntimeException("Only ADMIN can perform this action");
        if (actingAdminUid.equals(targetUid)) throw new RuntimeException("Admin cannot delete itself with this flow");

        User toDelete = userRepository.findByUID(targetUid)
                .orElseThrow(() -> new RuntimeException("User to delete not found"));

        // 1) IDs a reasignar
        List<Long> careerIds = isOrganizator(toDelete)
                ? careerRepository.findIdsByOrganizer(toDelete) : List.of();

        boolean isClubAdmin = userRepository.existsByIdAndRole_Name(toDelete.getId(), "club-administrator");
        List<Long> clubIds = isClubAdmin
                ? clubRepository.findIdsByManager(toDelete) : List.of();

        // 2) Reasignar
        if (!careerIds.isEmpty()) careerRepository.reassignOrganizer(toDelete, admin);
        if (!clubIds.isEmpty())   clubRepository.reassignManager(toDelete, admin);

        // 3) Log
        if (!careerIds.isEmpty()) {
            var logs = careerIds.stream()
                    .map(id -> ReassignmentLog.builder()
                            .entityType(ReassignmentLog.EntityType.CAREER)
                            .entityId(id)
                            .fromUser(toDelete)
                            .toUser(admin)
                            .build())
                    .toList();
            reassignmentLogRepository.saveAll(logs);
        }
        if (!clubIds.isEmpty()) {
            var logs = clubIds.stream()
                    .map(id -> ReassignmentLog.builder()
                            .entityType(ReassignmentLog.EntityType.CLUB)
                            .entityId(id)
                            .fromUser(toDelete)
                            .toUser(admin)
                            .build())
                    .toList();
            reassignmentLogRepository.saveAll(logs);
        }

        // 4) romper membresías sin tocar colección lazy + borrar inscripciones + borrar usuario
        detachUserFromAllClubs(toDelete);
        userRaceRepository.deleteAll(userRaceRepository.findByUser_UID(toDelete.getUID()));
        userRepository.delete(toDelete);
    }

    // ---- Helpers ----

    /** Rompe todas las membresías del usuario vía tabla puente y ajusta el contador de members. */
    private void detachUserFromAllClubs(User user) {
        // IDs de clubs antes de borrar filas de user_club (para actualizar contador)
        List<Long> clubIds = userRepository.findClubIdsByUserId(user.getId());

        // borrar filas en user_club
        userRepository.deleteAllClubsByUserId(user.getId());

        // actualizar contador members si procede
        if (clubIds != null && !clubIds.isEmpty()) {
            for (Long cid : clubIds) {
                clubRepository.decrementMembers(cid);
            }
        }
    }

    private boolean isAdmin(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "admin");
    }

    private boolean isOrganizator(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "organizator");
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
