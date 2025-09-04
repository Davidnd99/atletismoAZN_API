package com.running.endpoint.api;

import com.running.model.ReassignedClubDto;
import com.running.model.ReassignedCareerDto;
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
    public List<ReassignedCareerDto> getReassignedRaces(@PathVariable String uid) {
        return service.getReassignedCareersFor(uid);
    }

    @GetMapping("/{uid}/clubs")
    public List<ReassignedClubDto> getReassignedClubs(@PathVariable String uid) {
        return service.getReassignedClubsFor(uid);
    }
}
