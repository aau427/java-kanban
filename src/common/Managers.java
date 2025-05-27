package common;

import comparators.DateTimeComparator;
import history.HistoryManager;
import history.InMemoryHistoryManager;
import managers.FileBackedTaskManager;
import managers.IntervalManager;
import managers.TaskManager;

import java.time.format.DateTimeFormatter;

public class Managers {
    private static final String fileName = System.getProperty("user.home") + "\\" + "test.csv";
    private static final DateTimeFormatter dateTimeFormatterForIO = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static TaskManager getDefaultTaskManager() {
        return new FileBackedTaskManager(fileName);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static IntervalManager getDefaultIntervalManager() {
        return new IntervalManager();
    }

    public static DateTimeComparator getDefaultDateTimeComparator() {
        return new DateTimeComparator();
    }

    public static DateTimeFormatter getDefaultDateTimeFormatter() {
        return dateTimeFormatterForIO;
    }
}
