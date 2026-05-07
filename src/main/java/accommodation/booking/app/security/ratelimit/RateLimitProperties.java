package accommodation.booking.app.security.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties(prefix = "rate.limit")
@Getter
@Setter
public class RateLimitProperties {
    private int register;
    private int login;
    private int api;
    private int duration;
}
