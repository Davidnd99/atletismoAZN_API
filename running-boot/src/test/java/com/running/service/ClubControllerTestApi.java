package com.running.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.running.endpoint.api.ClubController;
import com.running.model.AdminClubDto;
import com.running.model.ClubDto;
import com.running.model.UserDto;
import com.running.service.ClubService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@ExtendWith(MockitoExtension.class)
public class ClubControllerTestApi {

    @Mock
    ClubService clubService;

    @InjectMocks
    ClubController controller;

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

    // ---------- GET /api/clubs/all ----------

    @Test
    @DisplayName("GET /api/clubs/all?provincia=Sevilla -> 200 y lista con elementos")
    void getAllClubs_withProvincia_ok() throws Exception {
        var c1 = ClubDto.builder().id(1L).name("Club A").province("Sevilla").place("Sevilla").members(10).photo("a.jpg").contact("a@a.com").joined(false).build();
        var c2 = ClubDto.builder().id(2L).name("Club B").province("Sevilla").place("Dos Hermanas").members(20).photo("b.jpg").contact("b@b.com").joined(false).build();
        when(clubService.getAllClubs("Sevilla")).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/clubs/all").param("provincia", "Sevilla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(clubService).getAllClubs("Sevilla");
    }

    @Test
    @DisplayName("GET /api/clubs/all (sin provincia) -> 200 y lista vacía")
    void getAllClubs_noProvincia_empty() throws Exception {
        when(clubService.getAllClubs(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/clubs/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(clubService).getAllClubs(null);
    }

    @Test
    @DisplayName("GET /api/clubs/all -> servicio lanza excepción -> 500")
    void getAllClubs_error500() throws Exception {
        when(clubService.getAllClubs("Cadiz")).thenThrow(new RuntimeException("boom all"));
        mockMvc.perform(get("/api/clubs/all").param("provincia", "Cadiz"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom all")));
    }

    // ---------- GET /api/clubs/user?uid=... ----------

    @Test
    @DisplayName("GET /api/clubs/user?uid=u1 -> 200 y lista con elementos")
    void getClubsByUser_ok() throws Exception {
        var c1 = ClubDto.builder().id(3L).name("Club X").province("Huelva").joined(true).build();
        var c2 = ClubDto.builder().id(4L).name("Club Y").province("Huelva").joined(true).build();
        when(clubService.getClubsByUser("u1")).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/clubs/user").param("uid", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[1].id").value(4));

        verify(clubService).getClubsByUser("u1");
    }

    @Test
    @DisplayName("GET /api/clubs/user -> 200 y lista vacía")
    void getClubsByUser_empty() throws Exception {
        when(clubService.getClubsByUser("empty")).thenReturn(List.of());

        mockMvc.perform(get("/api/clubs/user").param("uid", "empty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/clubs/user -> servicio lanza excepción -> 500")
    void getClubsByUser_error500() throws Exception {
        when(clubService.getClubsByUser("boom")).thenThrow(new RuntimeException("boom user"));
        mockMvc.perform(get("/api/clubs/user").param("uid", "boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("boom user")));
    }

    // ---------- GET /api/clubs/{id} (con/sin uid) ----------

    @Test
    @DisplayName("GET /api/clubs/10?uid=u1 -> 200 y club")
    void getClubById_withUid_ok() throws Exception {
        var club = ClubDto.builder().id(10L).name("Club 10").province("Córdoba").joined(true).build();
        when(clubService.getClubById(10L, "u1")).thenReturn(club);

        mockMvc.perform(get("/api/clubs/{id}", 10).param("uid", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Club 10"));

        verify(clubService).getClubById(10L, "u1");
    }

    @Test
    @DisplayName("GET /api/clubs/11 (sin uid) -> 200 y club")
    void getClubById_noUid_ok() throws Exception {
        var club = ClubDto.builder().id(11L).name("Club 11").province("Málaga").joined(false).build();
        when(clubService.getClubById(11L, null)).thenReturn(club);

        mockMvc.perform(get("/api/clubs/{id}", 11))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.name").value("Club 11"));

        verify(clubService).getClubById(11L, null);
    }

    @Test
    @DisplayName("GET /api/clubs/{id} -> servicio lanza excepción -> 500")
    void getClubById_error500() throws Exception {
        when(clubService.getClubById(12L, "u2")).thenThrow(new RuntimeException("Club no encontrado"));
        mockMvc.perform(get("/api/clubs/{id}", 12).param("uid", "u2"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Club no encontrado")));
    }

    // ---------- GET /api/clubs/{id}/members ----------

    @Test
    @DisplayName("GET /api/clubs/20/members -> 200 y miembros")
    void getClubMembers_ok() throws Exception {
        var u1 = new UserDto(); u1.setUid("a"); u1.setName("Ana"); u1.setEmail("ana@a.com"); u1.setSurname("A"); u1.setRole("runner");
        var u2 = new UserDto(); u2.setUid("b"); u2.setName("Ben"); u2.setEmail("ben@b.com"); u2.setSurname("B"); u2.setRole("runner");
        when(clubService.getUsersByClub(20L)).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/clubs/{id}/members", 20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("ana@a.com"))
                .andExpect(jsonPath("$[1].email").value("ben@b.com"));

        verify(clubService).getUsersByClub(20L);
    }

    @Test
    @DisplayName("GET /api/clubs/{id}/members -> servicio lanza excepción -> 500")
    void getClubMembers_error500() throws Exception {
        when(clubService.getUsersByClub(21L)).thenThrow(new RuntimeException("Club no encontrado"));
        mockMvc.perform(get("/api/clubs/{id}/members", 21))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Club no encontrado")));
    }

    // ---------- GET /api/clubs/{id}/AdminClub ----------

    @Test
    @DisplayName("GET /api/clubs/{id}/AdminClub -> 200 y devuelve manager")
    void getClubManager_ok() throws Exception {
        var m = new AdminClubDto("uid-x", "Xavier", "x@x.com");
        when(clubService.getManagerOfClub(30L)).thenReturn(m);

        mockMvc.perform(get("/api/clubs/{id}/AdminClub", 30))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("uid-x"))
                .andExpect(jsonPath("$.email").value("x@x.com"));

        verify(clubService).getManagerOfClub(30L);
    }

    @Test
    @DisplayName("GET /api/clubs/{id}/AdminClub -> 200 y body vacío si no hay manager")
    void getClubManager_null_ok() throws Exception {
        when(clubService.getManagerOfClub(31L)).thenReturn(null);

        mockMvc.perform(get("/api/clubs/{id}/AdminClub", 31))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // body vacío
    }

    @Test
    @DisplayName("GET /api/clubs/{id}/AdminClub -> servicio lanza excepción -> 500")
    void getClubManager_error500() throws Exception {
        when(clubService.getManagerOfClub(32L)).thenThrow(new RuntimeException("Club no encontrado"));
        mockMvc.perform(get("/api/clubs/{id}/AdminClub", 32))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Club no encontrado")));
    }

    // ---------- PUT /api/clubs/{id} ----------

    @Test
    @DisplayName("PUT /api/clubs/{id} -> 200 y devuelve club actualizado")
    void updateClub_ok() throws Exception {
        var req = ClubDto.builder().name("Nuevo Nombre").province("Sevilla").place("Sevilla").photo("n.jpg").contact("c@c.com").members(99).build();
        var saved = ClubDto.builder().id(40L).name("Nuevo Nombre").province("Sevilla").place("Sevilla").photo("n.jpg").contact("c@c.com").members(99).joined(false).build();

        when(clubService.updateClub(eq(40L), any(ClubDto.class))).thenReturn(saved);

        mockMvc.perform(put("/api/clubs/{id}", 40)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(40))
                .andExpect(jsonPath("$.name").value("Nuevo Nombre"));

        verify(clubService).updateClub(eq(40L), any(ClubDto.class));
    }

    @Test
    @DisplayName("PUT /api/clubs/{id} -> servicio lanza excepción -> 500")
    void updateClub_error500() throws Exception {
        var req = ClubDto.builder().name("X").build();
        when(clubService.updateClub(eq(41L), any(ClubDto.class))).thenThrow(new RuntimeException("Club no encontrado"));

        mockMvc.perform(put("/api/clubs/{id}", 41)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Club no encontrado")));
    }

    // ---------- PUT /api/clubs/{id}/manager ----------

    @Test
    @DisplayName("PUT /api/clubs/{id}/manager -> 200 y devuelve admin club")
    void updateClubManager_ok() throws Exception {
        var resp = new AdminClubDto("uid-m", "Mario", "m@m.com");
        when(clubService.updateClubManager(50L, "m@m.com")).thenReturn(resp);

        mockMvc.perform(put("/api/clubs/{id}/manager", 50)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"m@m.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("uid-m"))
                .andExpect(jsonPath("$.email").value("m@m.com"));

        verify(clubService).updateClubManager(50L, "m@m.com");
    }

    @Test
    @DisplayName("PUT /api/clubs/{id}/manager -> servicio lanza excepción -> 500")
    void updateClubManager_error500() throws Exception {
        when(clubService.updateClubManager(51L, "bad@bad.com"))
                .thenThrow(new RuntimeException("El usuario debe ser 'club-administrator' o 'admin'"));

        mockMvc.perform(put("/api/clubs/{id}/manager", 51)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bad@bad.com\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("club-administrator")));
    }

    // ---------- DELETE (no existe en este controller) ----------
    // (este controlador no expone DELETE; lo menciono para claridad)
}
