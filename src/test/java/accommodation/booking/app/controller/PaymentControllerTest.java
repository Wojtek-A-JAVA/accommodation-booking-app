package accommodation.booking.app.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import accommodation.booking.app.dto.payment.CreatePaymentRequestDto;
import accommodation.booking.app.dto.payment.PaymentDto;
import accommodation.booking.app.dto.payment.PaymentResponseDto;
import accommodation.booking.app.service.PaymentService;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    @WithMockUser(username = "james@google.com", roles = "CUSTOMER")
    void getPayments_AsCustomer_Success() throws Exception {
        Long userId = 3L;

        PaymentDto dto = new PaymentDto(
                10L, 1L, "PENDING", BigDecimal.valueOf(20), "sess_1", "http://test.com"
        );

        when(paymentService.getAllPaymentsByUserId(eq(userId), eq("james@google.com")))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/payments").param("user_id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "james@google.com", roles = "CUSTOMER")
    void createPaymentSession_ValidBody_ReturnsPaymentDto() throws Exception {
        CreatePaymentRequestDto request = new CreatePaymentRequestDto(1L);

        PaymentDto dto = new PaymentDto(
                11L, 1L, "PENDING", BigDecimal.valueOf(20), "sess_2", "http://example.com/checkout"
        );

        when(paymentService.createPaymentSession(eq(request), eq("james@google.com")))
                .thenReturn(dto);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11L))
                .andExpect(jsonPath("$.bookingId").value(1L));
    }

    @Test
    void paymentSuccess_PublicEndpoint_ReturnsResponse() throws Exception {
        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(1L);
        response.setStatus("CONFIRMED");
        response.setMessage("Payment completed");

        when(paymentService.paymentSuccess(eq("sess_ok"))).thenReturn(response);

        mockMvc.perform(get("/payments/success").param("session_id", "sess_ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1L))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void paymentCancel_PublicEndpoint_ReturnsResponse() throws Exception {
        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(2L);
        response.setStatus("CANCELED");
        response.setMessage("Payment is canceled");

        when(paymentService.paymentCancel(eq("sess_cancel"))).thenReturn(response);

        mockMvc.perform(get("/payments/cancel").param("session_id", "sess_cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(2L))
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }
}