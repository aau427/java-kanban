package managers;

import model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int TASK_IN_HISTORY = 10;
    private final List<Task> historyList;

    public InMemoryHistoryManager() {
        historyList = new LinkedList<>();
    }

    @Override
    public void addTask(Task task) {
        if (historyList.size() == TASK_IN_HISTORY) {
            historyList.removeFirst();
        }
        Task taskToHistory = new Task(task.getId(), task.getName(), task.getDescription(), task.getState());
        historyList.add(taskToHistory);
    }

    @Override
    public List<Task> getHistoryList() {
        return List.copyOf(historyList);
    }
}
