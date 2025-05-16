package common;

import history.HistoryManager;
import history.InMemoryHistoryManager;
import taskmanager.FileBackedTaskManager;
import taskmanager.TaskManager;

public class Managers {
    private static final String fileName = System.getProperty("user.home") + "\\" + "test.csv";

    public static TaskManager getDefaultTaskManager() {
        return new FileBackedTaskManager(fileName);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
