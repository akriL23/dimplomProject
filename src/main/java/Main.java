import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.io.IOException;
import java.security.GeneralSecurityException;


public class Main {
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        String botName = "MarksProject_Bot"; // В место звездочек указываем имя созданного вами ранее Бота
        String botToken = "7001093850:AAFwp0lKpKQtJOaWs2iknl_3-KWfLh8PZD8";
        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new TelegramBot(botName, botToken));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
