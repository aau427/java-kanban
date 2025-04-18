package common;

import history.HistoryManager;
import history.InMemoryHistoryManager;
import taskmanager.InMemoryTaskManager;
import taskmanager.TaskManager;

public class Managers {
    public static TaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
