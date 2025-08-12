package com.running.service;

import com.running.model.*;
import com.running.repository.ClubRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubService {

    private static final String CLUB_ADMIN_ROLE = "club-administrator";

    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    // ===== EXISTENTES =====

    public List<ClubDto> getAllClubs(String provincia) {
        List<Club> clubs = (provincia != null && !provincia.isBlank())
                ? clubRepository.findByProvinceIgnoreCaseAndNameNotIgnoreCase(provincia, "default")
                : clubRepository.findByNameNotIgnoreCase("default");

        return clubs.stream()
                .filter(club -> !"default".equalsIgnoreCase(club.getName()))
                .map(c -> toClubDto(c, false))
                .collect(Collectors.toList());
    }

    public List<ClubDto> getClubsByUser(String uid) {
        Optional<User> userOpt = userRepository.findByUID(uid);
        if (userOpt.isEmpty()) return List.of();

        User user = userOpt.get();

        return user.getClubs().stream()
                .filter(c -> !"default".equalsIgnoreCase(c.getName()))
                .map(c -> toClubDto(c, true))
                .collect(Collectors.toList());
    }

    public ClubDto getClubById(Long id, String uid) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        boolean joined = false;
        if (uid != null) {
            Optional<User> userOpt = userRepository.findByUID(uid);
            if (userOpt.isPresent()) {
                joined = userOpt.get().getClubs().contains(club);
            }
        }

        return toClubDto(club, joined);
    }

    public List<UserDto> getUsersByClub(Long clubId) {
        clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        List<User> users = userRepository.findByClubs_Id(clubId);

        return users.stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    public AdminClubDto getManagerOfClub(Long clubId) {
        Club club = clubRepository.findByIdWithManager(clubId)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));
        User m = club.getManager();
        if (m == null) return null;
        return new AdminClubDto(m.getUID(), m.getName(), m.getEmail());
    }

    // ===== NUEVOS =====

    /**
     * Actualiza datos del club (NO toca el manager).
     */
    @Transactional
    public ClubDto updateClub(Long clubId, ClubDto data) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        if (data.getName() != null) club.setName(data.getName());
        if (data.getPlace() != null) club.setPlace(data.getPlace());
        if (data.getProvince() != null) club.setProvince(data.getProvince());
        if (data.getPhoto() != null) club.setPhoto(data.getPhoto());
        if (data.getContact() != null) club.setContact(data.getContact());
        // members se suele gestionar de forma interna, no lo toco aquí

        Club saved = clubRepository.save(club);
        return toClubDto(saved, false);
    }

    /**
     * Cambia el administrador del club por email (valida rol).
     */
    @Transactional
    public AdminClubDto updateClubManager(Long clubId, String managerEmail) {
        Club club = clubRepository.findByIdWithManager(clubId)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        User newManager = userRepository.findByEmail(managerEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ese email"));

        if (newManager.getRole() == null ||
                newManager.getRole().getName() == null ||
                !CLUB_ADMIN_ROLE.equalsIgnoreCase(newManager.getRole().getName())) {
            throw new RuntimeException("El usuario no tiene rol '" + CLUB_ADMIN_ROLE + "'");
        }

        club.setManager(newManager);
        clubRepository.save(club);

        return new AdminClubDto(newManager.getUID(), newManager.getName(), newManager.getEmail());
    }

    // ===== Helpers =====

    private UserDto toUserDto(User u) {
        UserDto dto = new UserDto();
        dto.setEmail(u.getEmail());
        dto.setName(u.getName());
        dto.setSurname(u.getSurname());
        dto.setUid(u.getUID());
        dto.setRole(u.getRole() != null ? u.getRole().getName() : null);
        return dto;
    }

    private ClubDto toClubDto(Club c, boolean joined) {
        return ClubDto.builder()
                .id(c.getId())
                .name(c.getName())
                .province(c.getProvince())
                .photo(c.getPhoto())
                .place(c.getPlace())
                .members(c.getMembers())
                .contact(c.getContact())
                .joined(joined)
                .build();
    }
}
