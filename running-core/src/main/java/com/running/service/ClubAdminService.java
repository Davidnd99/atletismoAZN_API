package com.running.service;

import com.running.model.Club;
import com.running.model.ClubDto;
import com.running.model.User;
import com.running.repository.ClubRepository;
import com.running.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubAdminService {

    private final ClubRepository clubRepository;
    private final UserRepository userRepository;

    private User requireClubAdminByUid(String uid) {
        User u = userRepository.findByUID(uid)
                .orElseThrow(() -> new RuntimeException("User not found by uid"));
        if (!userRepository.existsByIdAndRole_Name(u.getId(), "club-administrator")) {
            throw new RuntimeException("User is not a club-administrator");
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
                .joined(joinedFlag) // en admin normalmente false; lo dejamos por compatibilidad
                .build();
    }

    // Listar clubs del admin
    public List<ClubDto> listMyClubs(String managerUid) {
        return clubRepository.findByManager_UIDOrderByNameAsc(managerUid)
                .stream().map(c -> toDto(c, false)).toList();
    }

    // Crear club y asignar manager
    public ClubDto createAsManager(String managerUid, ClubDto dto) {
        User me = requireClubAdminByUid(managerUid);

        Club c = Club.builder()
                .name(dto.getName())
                .province(dto.getProvince())
                .place(dto.getPlace())
                .photo(dto.getPhoto())
                .members(dto.getMembers())
                .contact(dto.getContact())
                .manager(me)
                .build();

        return toDto(clubRepository.save(c), false);
    }

    // Actualizar club del admin
    public ClubDto updateMyClub(String managerUid, Long clubId, ClubDto dto) {
        User me = requireClubAdminByUid(managerUid);
        Club c = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club not found"));

        if (c.getManager() == null || !c.getManager().getId().equals(me.getId())) {
            throw new RuntimeException("No puedes gestionar este club");
        }

        if (dto.getName() != null) c.setName(dto.getName());
        if (dto.getProvince() != null) c.setProvince(dto.getProvince());
        if (dto.getPlace() != null) c.setPlace(dto.getPlace());
        if (dto.getPhoto() != null) c.setPhoto(dto.getPhoto());
        if (dto.getMembers() != null) c.setMembers(dto.getMembers());
        if (dto.getContact() != null) c.setContact(dto.getContact());

        return toDto(clubRepository.save(c), false);
    }

    // Borrar club del admin
    public void deleteMyClub(String managerUid, Long clubId) {
        User me = requireClubAdminByUid(managerUid);
        Club c = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club not found"));

        if (c.getManager() == null || !c.getManager().getId().equals(me.getId())) {
            throw new RuntimeException("No puedes borrar este club");
        }
        clubRepository.deleteById(clubId);
    }
}

