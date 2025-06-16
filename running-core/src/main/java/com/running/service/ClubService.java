package com.running.service;

import com.running.model.Club;
import com.running.model.ClubDto;
import com.running.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;

    public List<ClubDto> getClubs(String provincia) {
        List<Club> clubs;

        if (provincia != null && !provincia.isEmpty()) {
            clubs = clubRepository.findByProvinceIgnoreCase(provincia);
        } else {
            clubs = clubRepository.findAll();
        }

        // Filtramos el club con ID = 1 (default)
        return clubs.stream()
                .filter(club -> club.getId() != 1L)
                .map(club -> new ClubDto(club.getId(), club.getName(), club.getProvince(), club.getPhoto()))
                .toList();
    }


    public ClubDto getClubById(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Club not found"));
        return toDto(club);
    }

    private ClubDto toDto(Club club) {
        return ClubDto.builder()
                .id(club.getId())
                .name(club.getName())
                .province(club.getProvince())
                .photo(club.getPhoto())
                .build();
    }
}