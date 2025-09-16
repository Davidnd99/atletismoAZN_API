package com.running.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.running.endpoint.api.RaceController;
import com.running.model.*;
import com.running.service.DifficultyService;
import com.running.service.ParticipantService;
import com.running.service.RaceService;
import com.running.service.TypeService;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RaceControllerTestApi {

    @Mock RaceService raceService;
    @Mock DifficultyService difficultyService;
    @Mock TypeService typeService;
    @Mock ParticipantService participantService;

    @InjectMocks RaceController controller;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new TestGlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    // Advice de prueba para mapear excepciones a HTTP
    @RestControllerAdvice
    static class TestGlobalExceptionHandler {
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<String> handleRSE(ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // ---------- POST /save ----------

    @Test
    @DisplayName("POST /api/races/save -> 200 crea y devuelve carrera")
    void addRace_ok() throws Exception {
        var saved = Race.builder().id(101L).name("Nueva").build();
        when(raceService.save(any(RaceDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/races/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.name").value("Nueva"));

        verify(raceService).save(any(RaceDto.class));
    }

    @Test
    @DisplayName("POST /api/races/save -> servicio lanza excepción -> 500")
    void addRace_error500() throws Exception {
        when(raceService.save(any(RaceDto.class))).thenThrow(new RuntimeException("Type not found"));
        mockMvc.perform(post("/api/races/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Type not found")));
    }

    // ---------- GET /getById ----------

    @Test
    @DisplayName("GET /api/races/getById?id=7 -> 200 y devuelve carrera")
    void getById_ok() throws Exception {
        var r = Race.builder().id(7L).name("Una").build();
        when(raceService.findById(7L)).thenReturn(r);

        mockMvc.perform(get("/api/races/getById").param("id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Una"));

        verify(raceService).findById(7L);
    }

    @Test
    @DisplayName("GET /api/races/getById -> servicio lanza excepción -> 500")
    void getById_error500() throws Exception {
        when(raceService.findById(9L)).thenThrow(new RuntimeException("Race not found with id: 9"));

        mockMvc.perform(get("/api/races/getById").param("id", "9"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Race not found")));
    }

    // ---------- GET /getAll ----------

    @Test
    @DisplayName("GET /api/races/getAll -> 200 y lista")
    void getAll_ok() throws Exception {
        var a = Race.builder().id(1L).name("A").build();
        var b = Race.builder().id(2L).name("B").build();
        when(raceService.findAll()).thenReturn(List.of(a, b));

        mockMvc.perform(get("/api/races/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/races/getAll -> 200 y lista vacía")
    void getAll_empty() throws Exception {
        when(raceService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/races/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- GET /filter ----------

    @Test
    @DisplayName("GET /api/races/filter con fechas válidas -> 200")
    void filter_ok() throws Exception {
        when(raceService.filterRaces(any(), any(), any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/races/filter")
                        .param("province", "Sevilla")
                        .param("fechaDesde", "2025-01-01")
                        .param("fechaHasta", "2025-12-31")
                        .param("typeId", "1")
                        .param("difficultyId", "2")
                        .param("finalizada", "false"))
                .andExpect(status().isOk());

        verify(raceService).filterRaces(eq("Sevilla"), any(), any(), eq(1L), eq(2L), eq(false));
    }

    @Test
    @DisplayName("GET /api/races/filter -> 400 formato fechaDesde inválido")
    void filter_badDateStart() throws Exception {
        mockMvc.perform(get("/api/races/filter")
                        .param("fechaDesde", "2025-01-01 25:00:00")) // invalida HH=25, y NO es longitud 10
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("fechaDesde")));
    }


    @Test
    @DisplayName("GET /api/races/filter -> 400 fechaDesde > fechaHasta")
    void filter_fromAfterTo() throws Exception {
        mockMvc.perform(get("/api/races/filter")
                        .param("fechaDesde", "2025-12-31")
                        .param("fechaHasta", "2025-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("fechaDesde no puede ser posterior")));
    }

    // ---------- GET /getByProvince ----------

    @Test
    @DisplayName("GET /api/races/getByProvince?province=Sevilla -> 200")
    void getByProvince_ok() throws Exception {
        when(raceService.findByProvince("Sevilla")).thenReturn(List.of());
        mockMvc.perform(get("/api/races/getByProvince").param("province", "Sevilla"))
                .andExpect(status().isOk());
        verify(raceService).findByProvince("Sevilla");
    }

    // ---------- GET /getByType ----------

    @Test
    @DisplayName("GET /api/races/getByType?typeId=9 -> 200")
    void getByType_ok() throws Exception {
        var type = mock(Type.class);
        when(typeService.findById(9L)).thenReturn(Optional.of(type));
        when(raceService.findByType(type)).thenReturn(List.of());

        mockMvc.perform(get("/api/races/getByType").param("typeId", "9"))
                .andExpect(status().isOk());

        verify(typeService).findById(9L);
        verify(raceService).findByType(type);
    }

    @Test
    @DisplayName("GET /api/races/getByType -> 500 si type no existe")
    void getByType_notFoundType() throws Exception {
        when(typeService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/races/getByType").param("typeId", "99"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Type not found with id: 99")));
    }

    // ---------- GET /getByDifficulty ----------

    @Test
    @DisplayName("GET /api/races/getByDifficulty?difficultyId=3 -> 200")
    void getByDifficulty_ok() throws Exception {
        var diff = mock(Difficulty.class);
        when(difficultyService.findById(3L)).thenReturn(Optional.of(diff));
        when(raceService.findByDifficulty(diff)).thenReturn(List.of());

        mockMvc.perform(get("/api/races/getByDifficulty").param("difficultyId", "3"))
                .andExpect(status().isOk());

        verify(difficultyService).findById(3L);
        verify(raceService).findByDifficulty(diff);
    }

    @Test
    @DisplayName("GET /api/races/getByDifficulty -> 500 si difficulty no existe")
    void getByDifficulty_notFoundDiff() throws Exception {
        when(difficultyService.findById(77L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/races/getByDifficulty").param("difficultyId", "77"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Difficulty not found with id: 77")));
    }

    // ---------- GET /getByOrganizer ----------

    @Test
    @DisplayName("GET /api/races/getByOrganizer?organizerUserId=5 -> 200")
    void getByOrganizer_ok() throws Exception {
        when(raceService.findByOrganizer(5L)).thenReturn(List.of());
        mockMvc.perform(get("/api/races/getByOrganizer").param("organizerUserId", "5"))
                .andExpect(status().isOk());
        verify(raceService).findByOrganizer(5L);
    }

    // ---------- GET /{id}/organizer ----------

    @Test
    @DisplayName("GET /api/races/{id}/organizer -> 200 y devuelve organizer")
    void getOrganizer_ok() throws Exception {
        var org = new OrganizerDto("uid-x", "X", "x@x.com");
        when(raceService.getOrganizerOfRace(10L)).thenReturn(org);

        mockMvc.perform(get("/api/races/{id}/organizer", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("uid-x"))
                .andExpect(jsonPath("$.email").value("x@x.com"));

        verify(raceService).getOrganizerOfRace(10L);
    }

    @Test
    @DisplayName("GET /api/races/{id}/organizer -> 200 y body vacío si no hay organizer")
    void getOrganizer_null_ok() throws Exception {
        when(raceService.getOrganizerOfRace(11L)).thenReturn(null);

        mockMvc.perform(get("/api/races/{id}/organizer", 11))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("GET /api/races/{id}/organizer -> servicio lanza excepción -> 500")
    void getOrganizer_error500() throws Exception {
        when(raceService.getOrganizerOfRace(12L)).thenThrow(new RuntimeException("Race not found"));
        mockMvc.perform(get("/api/races/{id}/organizer", 12))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Race not found")));
    }

    // ---------- PUT /{id}/organizer ----------

    @Test
    @DisplayName("PUT /api/races/{id}/organizer -> 200 y devuelve organizer actualizado")
    void updateOrganizer_ok() throws Exception {
        var org = new OrganizerDto("uid-m", "Mar", "m@m.com");
        when(raceService.updateRaceOrganizer(20L, "m@m.com")).thenReturn(org);

        mockMvc.perform(put("/api/races/{id}/organizer", 20)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"m@m.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("uid-m"))
                .andExpect(jsonPath("$.email").value("m@m.com"));

        verify(raceService).updateRaceOrganizer(20L, "m@m.com");
    }

    @Test
    @DisplayName("PUT /api/races/{id}/organizer -> servicio lanza excepción -> 500")
    void updateOrganizer_error500() throws Exception {
        when(raceService.updateRaceOrganizer(21L, "bad@bad.com"))
                .thenThrow(new RuntimeException("El usuario debe ser 'organizator' o 'admin'"));

        mockMvc.perform(put("/api/races/{id}/organizer", 21)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bad@bad.com\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("organizator")));
    }

    // ---------- GET /{raceId}/participants ----------

    @Test
    @DisplayName("GET /api/races/{raceId}/participants?uid=u1 -> 200 y lista (puede ser vacía)")
    void listParticipants_ok() throws Exception {
        when(participantService.listParticipants("u1", 33L, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/races/{raceId}/participants", 33)
                        .param("uid", "u1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(participantService).listParticipants("u1", 33L, null);
    }

    @Test
    @DisplayName("GET /api/races/{raceId}/participants -> 404 cuando service lanza NOT_FOUND")
    void listParticipants_notFound() throws Exception {
        when(participantService.listParticipants("u404", 44L, null))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found by uid"));

        mockMvc.perform(get("/api/races/{raceId}/participants", 44)
                        .param("uid", "u404"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));
    }

    @Test
    @DisplayName("GET /api/races/{raceId}/participants -> 403 cuando service lanza FORBIDDEN")
    void listParticipants_forbidden() throws Exception {
        when(participantService.listParticipants("uX", 55L, "pendiente"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes ver los inscritos de esta carrera"));

        mockMvc.perform(get("/api/races/{raceId}/participants", 55)
                        .param("uid", "uX")
                        .param("status", "pendiente"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("No puedes ver los inscritos")));
    }
}
