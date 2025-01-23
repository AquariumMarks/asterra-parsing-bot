package org.example.asterraparsingbot.telegram;

import lombok.extern.slf4j.Slf4j;
import org.example.asterraparsingbot.config.BotConfig;
import org.example.asterraparsingbot.handler.GenplanParsingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@EnableScheduling
public class TelegramBot extends TelegramLongPollingBot {

    // Beans
    @Autowired
    private BotConfig config;

    private GenplanParsingHandler genplanParsingHandler;

    @Value("${sendMessagesOn}")
    private boolean sendOn;

    private final Map<Long, Integer> lastMessageIds = new HashMap<>();

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
                startCommandReceived(chatId);
            } else if (message.equals("/stop")) {

            }

        }
    }

    /**
     * Метод главного меню
     */
    private void startCommandReceived(long chatId) {

    }

    /**
     * Остановка отправки сообщений пользователю
     */
    private void stopSendingMessages() {
        sendOn = false;

    }

    /**
     * Отправка сообщений пользователю
     */
    private void sendMessage(long chatId, String sendToText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(sendToText);
        try {
            var sentMessage = execute(message);
            lastMessageIds.put(chatId, sentMessage.getMessageId());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


}
