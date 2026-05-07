package accommodation.booking.app.security.ratelimit;

import accommodation.booking.app.security.jwt.JwtUtil;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        String header = request.getHeader("Authorization");
        Long userId = null;
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                userId = jwtUtil.extractUserId(token);
            } catch (io.jsonwebtoken.JwtException e) {
                log.debug("Invalid JWT token: {}", e.getMessage());
            }
        }

        String method = request.getMethod();
        String uri = request.getRequestURI();
        Bucket bucket = null;

        if (uri.equals("/auth/register")) {
            bucket = rateLimiterService.resolveRegisterBucket(ip);
        } else if (uri.equals("/auth/login")) {
            bucket = rateLimiterService.resolveLoginBucket(ip);
        } else if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")
                || method.equals("DELETE")) {
            if (userId != null) {
                bucket = rateLimiterService.resolveApiBucket(userId);
            } else {
                bucket = rateLimiterService.resolveApiBucket(ip);
            }
        }

        if (bucket != null) {
            if (!bucket.tryConsume(1)) {
                if (userId != null) {
                    log.warn("Rate limit exceeded for userId: {}, endpoint: {}", userId, uri);
                } else {
                    log.warn("Rate limit exceeded for IP: {}, endpoint: {}", ip, uri);
                }

                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("""
                        {
                          "error": "Too many requests",
                          "message": "Rate limit exceeded"
                        }
                        """);

                rateLimiterService.incrementBlockedRequests();
                if (userId != null) {
                    rateLimiterService.incrementUserBlocked(userId);
                } else {
                    rateLimiterService.incrementIpBlocked(ip);
                }
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
