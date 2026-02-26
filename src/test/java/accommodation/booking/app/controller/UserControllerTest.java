package accommodation.booking.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import accommodation.booking.app.dto.user.UserResponseDto;
import accommodation.booking.app.dto.user.UserUpdateRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "james@google.com", roles = "CUSTOMER")
    @Sql(scripts = "classpath:database/users/restore-updated-user-role-id3.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateUserRole_Success() throws Exception {
        Long userId = 3L;

        mockMvc.perform(put("/users/{id}/role", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.role").value("ADMIN")
                );
    }

    @Test
    @WithMockUser(username = "james@google.com", roles = "CUSTOMER")
    void getLoggedInUserInfo_Success() throws Exception {

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("james@google.com"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.role").value("CUSTOMER")
                );
    }

    @Test
    void getLoggedInUserInfo_Unauthenticated_ErrorStatus() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "james@google.com", roles = "CUSTOMER")
    @Sql(scripts = "classpath:database/users/restore-updated-user-data-id3.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateLoggedInUserInfo_Success() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto();
        request.setEmail("jamie@google.com").setFirstName("Jamie").setLastName("Test")
                .setPassword("password").setRepeatedPassword("password");

        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(patch("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertEquals(request.getEmail(), actual.getEmail());
        assertEquals(request.getFirstName(), actual.getFirstName());
    }

    @Test
    @WithMockUser(username = "james@google.com", roles = "CUSTOMER")
    void updateLoggedInUserInfo_WithWrongRepeatedPassword_ErrorStatus() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto();
        request.setEmail("jamie@google.com").setFirstName("Jamie").setLastName("Test")
                .setPassword("test1").setRepeatedPassword("test2");

        String jsonRequest = objectMapper.writeValueAsString(request);

        mockMvc.perform(patch("/users/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }
}