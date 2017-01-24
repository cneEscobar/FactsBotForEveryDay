package hellobot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;


public class telegramBot extends TelegramLongPollingBot {

    private int messageMaxLength = 400;

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new telegramBot());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
    return "cne33_bot";
    }

    @Override
    public String getBotToken() {
    return "253235277:AAG7os9TsnpWC5EAE0-JlHkm94wr9e5izjU";
    }

    @Override
    public void onUpdateReceived(Update update) {
        // получаем от пользователя сообщение
        Message message = update.getMessage();

        if (message != null && message.hasText()) {
            // сравниваем с тем что он ответил , в данном случае "/help"
            if (message.getText().equals("Помощь") || message.getText().equals("/help")) {
                sendMsg(message, "Вот список всех команд:"
                        + "\n"
                        + "/facts"
                        + "\n"
                        + "/help");
            } else if (message.getText().equals("Ежедневный факт") || message.getText().equals("/facts")) {
                // сравниваем с тем что он ответил , в данном случае "/facts"
                //TODO: json handling
                String urlString = "https://apps.arm1.ru/api/events";
                try {
                    URL url = new URL(urlString);
                    HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
                    request.connect();

                    JsonParser parser = new JsonParser();
                    JsonElement root = parser.parse(new InputStreamReader((InputStream) request.getContent()));
                    JsonObject object = root.getAsJsonObject();
                    JsonArray events = object.get("events").getAsJsonArray();

                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i=0; i<events.size()-1; i++) {
                        JsonObject news = events.get(i).getAsJsonObject();
                        //текст новости
                        String newsText = news.get("event").getAsString();
                        if (stringBuilder.length()+newsText.length()>messageMaxLength) {
                            if (stringBuilder.length()>=0) sendMsg(update.getMessage(), stringBuilder.toString());
                            stringBuilder = new StringBuilder();
                            if (newsText.length() <= messageMaxLength) stringBuilder.append(newsText).append("\n\n");
                        } else if (newsText.length() <= messageMaxLength) stringBuilder.append(newsText).append("\n\n");
                    }
                    if (stringBuilder.length()>=0) sendMsg(update.getMessage(), stringBuilder.toString());
                    /*
                    events.get(i)
                    //номер новости в списке
                    int newsId = 1;
                    //новость
                    JsonObject news = events.get(newsId).getAsJsonObject();
                    //текст новости
                    String newsText = news.get("event").getAsString();
                    sendMsg(update.getMessage(), newsText);
                    */

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                sendMsg(message, "Извини, но я тебя не понимаю :( "
                        +"\n"
                        +"Используй одну из кнопок ниже");
            }

        }

    }

    private void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        // клавиуатура
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboad(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("Помощь");
        keyboardFirstRow.add("Ежедневный факт");

        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        try {
            sendMessage(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}