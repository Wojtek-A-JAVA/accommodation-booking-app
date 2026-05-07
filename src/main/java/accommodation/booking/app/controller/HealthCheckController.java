package accommodation.booking.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "06. Administration")
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping
    public String healthCheck() {
        return "OK";
    }

    @PostMapping
    public String healthCheckPost() {
        return "OK";
    }
}
