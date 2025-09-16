package com.running.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.running.endpoint.api.OrganizerController;
import com.running.model.Race;
import com.running.model.RaceDto;
import com.running.service.OrganizerService;
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

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class OrganizerControllerTestApi {

    @Mock
    OrganizerService service;

    @InjectMocks
    OrganizerController controller;

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

    @RestControllerAdvice
    static class TestGlobalExceptionHandler {
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // ---------- GET /{uid}/races ----------

    @Test
    @DisplayName("GET /api/organizer/{uid}/races -> 200 y lista con elementos")
    void myRaces_ok() throws Exception {
        var r1 = Race.builder().id(1L).name("10K").build();
        var r2 = Race.builder().id(2L).name("Media").build();
        when(service.listMyRaces("org-1")).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/organizer/{uid}/races", "org-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(service).listMyRaces("org-1");
    }

    @Test
    @DisplayName("GET /api/organizer/{uid}/races -> 200 y lista vacía")
    void myRaces_empty() throws Exception {
        when(service.listMyRaces("empty")).thenReturn(List.of());

        mockMvc.perform(get("/api/organizer/{uid}/races", "empty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/organizer/{uid}/races -> servicio lanza excepción -> 500")
    void myRaces_error500() throws Exception {
        when(service.listMyRaces("boom")).thenThrow(new RuntimeException("boom list"));
        mockMvc.perform(get("/api/organizer/{uid}/races", "boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom list")));
    }

    // ---------- POST /{uid}/races ----------

    @Test
    @DisplayName("POST /api/organizer/{uid}/races -> 200 crea y devuelve carrera")
    void create_ok() throws Exception {
        var saved = Race.builder().id(10L).name("Nueva").build();
        when(service.createAsOrganizer(eq("org-1"), any(RaceDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/organizer/{uid}/races", "org-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Nueva"));

        verify(service).createAsOrganizer(eq("org-1"), any(RaceDto.class));
    }

    @Test
    @DisplayName("POST /api/organizer/{uid}/races -> servicio lanza excepción -> 500")
    void create_error500() throws Exception {
        when(service.createAsOrganizer(eq("bad"), any(RaceDto.class)))
                .thenThrow(new RuntimeException("User must be admin or organizator"));

        mockMvc.perform(post("/api/organizer/{uid}/races", "bad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("admin or organizator")));
    }

    // ---------- PUT /{uid}/races/{id} ----------

    @Test
    @DisplayName("PUT /api/organizer/{uid}/races/{id} -> 200 y devuelve carrera actualizada")
    void update_ok() throws Exception {
        var updated = Race.builder().id(5L).name("Editada").build();
        when(service.updateMyRace(eq("org-1"), eq(5L), any(RaceDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/organizer/{uid}/races/{id}", "org-1", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Editada"));

        verify(service).updateMyRace(eq("org-1"), eq(5L), any(RaceDto.class));
    }

    @Test
    @DisplayName("PUT /api/organizer/{uid}/races/{id} -> servicio lanza excepción -> 500")
    void update_error500() throws Exception {
        when(service.updateMyRace(eq("org-2"), eq(9L), any(RaceDto.class)))
                .thenThrow(new RuntimeException("No puedes gestionar esta carrera"));

        mockMvc.perform(put("/api/organizer/{uid}/races/{id}", "org-2", 9)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("No puedes gestionar esta carrera")));
    }

    // ---------- DELETE /{uid}/races/{id} ----------

    @Test
    @DisplayName("DELETE /api/organizer/{uid}/races/{id} -> 204 No Content")
    void delete_ok() throws Exception {
        mockMvc.perform(delete("/api/organizer/{uid}/races/{id}", "org-1", 7))
                .andExpect(status().isNoContent());

        verify(service).deleteMyRace("org-1", 7L);
    }

    @Test
    @DisplayName("DELETE /api/organizer/{uid}/races/{id} -> servicio lanza excepción -> 500")
    void delete_error500() throws Exception {
        doThrow(new RuntimeException("No puedes gestionar esta carrera"))
                .when(service).deleteMyRace("org-3", 8L);

        mockMvc.perform(delete("/api/organizer/{uid}/races/{id}", "org-3", 8))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("No puedes gestionar esta carrera")));
    }

    // ---------- PUT /{uid}/races/{raceId}/registrations/cancel-pending ----------

    @Test
    @DisplayName("PUT /api/organizer/{uid}/races/{raceId}/registrations/cancel-pending?userUid=uX -> 200 mensaje OK")
    void cancelPendingForUser_ok() throws Exception {
        mockMvc.perform(put("/api/organizer/{uid}/races/{raceId}/registrations/cancel-pending", "org-1", 55)
                        .param("userUid", "uX"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("cancelada")));

        verify(service).cancelPendingRegistration("org-1", 55L, "uX");
    }

    @Test
    @DisplayName("PUT /api/organizer/{uid}/races/{raceId}/registrations/cancel-pending -> servicio lanza excepción -> 500")
    void cancelPendingForUser_error500() throws Exception {
        doThrow(new RuntimeException("No autorizado para cancelar inscripciones en esta carrera"))
                .when(service).cancelPendingRegistration("org-2", 66L, "uY");

        mockMvc.perform(put("/api/organizer/{uid}/races/{raceId}/registrations/cancel-pending", "org-2", 66)
                        .param("userUid", "uY"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("No autorizado")));
    }
}
