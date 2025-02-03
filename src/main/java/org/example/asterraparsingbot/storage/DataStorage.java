package org.example.asterraparsingbot.storage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DataStorage {

    private static final String FILE_PATH = "previous_data.txt";


    public static void saveData(Map<String, String> data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> loadData() {
        Map<String, String> data = new HashMap<>();
        File file = new File(FILE_PATH);

        // Если файл не существует, возвращаем пустую карту
        if (!file.exists()) {
            return data;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    data.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
