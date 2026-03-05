package accommodation.booking.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import accommodation.booking.app.dto.accommodation.AccommodationDto;
import accommodation.booking.app.dto.accommodation.AccommodationUpdateRequestDto;
import accommodation.booking.app.dto.accommodation.CreateAccommodationRequestDto;
import accommodation.booking.app.model.Type;
import accommodation.booking.app.notification.telegram.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AccommodationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void getAccommodations_PublicEndpoint_Success() throws Exception {

        mockMvc.perform(get("/accommodations"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createAccommodation_AsCustomer_ErrorStatus() throws Exception {

        CreateAccommodationRequestDto request = new CreateAccommodationRequestDto(
                Type.APARTMENT, 1L, "Studio", List.of(1L, 3L), BigDecimal.valueOf(10), 1);

        mockMvc.perform(post("/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = "classpath:database/accommodation/delete-added-accommodation.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createAccommodation_AsAdmin_Success() throws Exception {
        CreateAccommodationRequestDto request = new CreateAccommodationRequestDto(
                Type.VACATION_HOME, 3L, "333 m 2", List.of(1L, 3L), BigDecimal.valueOf(1000), 1);

        MvcResult result = mockMvc.perform(post("/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        AccommodationDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                AccommodationDto.class);

        assertNotNull(actual);
        assertNotNull(actual.id());
        assertEquals(request.locationId(), actual.locationId());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAccommodationById_Success() throws Exception {
        mockMvc.perform(get("/accommodations/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = "classpath:database/accommodation/restore-updated-accommodation-id2.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateAccommodation_Success() throws Exception {

        AccommodationUpdateRequestDto request = new AccommodationUpdateRequestDto(
                List.of(1L, 2L, 3L), new BigDecimal("22.22"), 1);

        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(patch("/accommodations/{id}", 2L)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        AccommodationDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccommodationDto.class);

        assertNotNull(actual);
        assertNotNull(actual.id());
        assertNotNull(actual.locationId());
        assertEquals(2L, actual.id());
        assertEquals(request.amenityIds(), actual.amenityIds());
        assertEquals(request.dailyRate(), actual.dailyRate());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = "classpath:database/accommodation/restore-deleted-accommodation-id1.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteAccommodation_Success() throws Exception {
        mockMvc.perform(delete("/accommodations/{id}", 1L))
                .andExpect(status().isOk());
    }
}
