package org.example.asterraparsingbot.telegram;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.asterraparsingbot.config.BotConfig;
import org.example.asterraparsingbot.handler.GenplanParsingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@Component
@EnableScheduling
public class TelegramBot extends TelegramLongPollingBot {

    // Beans
    @Autowired
    private BotConfig config;

    private final GenplanParsingHandler genplanHandler;

    @Value("${sendMessagesOn}")
    private boolean sendOn;

    private final Map<Long, Integer> lastMessageIds = new HashMap<>();
    @Getter
    private final Set<Long> chatIds = new HashSet<>(); // Хранение chatId

    public TelegramBot(GenplanParsingHandler genplanHandler) {
        this.genplanHandler = genplanHandler;
    }

    @PostConstruct
    public void init() {
        List<BotCommand> listOfCommand = new ArrayList<>();
        listOfCommand.add(new BotCommand("/start", "Перезапустить бота"));
        listOfCommand.add(new BotCommand("/stop", "Остановить отправку статистики"));
        try {
            this.execute(new SetMyCommands(listOfCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (message.equals("/start")) {
                chatIds.add(chatId); // Добавляем chatId в коллекцию
                startCommandReceived();
            } else if (message.equals("/stop")) {
                chatIds.remove(chatId); // Удаляем chatId при команде /stop
                sendMessage(chatId, "Отправка остановлена!");
            }
        }
    }

    /**
     * Метод главного меню
     */
    private void startCommandReceived() {
        if (sendOn) {
            runDailyTask();
        }
    }

    public void runDailyTask() {
        String genplanData = genplanHandler.getGenplan();
        if (genplanData != null) {
            for (Long chatId : getChatIds()) {
                sendMessage(chatId, genplanData);
            }
        }
    }

    /**
     * Метод для выполнения задачи по расписанию
     */
    @Scheduled(cron = "0 0 09 * * ?")
//    @Scheduled(fixedDelayString = "20000")
    public void scheduledDailyTask() {
        runDailyTask();
    }

    /**
     * Отправка сообщений пользователю
     */
    public void sendMessage(long chatId, String sendToText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(sendToText);
        try {
            var sentMessage = execute(message);
            lastMessageIds.put(chatId, sentMessage.getMessageId());
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chatId: " + chatId, e);
            if (e.getMessage().contains("Forbidden: bot was blocked by the user")) {
                chatIds.remove(chatId); // Удаляем chatId, если пользователь заблокировал бота
            }
        }
    }


}
