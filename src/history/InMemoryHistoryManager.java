package history;

import model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Task taskToHistory = new Task(task.getId(), task.getName(), task.getDescription(), task.getState());
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
