package accommodation.booking.app.security.ratelimit;

import accommodation.booking.app.dto.ratelimit.RateLimitMetricsDto;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final RateLimitProperties properties;
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> blockedPerIp = new ConcurrentHashMap<>();
    private final Map<Long, AtomicInteger> blockedPerUser = new ConcurrentHashMap<>();
    private final AtomicInteger blockedRequests = new AtomicInteger(0);

    public Bucket resolveRegisterBucket(String ip) {
        return cache.computeIfAbsent("register:" + ip, key ->
                createBucket(properties.getRegister(), Duration.ofMinutes(properties.getDuration()))
        );
    }

    public Bucket resolveLoginBucket(String ip) {
        return cache.computeIfAbsent("login:" + ip, key ->
                createBucket(properties.getLogin(), Duration.ofMinutes(properties.getDuration()))
        );
    }

    public Bucket resolveApiBucket(Long userId) {
        return cache.computeIfAbsent("user:" + userId, key ->
                createBucket(properties.getApi(), Duration.ofMinutes(properties.getDuration()))
        );
    }

    public Bucket resolveApiBucket(String ip) {
        return cache.computeIfAbsent("ip:" + ip, key ->
                createBucket(properties.getApi(), Duration.ofMinutes(properties.getDuration()))
        );
    }

    private Bucket createBucket(long capacity, Duration refillDuration) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, refillDuration)
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public void incrementBlockedRequests() {
        blockedRequests.incrementAndGet();
    }

    public void incrementIpBlocked(String ip) {
        blockedPerIp
                .computeIfAbsent(ip, k -> new AtomicInteger(0))
                .incrementAndGet();
    }

    public void incrementUserBlocked(Long userId) {
        blockedPerUser
                .computeIfAbsent(userId, k -> new AtomicInteger(0))
                .incrementAndGet();
    }

    public RateLimitMetricsDto getMetrics() {

        Map<String, Integer> ipStats = blockedPerIp.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()
                ));

        Map<Long, Integer> userStats = blockedPerUser.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()
                ));

        return new RateLimitMetricsDto(
                blockedRequests.get(),
                userStats,
                ipStats
        );
    }

}
