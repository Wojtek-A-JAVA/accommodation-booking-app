package accommodation.booking.app.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import accommodation.booking.app.security.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@TestPropertySource(properties = {
        "rate.limit.api=1",
        "rate.limit.duration=1"
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RateLimitMetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void shouldReturn429_whenRateLimitExceeded() throws Exception {
        String ip = "127.0.0.1";

        mockMvc.perform(post("/health"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/health"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldLimitPerUser() throws Exception {
        String token = "Bearer " + jwtUtil.generateToken(3L, "james@google.com");

        mockMvc.perform(post("/health").header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/health").header("Authorization", token))
                .andExpect(status().isTooManyRequests());
    }
}
