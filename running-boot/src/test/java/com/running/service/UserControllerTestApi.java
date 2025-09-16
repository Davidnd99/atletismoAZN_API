package com.running.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.running.endpoint.api.UserController;
import com.running.model.RoleDto;
import com.running.model.User;
import com.running.model.UserDto;
import com.running.service.UserService;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTestApi {

    @Mock UserService userService;
    @InjectMocks UserController controller;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new TestAdvice())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    // Advice de prueba
    @RestControllerAdvice
    static class TestAdvice {
        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<String> handleRSE(ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<String> handleRuntime(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    // ---------- PUT /{uid}/join-club/{clubId} ----------

    @Test
    @DisplayName("PUT /api/user/{uid}/join-club/{clubId} -> 200 y mensaje OK")
    void joinClub_ok() throws Exception {
        mockMvc.perform(put("/api/user/{uid}/join-club/{clubId}", "u1", 10))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("joined")));

        verify(userService).joinClub("u1", 10L);
    }

    @Test
    @DisplayName("PUT /api/user/{uid}/join-club/{clubId} -> 500 si falla servicio")
    void joinClub_error500() throws Exception {
        doThrow(new RuntimeException("Club not found")).when(userService).joinClub("u1", 99L);

        mockMvc.perform(put("/api/user/{uid}/join-club/{clubId}", "u1", 99))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Club not found")));
    }

    // ---------- PUT /{uid}/leave-club/{clubId} ----------

    @Test
    @DisplayName("PUT /api/user/{uid}/leave-club/{clubId} -> 200 y mensaje OK")
    void leaveClub_ok() throws Exception {
        mockMvc.perform(put("/api/user/{uid}/leave-club/{clubId}", "u1", 10))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("left")));

        verify(userService).leaveClub("u1", 10L);
    }

    @Test
    @DisplayName("PUT /api/user/{uid}/leave-club/{clubId} -> 500 si falla servicio")
    void leaveClub_error500() throws Exception {
        doThrow(new RuntimeException("Usuario no encontrado")).when(userService).leaveClub("uX", 5L);

        mockMvc.perform(put("/api/user/{uid}/leave-club/{clubId}", "uX", 5))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Usuario no encontrado")));
    }

    // ---------- POST /save ----------

    @Test
    @DisplayName("POST /api/user/save -> 200 y devuelve User")
    void save_ok() throws Exception {
        UserDto in = new UserDto(); in.setEmail("a@a.com"); in.setName("Ana"); in.setSurname("S");
        User out = new User(); out.setEmail("a@a.com"); out.setName("Ana"); out.setSurname("S");

        when(userService.saveFromDto(any(UserDto.class))).thenReturn(out);

        mockMvc.perform(post("/api/user/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@a.com"))
                .andExpect(jsonPath("$.name").value("Ana"));

        verify(userService).saveFromDto(any(UserDto.class));
    }

    @Test
    @DisplayName("POST /api/user/save -> 500 si falla servicio")
    void save_error500() throws Exception {
        when(userService.saveFromDto(any(UserDto.class))).thenThrow(new RuntimeException("Default club not found"));

        mockMvc.perform(post("/api/user/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@x.com\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Default club not found")));
    }

    // ---------- GET /findByUID/{uid} ----------

    @Test
    @DisplayName("GET /api/user/findByUID/{uid} -> 200 y devuelve User")
    void findByUID_ok() throws Exception {
        User u = new User(); u.setUID("u1"); u.setEmail("e@e.com"); u.setName("Eva");
        when(userService.findByUID("u1")).thenReturn(u);

        mockMvc.perform(get("/api/user/findByUID/{uid}", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("u1"))
                .andExpect(jsonPath("$.email").value("e@e.com"));

        verify(userService).findByUID("u1");
    }

    @Test
    @DisplayName("GET /api/user/findByUID/{uid} -> 500 si no existe")
    void findByUID_error500() throws Exception {
        when(userService.findByUID("u404")).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/user/findByUID/{uid}", "u404"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("User not found")));
    }

    // ---------- GET /findAll ----------

    @Test
    @DisplayName("GET /api/user/findAll -> 200 y lista")
    void findAll_ok() throws Exception {
        var u1 = new User(); u1.setEmail("a@a.com");
        var u2 = new User(); u2.setEmail("b@b.com");
        when(userService.findAll()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/user/findAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(userService).findAll();
    }

    // ---------- DELETE /delete/{uid} ----------

    @Test
    @DisplayName("DELETE /api/user/delete/{uid} -> 200 y mensaje OK")
    void deleteUser_ok() throws Exception {
        mockMvc.perform(delete("/api/user/delete/{uid}", "u1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("deleted")));

        verify(userService).deleteByUID("u1");
    }

    @Test
    @DisplayName("DELETE /api/user/delete/{uid} -> 500 si falla servicio")
    void deleteUser_error500() throws Exception {
        doThrow(new RuntimeException("User not found with UID: uX"))
                .when(userService).deleteByUID("uX");

        mockMvc.perform(delete("/api/user/delete/{uid}", "uX"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("User not found")));
    }

    // ---------- GET /role/{uid} ----------

    @Test
    @DisplayName("GET /api/user/role/{uid} -> 200 y devuelve RoleDto")
    void getUserRole_ok() throws Exception {
        RoleDto role = new RoleDto(); role.setName("admin");
        when(userService.getUserRoleByUID("u1")).thenReturn(role);

        mockMvc.perform(get("/api/user/role/{uid}", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("admin"));

        verify(userService).getUserRoleByUID("u1");
    }

    @Test
    @DisplayName("GET /api/user/role/{uid} -> 500 si falla servicio")
    void getUserRole_error500() throws Exception {
        when(userService.getUserRoleByUID("uX")).thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(get("/api/user/role/{uid}", "uX"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Usuario no encontrado")));
    }

    // ---------- PUT /update/{uid} ----------

    @Test
    @DisplayName("PUT /api/user/update/{uid} -> 200 y devuelve User actualizado")
    void updateNameAndSurname_ok() throws Exception {
        UserDto dto = new UserDto(); dto.setName("Pepe"); dto.setSurname("García");
        User updated = new User(); updated.setName("Pepe"); updated.setSurname("García");

        when(userService.updateNameAndSurname(eq("u1"), any(UserDto.class))).thenReturn(updated);

        mockMvc.perform(put("/api/user/update/{uid}", "u1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pepe"))
                .andExpect(jsonPath("$.surname").value("García"));

        verify(userService).updateNameAndSurname(eq("u1"), any(UserDto.class));
    }

    @Test
    @DisplayName("PUT /api/user/update/{uid} -> 500 si falla servicio")
    void updateNameAndSurname_error500() throws Exception {
        when(userService.updateNameAndSurname(eq("uX"), any(UserDto.class)))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(put("/api/user/update/{uid}", "uX")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Usuario no encontrado")));
    }

    // ---------- GET /roles/names ----------

    @Test
    @DisplayName("GET /api/user/roles/names -> 200 y lista de nombres")
    void getAllRoleNames_ok() throws Exception {
        when(userService.getAllRoleNames()).thenReturn(List.of("admin", "user"));

        mockMvc.perform(get("/api/user/roles/names"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("admin"))
                .andExpect(jsonPath("$[1]").value("user"));

        verify(userService).getAllRoleNames();
    }

    // ---------- POST /save-admin ----------

    @Test
    @DisplayName("POST /api/user/save-admin -> 200 y devuelve User")
    void saveUserFromAdmin_ok() throws Exception {
        UserDto dto = new UserDto(); dto.setEmail("a@a.com"); dto.setRole("user");
        User out = new User(); out.setEmail("a@a.com");

        when(userService.saveFromAdminDto(any(UserDto.class))).thenReturn(out);

        mockMvc.perform(post("/api/user/save-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("a@a.com"));

        verify(userService).saveFromAdminDto(any(UserDto.class));
    }

    @Test
    @DisplayName("POST /api/user/save-admin -> 500 si falla servicio")
    void saveUserFromAdmin_error500() throws Exception {
        when(userService.saveFromAdminDto(any(UserDto.class)))
                .thenThrow(new RuntimeException("Rol no encontrado"));

        mockMvc.perform(post("/api/user/save-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@x.com\",\"role\":\"bad\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Rol no encontrado")));
    }

    // ---------- POST /admin-create ----------

    @Test
    @DisplayName("POST /api/user/admin-create -> 200 y devuelve User")
    void adminCreate_ok() throws Exception {
        UserDto dto = new UserDto(); dto.setEmail("b@b.com"); dto.setPassword("123456");
        User out = new User(); out.setEmail("b@b.com");

        when(userService.createUserWithFirebase(any(UserDto.class))).thenReturn(out);

        mockMvc.perform(post("/api/user/admin-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("b@b.com"));

        verify(userService).createUserWithFirebase(any(UserDto.class));
    }

    @Test
    @DisplayName("POST /api/user/admin-create -> 500 si falla servicio")
    void adminCreate_error500() throws Exception {
        when(userService.createUserWithFirebase(any(UserDto.class)))
                .thenThrow(new RuntimeException("Error creando usuario en Firebase: x"));

        mockMvc.perform(post("/api/user/admin-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"bad@bad.com\",\"password\":\"123\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Firebase")));
    }

    // ---------- DELETE /admin-delete/{uid}?actingUid=... ----------

    @Test
    @DisplayName("DELETE /api/user/admin-delete/{uid}?actingUid=admin-1 -> 200 y mensaje OK")
    void adminDelete_ok() throws Exception {
        mockMvc.perform(delete("/api/user/admin-delete/{uid}", "uDel")
                        .param("actingUid", "admin-1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("reassigned")));

        verify(userService).adminDeleteWithFirebase("uDel", "admin-1");
    }

    @Test
    @DisplayName("DELETE /api/user/admin-delete/{uid} -> 500 si falla servicio")
    void adminDelete_error500() throws Exception {
        doThrow(new RuntimeException("Only ADMIN can perform this action"))
                .when(userService).adminDeleteWithFirebase("u2", "user-x");

        mockMvc.perform(delete("/api/user/admin-delete/{uid}", "u2")
                        .param("actingUid", "user-x"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Only ADMIN")));
    }

    // ---------- GET /by-email?email=... ----------

    @Test
    @DisplayName("GET /api/user/by-email?email=x -> 200 y devuelve UserDto")
    void getByEmail_ok() throws Exception {
        UserDto dto = new UserDto(); dto.setEmail("c@c.com"); dto.setName("Carla");
        when(userService.findByEmailDto("c@c.com")).thenReturn(dto);

        mockMvc.perform(get("/api/user/by-email").param("email", "c@c.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("c@c.com"))
                .andExpect(jsonPath("$.name").value("Carla"));

        verify(userService).findByEmailDto("c@c.com");
    }

    @Test
    @DisplayName("GET /api/user/by-email -> 500 si no existe")
    void getByEmail_error500() throws Exception {
        when(userService.findByEmailDto("none@x.com"))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(get("/api/user/by-email").param("email", "none@x.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Usuario no encontrado")));
    }
}
