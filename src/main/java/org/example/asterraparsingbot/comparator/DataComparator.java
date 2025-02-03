package org.example.asterraparsingbot.comparator;

import java.util.Map;

public class DataComparator {

    public static String compareData(Map<String, String> previousData, Map<String, String> currentData) {
        StringBuilder changes = new StringBuilder();

        for (Map.Entry<String, String> entry : currentData.entrySet()) {
            String key = entry.getKey();
            String currentValue = entry.getValue();
            String previousValue = previousData.get(key);

            if (previousValue == null) {
                changes.append("Участок ").append(key).append(" добавлен\n");
            } else if (!previousValue.equals(currentValue)) {
                changes.append("Участок ").append(key).append("\n")
                        .append("Стоимость: ").append(previousValue).append(" -> ").append(currentValue).append("\n");
            }
        }

        for (String key : previousData.keySet()) {
            if (!currentData.containsKey(key)) {
                changes.append("Участок ").append(key).append(" удален\n");
            }
        }

        return changes.toString();
    }
}
