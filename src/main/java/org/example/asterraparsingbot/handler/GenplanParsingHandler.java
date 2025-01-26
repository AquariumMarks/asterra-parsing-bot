package org.example.asterraparsingbot.handler;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EnableScheduling
public class GenplanParsingHandler {

    @Value("${https.asterra.genplan}")
    private String url;


    @Scheduled(fixedDelay = 180000000)
    public String getGenplan() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
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

            for (Element item : items) {
                Elements typeElements = item.select("p.type"); // Тип земельного участка
                Elements squareElements = item.select("p.square"); // Размер земельного участка в сотках
                assert item.parent() != null;
                Elements statusElements = item.parent().select("div.status.stat0"); // статус "Свободно"

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
                    var squareResult = item.select("p.square").text();
                    var number = item.select("div.number").text();
                    var status = item.select("div.status.stat0").text();
                    StringBuilder result = new StringBuilder();
                    for (int i = 0; i < statusElements.size(); i++) {
                        result.append(statusElements.get(i).append(number));
                    }
                }
            }
            assert response.body() != null;
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}

