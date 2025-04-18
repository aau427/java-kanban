package taskmanager;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.List;

public interface TaskManager {
    boolean createTask(Task task);

    boolean updateTask(Task task);

    boolean createEpic(Epic epic);

    boolean updateEpic(Epic epic);

    boolean createSubTask(SubTask subTask);

    boolean updateSubTask(SubTask subTask);

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
}
