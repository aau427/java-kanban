package taskmanager;

import common.Managers;
import history.HistoryManager;
import model.Epic;
import model.SubTask;
import model.Task;
import referencebook.States;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> taskList = new HashMap<>();
    private final Map<Integer, Epic> epicList = new HashMap<>();
    private final Map<Integer, SubTask> subTaskList = new HashMap<>();
    private Integer currentId = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public int createTask(Task task) {
        if (task.getId() != null) {
            System.out.println("Ошибка! При создании задачи указан ID, это работа  TaskManager-а!");
            return -1;
        }
        if (!task.isValid()) {
            return -1;
        }
        int taskId = getNextId();
        createTaskCommon(new Task(taskId, task.getName(), task.getDescription(), task.getState()));
        return taskId;
    }

    public int createTaskCommon(Task task) {
        taskList.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public boolean updateTask(Task task) {
        if (task.getId() == null) {
            System.out.println("Ошибка! не указан Id задачи, не могу понять, что менять!");
            return false;
        }
        if (!task.isValid()) {
            return false;
        }
        taskList.put(task.getId(), task);
        return true;
    }

    @Override
    public int createEpic(Epic epic) {
        if (epic.getId() != null) {
            System.out.println("Ошибка! При создании Эпика. Указан Id! это работа TaskManager-а!");
            return -1;
        }
        if (!epic.isValid()) {
            return -1;
        }
        int epicId = getNextId();
        return createEpicCommon(new Epic(epicId, epic.getName(), epic.getDescription()));
    }

    protected int createEpicCommon(Epic epic) {
        epic.setState(States.NEW);
        epicList.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (epic.getId() == null) {
            System.out.println("Ошибка! не указан ID Эпика! не могу понять, что менять!");
            return false;
        }
        if (!epic.isValid()) {
            return false;
        }
        Epic oldEpic = epicList.get(epic.getId());
        if (oldEpic == null) {
            System.out.println(String.format("Ошибка: не нашел Epic № %d в списке  при его обновлении!", epic.getId()));
            return false;
        }
        //пользователь не может управлять статусом Эпика!
        if (oldEpic.getState() != epic.getState()) {
            System.out.println("Ошибка! в соответствии с ТЗ пользователь не может поменять статус Эпика самостоятельно!");
            return false;
        }
        epicList.put(epic.getId(), epic);
        return true;
    }

    @Override
    public int createSubTask(SubTask subTask) {
        if (subTask.getId() != null) {
            System.out.println("Ошибка! При создании подзадачи указан Id! это работа TaskManager-а!");
            return -1;
        }
        if (!subTask.isValid()) {
            System.out.println("Не могу породить/изменить подзадачу: не прошли базовые проверки!");
            return -1;
        }
        int parentEpicId = subTask.getParentEpic();
        /*Судя по ТЗ в историю пишем, если кто-то "смотрит" эпик
         Нам здесь не нужно записывать в историю, т.к. это чисто техническое получение Епика*/
        Epic epic = getEpicByIdWithoutHistory(parentEpicId);
        if (epic == null) {
            System.out.println("Не могу породить/изменить подзадачу! Не нашел родительский Эпик № " + parentEpicId);
            return -1;
        }
        int subTaskId = getNextId();
        SubTask subTaskToInsert = new SubTask(subTaskId, subTask.getName(), subTask.getDescription(),
                subTask.getState(), subTask.getParentEpic());
        return createSubTaskCommon(subTaskToInsert);
    }

    public int createSubTaskCommon(SubTask subTask) {
        Epic epic = getEpicByIdWithoutHistory(subTask.getParentEpic());
        epic.getChildSubTasks().add(subTask.getId());
        subTaskList.put(subTask.getId(), subTask);
        setState(epic);
        return subTask.getId();

    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        if (subTask.getId() == null) {
            System.out.println("Ошибка! не указан ID подзадачи! не могу понять, что менять!");
            return false;
        }
        if (!subTask.isValid()) {
            System.out.println("Не могу породить/изменить подзадачу: не прошла базовые проверки!");
            return false;
        }
        SubTask oldSubtask = subTaskList.get(subTask.getId());
        if (oldSubtask == null) {
            System.out.println(String.format("Ошибка: не нашел подзадачу № %d в списке  при ее обновлении!", subTask.getId()));
            return false;
        }
        int parentEpicId = subTask.getParentEpic();
        //не нужно писать в историю, никто не смотрит на Эпик извне, это чисто техническое получение Эпика
        Epic epic = getEpicByIdWithoutHistory(parentEpicId);
        if (epic == null) {
            System.out.println(String.format("Не могу породить/изменить подзадачу! Не нашел ee Эпик № %d ", parentEpicId));
            return false;
        }
        /*В subTask может поменяться не только наименование, комментарий, статус, а также Эпик!!!
        В случае, если в подзадаче меняется ЭПИК, то необходимо:
          - в "старом" Эпике убрать ссылку на подзадачу;
          - перерассчитать статус старого Эпика, так как изменился состав его подзадач.
          - добавить подзадачу в новый Эпик.
          - перерассчитать статус нового Эпика, так как изменился состав его подзадач
        */
        if (oldSubtask.getParentEpic() != subTask.getParentEpic()) {
            Epic oldEpic = epicList.get(oldSubtask.getParentEpic());
            oldEpic.getChildSubTasks().remove(subTask.getId());
            setState(oldEpic);
            epic.getChildSubTasks().add(subTask.getId());
        }
        subTaskList.put(subTask.getId(), subTask);
        setState(epic);
        return true;
    }

    @Override
    public void deleteTaskById(int taskId) {
        /* в соответствии с новым ТЗ (принт 6)
            Добавьте вызов метода при удалении задач, чтобы они удалялись также из истории просмотров.
            относится к задачам, подзадачам и эпикам
        */
        historyManager.remove(taskId);
        taskList.remove(taskId);
    }

    @Override
    public void deleteSubTaskById(int subTaskId) {
        SubTask subTask = subTaskList.remove(subTaskId);
        Epic epic = epicList.get(subTask.getParentEpic());
        //remove не по Index, а по Object!
        epic.getChildSubTasks().remove(Integer.valueOf(subTaskId));
        setState(epic);
        historyManager.remove(subTaskId);
    }

    @Override
    public void deleteEpicById(int epicId) {
        clearSubTaskListForEpic(epicId);
        epicList.remove(epicId);
        historyManager.remove(epicId);
    }

    @Override
    public void deleteAllTasks() {
        for (Integer taskId : taskList.keySet()) {
            historyManager.remove(taskId);
        }
        taskList.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Integer epicId : epicList.keySet()) {
            historyManager.remove(epicId);
        }
        epicList.clear();
        deleteSubTaskList();
    }

    @Override
    public void deleteAllSubTasks() {
        for (Epic epic : epicList.values()) {
            epic.getChildSubTasks().clear();
            setState(epic);
        }
        deleteSubTaskList();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistoryList();
    }

    @Override
    public Task getTaskById(int taskId) {
        Task task = taskList.get(taskId);
        if (task != null) {
            historyManager.addTask(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int epicId) {
        Epic epic = epicList.get(epicId);
        if (epic != null) {
            historyManager.addTask(epic);
            Epic tmpEpic = epic.getEpicCopy();
            return tmpEpic;
        } else {
            return epic;
        }
    }

    @Override
    public SubTask getSubTaskById(int subTaskId) {
        SubTask subTask = subTaskList.get(subTaskId);
        if (subTask != null) {
            historyManager.addTask(subTask);
        }
        return subTask;
    }

    @Override
    public List<Task> getTaskList() {
        return new ArrayList<>(taskList.values());
    }

    @Override
    public List<Epic> getEpicList() {
        List<Epic> returnEpicList = new ArrayList<>();
        for (Epic epic : epicList.values()) {
            returnEpicList.add(epic.getEpicCopy());
        }
        return returnEpicList;
    }

    @Override
    public List<SubTask> getSubTaskList() {
        return new ArrayList<>(subTaskList.values());
    }

    @Override
    public List<SubTask> getAllSubTaskForEpic(Epic epic) {
        List<SubTask> epicSubTasks = new ArrayList<>();
        for (Integer subTaskId : epic.getChildSubTasks()) {
            epicSubTasks.add(subTaskList.get(subTaskId));
        }
        return epicSubTasks;
    }

    private void setState(Epic epic) {
        if ((epic.getChildSubTasks() == null) || epic.getChildSubTasks().isEmpty()) {
            epic.setState(States.NEW);
        } else {
            boolean isAllSubTasksAreDone = true;
            boolean isAllSubTasksAreNew = true;
            for (SubTask subTask : getAllSubTaskForEpic(epic)) {
                if (subTask.getState() == States.DONE) {
                    isAllSubTasksAreNew = false;
                } else if (subTask.getState() == States.NEW) {
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

    private void clearSubTaskListForEpic(int epicId) {
        Epic epic = epicList.get(epicId);
        for (Integer subTaskId : epic.getChildSubTasks()) {
            subTaskList.remove(subTaskId);
            historyManager.remove(subTaskId);
        }
        //удалить все его подзадачи из списка дочерних подзадач
        epic.getChildSubTasks().clear();
    }

    private void deleteSubTaskList() {
        for (Integer subTaskId : subTaskList.keySet()) {
            historyManager.remove(subTaskId);
        }
        subTaskList.clear();
    }

    private int getNextId() {
        return ++currentId;
    }

    protected void setCurrentId(int id) {
        currentId = id;
    }

    protected Epic getEpicByIdWithoutHistory(int epicId) {
        return epicList.get(epicId);

    }

    protected Task getTaskByIdWithoutHistory(int taskId) {
        return taskList.get(taskId);
    }

    protected SubTask getSubTaskByIdWithoutHistory(int subTaskId) {
        return subTaskList.get(subTaskId);
    }

    protected HistoryManager getHistoryManager() {
        return historyManager;
    }
}


