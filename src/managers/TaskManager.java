package managers;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
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

    Task getTaskById(int taskId);

    Epic getEpicById(int epicId);

    SubTask getSubTaskById(int subTaskId);

    ArrayList<Task> getTaskList();

    ArrayList<Epic> getEpicList();

    ArrayList<SubTask> getSubTaskList();

    List<SubTask> getAllSubTaskForEpic(Epic epic);
}
