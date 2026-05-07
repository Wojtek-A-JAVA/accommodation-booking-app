package accommodation.booking.app.notification.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "false", matchIfMissing = true)
@Component
@Slf4j
public class DisabledTelegramNotificationService implements NotificationService {

    @Override
    public void telegramSendMessage(String message) {
        log.info("Notifications are disabled");
    }
}
