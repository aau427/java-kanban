package history;

import model.Epic;
import model.Task;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryHistoryManager implements HistoryManager {

    private final CustomLinkedList<Task> historyList;
    private final Map<Integer, Node<Task>> id2NodeInHistory;

    public InMemoryHistoryManager() {
        historyList = new CustomLinkedList<>();
        id2NodeInHistory = new HashMap<>();
    }

    @Override
    public void addTask(Task task) {
        remove(task.getId());
        LocalDateTime startDate;
        if (task.getClass().getSimpleName().equals("Epic")) {
            Epic epic = (Epic) task;
            Optional<LocalDateTime> dateTimeOptional = epic.getStartTimeOptional();
            if (dateTimeOptional.isPresent()) {
                startDate = dateTimeOptional.get();
            } else {
                startDate = null;
            }
        } else {
            startDate = task.getStartTime();
        }
        Task taskToHistory = new Task(task.getId(), task.getName(), task.getDescription(), task.getState(),
                startDate, task.getDuration());
        Node<Task> newNode = historyList.linkLast(taskToHistory);
        id2NodeInHistory.put(task.getId(), newNode);
    }

    @Override
    public void remove(int id) {
        if (id2NodeInHistory.containsKey(id)) {
            Node<Task> node2Delete = id2NodeInHistory.get(id);
            historyList.removeNode(node2Delete);
            id2NodeInHistory.remove(id);
        }
    }

    @Override
    public List<Task> getHistoryList() {
        return historyList.getDataList();
    }
}
