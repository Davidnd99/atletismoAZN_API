package com.running.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.running.endpoint.api.AdminUserRaceController;
import com.running.model.UserRaceResponseDto;
import com.running.service.AdminUserRaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AdminUserRaceControllerTestApi {

    @Mock
    AdminUserRaceService service;

    @InjectMocks
    AdminUserRaceController controller;

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

    // Advice de prueba para convertir excepciones en respuestas HTTP limpias
    @RestControllerAdvice
    static class TestGlobalExceptionHandler {
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // ---------- pendingByOrganizer ----------

    @Test
    @DisplayName("GET /pending/by-organizer/{uid} -> 200 y lista con elementos")
    void pendingByOrganizer_ok() throws Exception {
        var now = LocalDateTime.now();
        var r1 = UserRaceResponseDto.builder()
                .raceId(11L).raceName("10K Sevilla").place("Sevilla")
                .distanceKm(10.0).raceDate(now).registrationDate(now).status("pendiente").photo("p1.jpg")
                .build();
        var r2 = UserRaceResponseDto.builder()
                .raceId(22L).raceName("Media Córdoba").place("Córdoba")
                .distanceKm(21.1).raceDate(now).registrationDate(now).status("pendiente").photo("p2.jpg")
                .build();

        when(service.listPendingByOrganizer("org-1")).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/admin/registrations/pending/by-organizer/{uid}", "org-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].raceId").value(11))
                .andExpect(jsonPath("$[1].raceId").value(22));

        verify(service).listPendingByOrganizer("org-1");
    }

    @Test
    @DisplayName("GET /pending/by-organizer/{uid} -> 200 y lista vacía")
    void pendingByOrganizer_empty() throws Exception {
        when(service.listPendingByOrganizer("empty")).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/registrations/pending/by-organizer/{uid}", "empty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /pending/by-organizer/{uid} -> servicio lanza excepción -> 500")
    void pendingByOrganizer_error500() throws Exception {
        when(service.listPendingByOrganizer("boom")).thenThrow(new RuntimeException("boom organizer"));

        mockMvc.perform(get("/api/admin/registrations/pending/by-organizer/{uid}", "boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom organizer")));
    }

    // ---------- pendingByRace ----------

    @Test
    @DisplayName("GET /pending/by-race/{raceId} -> 200 y lista con elementos")
    void pendingByRace_ok() throws Exception {
        var now = LocalDateTime.now();
        var r1 = UserRaceResponseDto.builder()
                .raceId(77L).raceName("5K Barrio").place("Dos Hermanas")
                .distanceKm(5.0).raceDate(now).registrationDate(now).status("pendiente").photo("x.jpg")
                .build();

        when(service.listPendingByRace(77L)).thenReturn(List.of(r1));

        mockMvc.perform(get("/api/admin/registrations/pending/by-race/{raceId}", 77))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].raceId").value(77));

        verify(service).listPendingByRace(77L);
    }

    @Test
    @DisplayName("GET /pending/by-race/{raceId} -> 200 y lista vacía")
    void pendingByRace_empty() throws Exception {
        when(service.listPendingByRace(999L)).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/registrations/pending/by-race/{raceId}", 999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /pending/by-race/{raceId} -> servicio lanza excepción -> 500")
    void pendingByRace_error500() throws Exception {
        when(service.listPendingByRace(13L)).thenThrow(new RuntimeException("boom race"));

        mockMvc.perform(get("/api/admin/registrations/pending/by-race/{raceId}", 13))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom race")));
    }

    // ---------- cancelByOrganizer ----------

    @Test
    @DisplayName("PUT /cancel/by-organizer/{uid} -> 200 y devuelve número de cancelaciones")
    void cancelByOrganizer_ok() throws Exception {
        when(service.cancelAllPendingByOrganizer("org-2")).thenReturn(3);

        mockMvc.perform(put("/api/admin/registrations/cancel/by-organizer/{uid}", "org-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));

        verify(service).cancelAllPendingByOrganizer("org-2");
    }

    @Test
    @DisplayName("PUT /cancel/by-organizer/{uid} -> servicio lanza excepción -> 500")
    void cancelByOrganizer_error500() throws Exception {
        when(service.cancelAllPendingByOrganizer("bad")).thenThrow(new RuntimeException("boom cancel org"));

        mockMvc.perform(put("/api/admin/registrations/cancel/by-organizer/{uid}", "bad"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom cancel org")));
    }

    // ---------- cancelByRace ----------

    @Test
    @DisplayName("PUT /cancel/by-race/{raceId} -> 200 y devuelve número de cancelaciones")
    void cancelByRace_ok() throws Exception {
        when(service.cancelAllPendingByRace(55L)).thenReturn(7);

        mockMvc.perform(put("/api/admin/registrations/cancel/by-race/{raceId}", 55))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(7));

        verify(service).cancelAllPendingByRace(55L);
    }

    @Test
    @DisplayName("PUT /cancel/by-race/{raceId} -> servicio lanza excepción -> 500")
    void cancelByRace_error500() throws Exception {
        when(service.cancelAllPendingByRace(66L)).thenThrow(new RuntimeException("boom cancel race"));

        mockMvc.perform(put("/api/admin/registrations/cancel/by-race/{raceId}", 66))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom cancel race")));
    }
}
