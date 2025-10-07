package com.running.service;

import com.running.model.Club;
import com.running.model.ClubDto;
import com.running.model.User;
import com.running.repository.ClubRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubAdminService {

    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    private boolean isAdmin(User u) {
        return userRepository.existsByIdAndRole_Name(u.getId(), "admin");
    }

    private User requireClubAdminByUid(String uid) {
        User u = userRepository.findByUID(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found by uid"));
        boolean ok = isAdmin(u) || userRepository.existsByIdAndRole_Name(u.getId(), "club-administrator");
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not admin nor club-administrator");
        }
        return u;
    }

    private ClubDto toDto(Club c, boolean joinedFlag) {
        return ClubDto.builder()
                .id(c.getId())
                .name(c.getName())
                .province(c.getProvince())
                .photo(c.getPhoto())
                .place(c.getPlace())
                .members(c.getMembers())
                .contact(c.getContact())
                .joined(joinedFlag)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ClubDto> listMyClubs(String managerUid) {
        return clubRepository.findByManager_UIDOrderByNameAsc(managerUid)
                .stream().map(c -> toDto(c, false)).toList();
    }

    @Transactional
    public ClubDto createAsManager(String managerUid, ClubDto dto) {
        User me = requireClubAdminByUid(managerUid);

        String name = dto.getName() != null ? dto.getName().trim() : null;
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del club es obligatorio");
        }
        if (clubRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un club con ese nombre");
        }

        Club c = Club.builder()
                .name(name)
                .province(dto.getProvince())
                .place(dto.getPlace())
                .photo(dto.getPhoto())
                .members(dto.getMembers())
                .contact(dto.getContact())
                .manager(me)
                .build();

        try {
            return toDto(clubRepository.save(c), false);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un club con ese nombre", e);
        }
    }

    @Transactional
    public ClubDto updateMyClub(String managerUid, Long clubId, ClubDto dto) {
        User me = requireClubAdminByUid(managerUid);
        Club c = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found"));

        if (c.getManager() == null || !c.getManager().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes gestionar este club");
        }

        if (dto.getName() != null) {
            String newName = dto.getName().trim();
            if (!newName.equalsIgnoreCase(c.getName())) {
                if (clubRepository.existsByNameIgnoreCaseAndIdNot(newName, c.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un club con ese nombre");
                }
                c.setName(newName);
            }
        }

        if (dto.getProvince() != null) c.setProvince(dto.getProvince());
        if (dto.getPlace() != null) c.setPlace(dto.getPlace());
        if (dto.getPhoto() != null) c.setPhoto(dto.getPhoto());
        if (dto.getMembers() != null) c.setMembers(dto.getMembers());
        if (dto.getContact() != null) c.setContact(dto.getContact());

        try {
            return toDto(clubRepository.save(c), false);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un club con ese nombre", e);
        }
    }

    @Transactional
    public void deleteMyClub(String managerUid, Long clubId) {
        User me = requireClubAdminByUid(managerUid);
        Club c = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found"));

        if (c.getManager() == null || !c.getManager().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes borrar este club");
        }
        clubRepository.deleteById(clubId);
    }
}
