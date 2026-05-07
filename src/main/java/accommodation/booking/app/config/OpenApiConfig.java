package accommodation.booking.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI myOpenApiConfig() {

        Info info = new Info()
                .title("Accommodation Booking Application API")
                .description("""
                        <b>Main features:</b>
                        <ul>
                        <li>User registration and authentication using JWT Bearer tokens</li>
                        <li>Accommodation management</li>
                        <li>Booking management</li>
                        <li>Stripe payment integration</li>
                        <li>Role-based authorization</li>
                        <li>Rate limiting protection</li>
                        <li>OpenAPI / Swagger documentation</li>
                        </ul>
                        
                        <b>Authentication:</b><br>
                        Protected endpoints require JWT Bearer authentication.<br><br>
                        
                        <b>Rate limiting:</b>
                        <ul>
                        <li>Register: 5 requests per minute per IP</li>
                        <li>Login: 10 requests per minute per IP</li>
                        <li>Authenticated endpoints: 50 requests per minute per user</li>
                        <li>Fallback limiting by IP address for unauthenticated requests</li>
                        </ul>
                        
                        Rate limiting is primarily applied to write and authentication operations.
                        Read-only GET endpoints are intentionally less restricted for demonstration
                         purposes.<br><br>
                        
                        <b>Demo environment:</b>
                        <ul>
                        <li>Users may be granted administrator privileges to demonstrate 
                        administrative functionality</li>
                        <li>Some destructive operations are intentionally restricted in the public
                         demo environment</li>
                        <li>DELETE operations may be disabled to protect shared demonstration 
                        data</li>
                        </ul>
                        
                        When the rate limit is exceeded, the API returns HTTP 429 
                        (Too Many Requests).
                        """);

        return new OpenAPI().info(info)
                .components(new Components().addSecuritySchemes("BearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}
