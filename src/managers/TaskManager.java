package managers;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.List;

public interface TaskManager {
    int createTask(Task task);

    int updateTask(Task task);

    int createEpic(Epic epic);

    int updateEpic(Epic epic);

    int createSubTask(SubTask subTask);

    int updateSubTask(SubTask subTask);

    void deleteTaskById(int taskId);

    void deleteSubTaskById(int subTaskId);

    void deleteEpicById(int epicId);

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubTasks();

    List<Task> getHistory();

    Task getTaskById(int taskId);

    Epic getEpicById(int epicId);

    SubTask getSubTaskById(int subTaskId);

    List<Task> getTaskList();

    List<Epic> getEpicList();

    List<SubTask> getSubTaskList();

    List<SubTask> getAllSubTaskForEpic(Epic epic);

    List<Task> getPrioritizedTasks();
}
