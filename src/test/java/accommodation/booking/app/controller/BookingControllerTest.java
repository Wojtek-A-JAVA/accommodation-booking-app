package accommodation.booking.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import accommodation.booking.app.dto.booking.BookingDto;
import accommodation.booking.app.dto.booking.BookingUpdateRequestDto;
import accommodation.booking.app.dto.booking.CreateBookingRequestDto;
import accommodation.booking.app.exception.BookingException;
import accommodation.booking.app.notification.telegram.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @MockitoBean
    private NotificationService notificationService;

    @Test
    @WithMockUser(username = "james@google.com", roles = "CUSTOMER")
    @Sql(scripts = "classpath:database/bookings/delete-added-booking.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createBooking_Success() throws Exception {

        CreateBookingRequestDto request = new CreateBookingRequestDto(
                LocalDate.of(2036, 2, 20),
                LocalDate.of(2036, 2, 21),
                1L);

        String jsonRequest = objectMapper.writeValueAsString(request);

        BookingDto expected = new BookingDto(4L,
                LocalDate.of(2036, 2, 20),
                LocalDate.of(2036, 2, 21),
                1L, 3L, "PENDING");

        MvcResult result = mockMvc.perform(
                        post("/bookings")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookingDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto.class
        );

        assertNotNull(actual);
        assertNotNull(actual.id());
        assertEquals(expected.id(), actual.id());
        assertEquals(expected.checkInDate(), actual.checkInDate());
        assertEquals(expected.accommodationId(), actual.accommodationId());
        assertEquals(expected.status(), actual.status());
    }

    @Test
    @WithMockUser(username = "jane@google.pl", roles = "CUSTOMER")
    void createBooking_AsCustomerWithPendingBooking_ErrorStatus() throws Exception {

        CreateBookingRequestDto requestDto = new CreateBookingRequestDto(
                LocalDate.of(2036, 2, 20),
                LocalDate.of(2036, 2, 21),
                1L);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(
                        post("/bookings")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError())
                .andReturn();

        Exception exception = result.getResolvedException();
        assertNotNull(exception);
        assertInstanceOf(BookingException.class, exception);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserBookingList_ByUserIdAndStatus_Success() throws Exception {
        Long userId = 2L;
        String status = "CONFIRMED";
        BookingDto bookingDto = new BookingDto(2L, LocalDate.of(2027, 2, 2),
                LocalDate.of(2027, 3, 2), 2L, 2L,
                "CONFIRMED");

        List<BookingDto> expected = List.of(bookingDto);

        MvcResult result = mockMvc.perform(get("/bookings")
                        .param("user_id", userId.toString())
                        .param("status", status))
                .andExpect(status().isOk())
                .andReturn();

        BookingDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto[].class);

        assertNotNull(actual);
        assertTrue(actual.length >= 1);
        assertNotNull(actual[0].id());
        assertEquals(userId, actual[0].userId());
        assertEquals(status, actual[0].status());

    }

    @Test
    @WithMockUser(username = "jane@google.pl", roles = "CUSTOMER")
    void getUserBookings_Success() throws Exception {
        Long userId = 2L;

        MvcResult result = mockMvc.perform(get("/bookings/my"))
                .andExpect(status().isOk())
                .andReturn();

        BookingDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto[].class);

        assertNotNull(actual);
        assertTrue(actual.length >= 1);
        assertNotNull(actual[0].id());
        assertEquals(userId, actual[0].userId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBooking_ByAdmin_Success() throws Exception {
        Long bookingId = 2L;
        BookingDto expected = new BookingDto(2L, LocalDate.of(2027, 2, 2),
                LocalDate.of(2027, 3, 2), 2L, 2L,
                "CONFIRMED");

        MvcResult result = mockMvc.perform(get("/bookings/{id}", bookingId))
                .andExpect(status().isOk())
                .andReturn();

        BookingDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto.class);

        assertNotNull(actual);
        assertNotNull(actual.id());
        assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getBooking_ByCustomer_ErrorStatus() throws Exception {
        Long bookingId = 2L;

        mockMvc.perform(get("/bookings/{id}", bookingId))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(username = "jane@google.pl", roles = "CUSTOMER")
    @Sql(scripts = "classpath:database/bookings/restore-updated-booking-id2.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateBooking_ByCustomer_Success() throws Exception {
        Long bookingId = 2L;
        Long userId = 2L;
        BookingUpdateRequestDto request = new BookingUpdateRequestDto(LocalDate.of(2027, 12, 2),
                LocalDate.of(2027, 12, 5), "CONFIRMED");

        String jsonRequest = objectMapper.writeValueAsString(request);


        MvcResult result = mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookingDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto.class);

        assertNotNull(actual);
        assertNotNull(actual.id());
        assertEquals(userId, actual.id());
        assertEquals(request.checkInDate(), actual.checkInDate());
        assertEquals(request.checkOutDate(), actual.checkOutDate());
        assertEquals(request.status(), actual.status());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Sql(scripts = "classpath:database/bookings/restore-deleted-booking-id1.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteBooking_Success() throws Exception {
        mockMvc.perform(delete("/bookings/{id}", 1L))
                .andExpect(status().isOk());
    }
}
