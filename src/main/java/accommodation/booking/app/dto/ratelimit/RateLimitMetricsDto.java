package accommodation.booking.app.dto.ratelimit;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Rate limit metrics response")
public record RateLimitMetricsDto(

        @Schema(description = "Total number of blocked requests")
        int totalBlocked,

        @Schema(description = "Blocked requests per user")
        Map<Long, Integer> blockedByUser,

        @Schema(description = "Blocked requests per IP address")
        Map<String, Integer> blockedByIp
) {
}
