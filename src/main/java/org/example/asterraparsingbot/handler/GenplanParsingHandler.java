package org.example.asterraparsingbot.handler;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.asterraparsingbot.comparator.DataComparator;
import org.example.asterraparsingbot.storage.DataStorage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class GenplanParsingHandler {

    public String getGenplan() {
        log.info("start getGenplan()");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.asterra.ru/projects/bogorodsk-forest/genplan/")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = "";
            if (response.body() != null) {
                responseBody = response.body().string();
            }
            Document document = Jsoup.parse(responseBody);
            Elements items = document.select(".genplanTooltip-content");

            StringBuilder result = new StringBuilder();
            Map<String, String> currentData = new HashMap<>();
            int totalPlots = 0;

            for (Element item : items) {
                Elements typeElements = item.select("p.type");
                Elements squareElements = item.select("p.square");
                Elements statusElements = item.parent().select("div.status.stat0");

                boolean isLandPlot = false;
                boolean hasSquare = false;
                boolean isAvailable = false;

                for (Element typeElement : typeElements) {
                    if (typeElement.text().equals("Земельный участок")) {
                        isLandPlot = true;
                        break;
                    }
                }

                Pattern pattern = Pattern.compile("(\\d+(\\.\\d+)?)\\s*сот\\.");
                for (Element squareElement : squareElements) {
                    Matcher matcher = pattern.matcher(squareElement.text());
                    if (matcher.find()) {
                        double square = Double.parseDouble(matcher.group(1).replace(",", "."));
                        if (square >= 5 && square <= 5.5) {
                            hasSquare = true;
                            break;
                        }
                    }
                }

                if (!statusElements.isEmpty()) {
                    isAvailable = true;
                }

                if (isLandPlot && hasSquare && isAvailable) {
                    String squareResult = squareElements.first().text();
                    String number = item.selectFirst("div.number").text();
                    String status = statusElements.first().text();
                    result.append(number).append("\n")
                            .append("Размер: ").append(squareResult).append("\n")
                            .append("Стоимость: ").append(status).append("\n\n");
                    currentData.put(number, status);
                    totalPlots++;
                }
            }

            // Загрузка предыдущих данных
            Map<String, String> previousData = DataStorage.loadData();

            // Сравнение данных
            String changes = DataComparator.compareData(previousData, currentData);
            if (!changes.isEmpty()) {
                result.append("Изменения:\n").append(changes);
            }

            // Сохранение текущих данных
            DataStorage.saveData(currentData);
            result.append("\nОбщее количество участков: ").append(totalPlots);

            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}

