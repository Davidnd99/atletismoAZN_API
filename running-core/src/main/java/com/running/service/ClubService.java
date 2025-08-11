package com.running.service;

import com.running.model.Club;
import com.running.model.ClubDto;
import com.running.model.User;
import com.running.model.UserDto;
import com.running.repository.ClubRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    // Obtener todos los clubs (con o sin filtro por provincia)
    public List<ClubDto> getAllClubs(String provincia) {
        List<Club> clubs = (provincia != null && !provincia.isBlank())
                ? clubRepository.findByProvinceIgnoreCaseAndNameNotIgnoreCase(provincia, "default")
                : clubRepository.findByNameNotIgnoreCase("default");

        return clubs.stream()
                .filter(club -> !"default".equalsIgnoreCase(club.getName()))
                .map(c -> toClubDto(c, false))
                .collect(Collectors.toList());
    }

    // Obtener los clubs a los que pertenece un usuario
    public List<ClubDto> getClubsByUser(String uid) {
        Optional<User> userOpt = userRepository.findByUID(uid);
        if (userOpt.isEmpty()) return List.of();

        User user = userOpt.get();

        return user.getClubs().stream()
                .filter(c -> !"default".equalsIgnoreCase(c.getName()))
                .map(c -> toClubDto(c, true))
                .collect(Collectors.toList());
    }

    // Obtener un club por ID y marcar si el usuario pertenece o no
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

    // NUEVO: miembros de un club
    public List<UserDto> getUsersByClub(Long clubId) {
        // Validar existencia del club
        clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club no encontrado"));

        List<User> users = userRepository.findByClubs_Id(clubId);

        return users.stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    // ===== Helpers de mapeo =====

    private UserDto toUserDto(User u) {
        UserDto dto = new UserDto();
        dto.setEmail(u.getEmail());
        dto.setName(u.getName());
        dto.setSurname(u.getSurname());
        dto.setUid(u.getUID());
        dto.setRole(u.getRole() != null ? u.getRole().getName() : null);
        // Nunca devolvemos password
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
