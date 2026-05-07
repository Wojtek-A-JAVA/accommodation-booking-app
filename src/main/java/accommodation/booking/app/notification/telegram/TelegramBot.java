package accommodation.booking.app.notification.telegram;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true")
@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;

    public TelegramBot(@Value("${telegram.bot.token}") String botToken,
                       @Value("${telegram.bot.username}") String botUsername) {
        super(botToken);
        this.botToken = botToken;
        this.botUsername = botUsername;
        log.info("TelegramBot initialized");
    }

    @PostConstruct
    public void test() {
        System.out.println("BOT HASH: " + this.hashCode());
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Received update: {}", update);
    }
}

