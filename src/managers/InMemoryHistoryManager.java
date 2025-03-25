package managers;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    public static final int TASK_IN_HISTORY = 10;
    private final List<Task> historyList;

    public InMemoryHistoryManager() {
        historyList = new ArrayList<>();
    }

    @Override
    public void addTask(Task task) {
        if(historyList.size() == TASK_IN_HISTORY) {
            //грохнем самую старую, т.к. по ТЗ выводим всегда 10 ПОСЛЕДНИХ просмотров
            historyList.removeFirst();
        }
        /*в связи с тем, что в тестах есть проверка:
        "задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных",
        нужно обеспечить в истории состояние задачи при ее просмотре.
        Т.о. если задача в будущем изменится, все равно в истории сохранится просмотр ее "предыдущей" версии,
        т.е. версии, актуальной на момент просмотра!!!!
        вот такой вот мозговынос:)
         */
        Task taskToHistory = new Task(task.getName(), task.getDescription(), task.getState());
        taskToHistory.setId(task.getId());
        historyList.add(taskToHistory);
    }

    @Override
    public List<Task> getHistoryList() {
        return historyList;
    }
}
