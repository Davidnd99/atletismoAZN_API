package com.running.service;

import com.running.model.Club;
import com.running.model.ClubDto;
import com.running.model.User;
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
                .map(club -> new ClubDto(
                        club.getId(),
                        club.getName(),
                        club.getProvince(),
                        club.getPhoto(),
                        club.getPlace(),
                        club.getMembers(),
                        false,
                        club.getContact()))
                .collect(Collectors.toList());
    }

    // Obtener los clubs a los que pertenece un usuario
    public List<ClubDto> getClubsByUser(String uid) {
        Optional<User> userOpt = userRepository.findByUID(uid);
        if (userOpt.isEmpty()) return List.of();

        User user = userOpt.get();

        return user.getClubs().stream()
                .filter(club -> !"default".equalsIgnoreCase(club.getName()))
                .map(club -> new ClubDto(
                        club.getId(),
                        club.getName(),
                        club.getProvince(),
                        club.getPhoto(),
                        club.getPlace(),
                        club.getMembers(),
                        true,
                        club.getContact()))
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

        return new ClubDto(
                club.getId(),
                club.getName(),
                club.getProvince(),
                club.getPhoto(),
                club.getPlace(),
                club.getMembers(),
                joined,
                club.getContact());
    }

}
