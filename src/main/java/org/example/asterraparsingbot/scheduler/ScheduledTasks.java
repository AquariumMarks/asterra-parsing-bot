//package org.example.asterraparsingbot.scheduler;
//
//import lombok.extern.slf4j.Slf4j;
//import org.example.asterraparsingbot.handler.GenplanParsingHandler;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class ScheduledTasks {
//
//    private final GenplanParsingHandler genplanHandler;
//
//    public ScheduledTasks(GenplanParsingHandler genplanHandler) {
//        this.genplanHandler = genplanHandler;
//    }
//
//    /**
//     * Метод для запуска задачи вручную
//     */
//    public String runDailyTask() {
//        log.info("Starting tasks");
//        String genplanData = genplanHandler.getGenplan();
//        return genplanData;
//    }
//}
