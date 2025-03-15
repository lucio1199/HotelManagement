package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.config.TestSecurityConfig;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.KeyStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.service.KeyService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Slf4j
@Import(TestSecurityConfig.class)
class DigitalKeyEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeyService keyService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "GUEST")
    void givenValidId_whenUnlock_thenReturnsOk() throws Exception {
        Long keyId = 1L;
        String email = "test@example.com";

        when(userService.getLoggedInUserEmail()).thenReturn(email);

        doNothing().when(keyService).unlock(keyId, email);

        mockMvc.perform(post("/api/v1/key/unlock/{id}", keyId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "GUEST")
    void givenValidId_whenGetStatus_thenReturnsKeyStatus() throws Exception {
        Long keyId = 1L;
        String email = "test@example.com";
        KeyStatusDto keyStatusDto = new KeyStatusDto(1L, 100L, "unlock");

        when(userService.getLoggedInUserEmail()).thenReturn(email);
        when(keyService.getStatus(keyId, email)).thenReturn(keyStatusDto);

        mockMvc.perform(get("/api/v1/key/{id}", keyId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roomId").value(1L))
            .andExpect(jsonPath("$.smartLockId").value(100L))
            .andExpect(jsonPath("$.status").value("unlock"));
    }
}

