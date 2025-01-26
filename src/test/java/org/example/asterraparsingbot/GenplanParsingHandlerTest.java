package org.example.asterraparsingbot;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenplanParsingHandlerTest {
    public static void main(String[] args) {
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
                        double squareValue = Double.parseDouble(matcher.group(1).replace(",", "."));
                        if (squareValue >= 5 && squareValue <= 5.5) {
                            hasSquare = true;
                            break;
                        }
                    }
                }

                if (!statusElements.isEmpty()) {
                    isAvailable = true;
                }

                if (isLandPlot && hasSquare && isAvailable) {
                    var number = item.parent().select("div.number").text();
                    System.out.println(number + "\n" + item.parent().select("p.square").text() +
                            "\n" + item.parent().select("div.status.stat0").text() +  "\n "); // Выводим HTML-содержимое родительского элемента
//                    System.out.println(item.parent().html());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
