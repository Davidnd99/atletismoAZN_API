package com.running.endpoint.api;

import com.running.model.ReassignedClubDto;
import com.running.model.ReassignedRaceDto;
import com.running.service.ReassignmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins="*")
@RestController
@RequestMapping("/api/admin/reassigned")
@RequiredArgsConstructor
public class AdminReassignedController {

    private final ReassignmentQueryService service;

    @GetMapping("/{uid}/races")
    public List<ReassignedRaceDto> getReassignedRaces(@PathVariable String uid) {
        return service.getReassignedRacesFor(uid);
    }

    @GetMapping("/{uid}/clubs")
    public List<ReassignedClubDto> getReassignedClubs(@PathVariable String uid) {
        return service.getReassignedClubsFor(uid);
    }
}
