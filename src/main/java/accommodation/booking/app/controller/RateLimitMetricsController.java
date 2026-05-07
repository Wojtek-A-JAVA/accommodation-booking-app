package accommodation.booking.app.controller;

import accommodation.booking.app.dto.ratelimit.RateLimitMetricsDto;
import accommodation.booking.app.security.ratelimit.RateLimiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "06. Administration", description = "Administrative and monitoring endpoints")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RateLimitMetricsController {

    private final RateLimiterService rateLimiterService;

    @GetMapping("/rate-limit")
    @Operation(summary = "Get rate limit statistics",
            description = "Returns information about API rate limiting, "
                    + "including blocked requests and usage per user/IP")
    public RateLimitMetricsDto getRateLimitStats() {
        return rateLimiterService.getMetrics();
    }
}
