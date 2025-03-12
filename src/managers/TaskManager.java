package managers;

import model.Epic;
import model.SubTask;
import model.Task;
import referencebook.States;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private final HashMap<Integer, Task> taskList = new HashMap<>();
    private final HashMap<Integer, Epic> epicList = new HashMap<>();
    private final HashMap<Integer, SubTask> subTaskList = new HashMap<>();

    public boolean createOrUpdateTask(Task task) {
        setIdToTask(task);
        if (!task.checkTask()) {
            return false;
        }
        taskList.put(task.getTaskId(), task);
        return true;
    }

    public boolean createOrUpdateEpic(Epic epic) {
        setIdToTask(epic);
        if (!epic.checkTask()) {
            return false;
        }
        setState(epic);
        epicList.put(epic.getTaskId(), epic);
        return true;
    }

    public boolean createOrUpdateSubTask(SubTask subTask) {
        int parentEpicId = subTask.getParentEpic();
        if (getEpicById(parentEpicId) == null) {
            System.out.println("Не могу породить/изменить подзадачу! Не нашел родительский Эпик № " + parentEpicId);
            return false;
        }
        setIdToTask(subTask);
        if (!subTask.checkTask()) {
            System.out.println("Не могу породить/изменить подзадачу: не прошла базовые проверки!");
            return false;
        }
        //в subTask может поменяться: наименование, комментарий, статус, а также и (!!!!) родительская задача
        //при этом считаем, что subTaskId никогда при изменении не меняется.
        removeSubTaskFromEpicIfNeed(subTask);
        Epic epic = epicList.get(subTask.getParentEpic());
        epic.getChildSubTasks().add(subTask.getTaskId());
        subTaskList.put(subTask.getTaskId(), subTask);
        setState(epic);
        return true;
    }

    public void deleteTaskById(int taskId) {
        taskList.remove(taskId);
    }

    public void deleteSubTaskById(int subTaskId) {
        SubTask subTask = subTaskList.get(subTaskId);
        Epic epic = epicList.get(subTask.getParentEpic());
        epic.removeSubtaskFromEpic(subTaskId);
        subTaskList.remove(subTaskId);
        setState(epic);
    }

    public void deleteEpicById(int epicId) {
        clearSubTaskListForEpic(epicId);
        epicList.remove(epicId);
    }

    public void deleteAllTasks() {
        taskList.clear();
    }

    public void deleteAllEpics() {
        epicList.clear();
        deleteSubTaskList();
    }

    public void deleteAllSubTasks() {
        for (Epic epic : epicList.values()) {
            epic.getChildSubTasks().clear();
            setState(epic);
        }
        deleteSubTaskList();
    }

    public Task getTaskById(int taskId) {
        return taskList.get(taskId);
    }

    public Epic getEpicById(int epicId) {
        return epicList.get(epicId);
    }

    public SubTask getSubTaskById(int subTaskId) {
        return subTaskList.get(subTaskId);
    }

    public ArrayList<Task> getTaskList() {
        return new ArrayList<>(taskList.values());
    }

    public ArrayList<Epic> getEpicList() {
        return new ArrayList<>(epicList.values());
    }

    public ArrayList<SubTask> getSubTaskList() {
        return new ArrayList<>(subTaskList.values());
    }

    //получение списка подзадач определенного Эпика
    public List<SubTask> getAllSubTaskForEpic(Epic epic) {
        List<SubTask> epicSubTasks = new ArrayList<>();
        for (Integer subTaskId : epic.getChildSubTasks()) {
            epicSubTasks.add(subTaskList.get(subTaskId));
        }
        return epicSubTasks;
    }

    private void setIdToTask(Task task) {
        if (task.getTaskId() == null) {
            task.setTaskId(IdManager.getNextId());
        }
    }

    private void setState(Epic epic) {
        if ((epic.getChildSubTasks() == null) || epic.getChildSubTasks().isEmpty()) {
            epic.setState(States.NEW);
        } else {
            boolean isAllSubTasksAreDone = true;
            boolean isAllSubTasksAreNew = true;
            for (SubTask subTask : getAllSubTaskForEpic(epic)) {
                if (subTask.getTaskState() == States.DONE) {
                    isAllSubTasksAreNew = false;
                } else if (subTask.getTaskState() == States.NEW) {
                    isAllSubTasksAreDone = false;
                } else {
                    isAllSubTasksAreNew = false;
                    isAllSubTasksAreDone = false;
                }
            }
            if (isAllSubTasksAreNew) {
                epic.setState(States.NEW);
            } else if (isAllSubTasksAreDone) {
                epic.setState(States.DONE);
            } else {
                epic.setState(States.IN_PROGRESS);
            }
        }
    }

    /* Проверяет, не изменили ли у subTask родителя.
       Если изменили, то удаляет некорректную запись в списке дочерних задач эпика   */
    private void removeSubTaskFromEpicIfNeed(SubTask newSubTask) {
        SubTask oldSubTask = subTaskList.get(newSubTask.getTaskId());
        if (oldSubTask == null) {
            return;
        }
        if (oldSubTask.getParentEpic() != newSubTask.getParentEpic()) {
            Epic epic = epicList.get(oldSubTask.getParentEpic());
            epic.getChildSubTasks().remove(oldSubTask.getTaskId());
            setState(epic);
        }
    }

    private void clearSubTaskListForEpic(int epicId) {
        Epic epic = epicList.get(epicId);
        for (Integer subTaskId : epic.getChildSubTasks()) {
            subTaskList.remove(subTaskId);
        }
        //удалить все его подзадачи из списка дочерних подзадач
        epic.getChildSubTasks().clear();
    }

    private void deleteSubTaskList() {
        subTaskList.clear();
    }

}
