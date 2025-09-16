package com.running.endpoint.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.running.endpoint.api.AdminReassignedController;
import com.running.model.ReassignedClubDto;
import com.running.model.ReassignedRaceDto;
import com.running.service.ReassignmentQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AdminReassignedControllerTestApi {

    @Mock
    ReassignmentQueryService service;

    @InjectMocks
    AdminReassignedController controller;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new TestGlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    // Advice de test para mapear excepciones a HTTP
    @RestControllerAdvice
    static class TestGlobalExceptionHandler {
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // --------- RACES ---------

    @Test
    @DisplayName("GET /api/admin/reassigned/{uid}/races -> 200 y array con elementos")
    void getReassignedRaces_ok() throws Exception {
        var now = LocalDateTime.now();
        var r1 = ReassignedRaceDto.builder()
                .id(1L).name("10K Sevilla").place("Sevilla").distanceKm(10.0)
                .photo("p1.jpg").reassignedFromEmail("a@a.com").reassignedAt(now).build();
        var r2 = ReassignedRaceDto.builder()
                .id(2L).name("Media Córdoba").place("Córdoba").distanceKm(21.1)
                .photo("p2.jpg").reassignedFromEmail("b@b.com").reassignedAt(now).build();

        when(service.getReassignedRacesFor("u123")).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/admin/reassigned/{uid}/races", "u123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(service).getReassignedRacesFor("u123");
    }

    @Test
    @DisplayName("GET /api/admin/reassigned/{uid}/races -> 200 y array vacío")
    void getReassignedRaces_empty() throws Exception {
        when(service.getReassignedRacesFor("empty")).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/reassigned/{uid}/races", "empty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/admin/reassigned/{uid}/races -> si el servicio lanza excepción -> 500")
    void getReassignedRaces_error500() throws Exception {
        when(service.getReassignedRacesFor("boom")).thenThrow(new RuntimeException("boom races"));

        mockMvc.perform(get("/api/admin/reassigned/{uid}/races", "boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom races")));
    }

    // --------- CLUBS ---------

    @Test
    @DisplayName("GET /api/admin/reassigned/{uid}/clubs -> 200 y array con elementos")
    void getReassignedClubs_ok() throws Exception {
        var now = LocalDateTime.now();
        var c1 = ReassignedClubDto.builder()
                .id(10L).name("Club A").province("Sevilla").place("Sevilla")
                .members(50).photo("c1.jpg").reassignedFromEmail("x@x.com").reassignedAt(now).build();
        var c2 = ReassignedClubDto.builder()
                .id(20L).name("Club B").province("Cádiz").place("Cádiz")
                .members(30).photo("c2.jpg").reassignedFromEmail("y@y.com").reassignedAt(now).build();

        when(service.getReassignedClubsFor("u999")).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/admin/reassigned/{uid}/clubs", "u999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[1].id").value(20));

        verify(service).getReassignedClubsFor("u999");
    }

    @Test
    @DisplayName("GET /api/admin/reassigned/{uid}/clubs -> 200 y array vacío")
    void getReassignedClubs_empty() throws Exception {
        when(service.getReassignedClubsFor("empty")).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/reassigned/{uid}/clubs", "empty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/admin/reassigned/{uid}/clubs -> si el servicio lanza excepción -> 500")
    void getReassignedClubs_error500() throws Exception {
        when(service.getReassignedClubsFor("boom")).thenThrow(new RuntimeException("boom clubs"));

        mockMvc.perform(get("/api/admin/reassigned/{uid}/clubs", "boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom clubs")));
    }
}
