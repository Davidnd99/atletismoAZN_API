package com.running.service;

import com.running.model.Race;
import com.running.model.ParticipantDto;
import com.running.model.User;
import com.running.repository.RaceRepository;
import com.running.repository.UserRaceRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final UserRaceRepository userRaceRepository;
    private final UserRepository userRepository;
    private final RaceRepository raceRepository;

    private boolean isAdmin(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "admin")
                || userRepository.existsByIdAndRole_Name(u.getId(), "administrator");
    }

    private boolean isOrganizator(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "organizator");
    }

    private User getUserByUidOr404(String uid) {
        return userRepository.findByUID(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found by uid"));
    }

    public List<ParticipantDto> listParticipants(String uid, Long raceId, String statusOpt) {
        User caller = getUserByUidOr404(uid);
        Race race = raceRepository.findById(raceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Race not found"));

        if (!isAdmin(caller)) {
            if (!isOrganizator(caller) ||
                    race.getOrganizer() == null ||
                    !race.getOrganizer().getId().equals(caller.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes ver los inscritos de esta carrera");
            }
        }

        String status = (statusOpt == null || statusOpt.isBlank()) ? null : statusOpt.toLowerCase();
        return userRaceRepository.findParticipantsByRaceAndRoleUser(raceId, status);
    }
}
