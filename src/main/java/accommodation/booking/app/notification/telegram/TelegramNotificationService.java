package accommodation.booking.app.notification.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationService implements NotificationService {

    @Value("${telegram.admin.chat-id}")
    private String adminChatId;

    private final TelegramBot bot;

    @Async
    @Override
    public void telegramSendMessage(String message) {
        try {
            bot.execute(new SendMessage(adminChatId, message));
            log.info("Telegram notification sent");
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram notification", e);
        }
    }
}
