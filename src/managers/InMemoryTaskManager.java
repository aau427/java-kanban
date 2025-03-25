package managers;

import model.Epic;
import model.SubTask;
import model.Task;
import referencebook.States;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> taskList = new HashMap<>();
    private final HashMap<Integer, Epic> epicList = new HashMap<>();
    private final HashMap<Integer, SubTask> subTaskList = new HashMap<>();
    private Integer currentId = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    @Override
    public boolean createTask(Task task) {
        if (task.getId() != null) {
            System.out.println("Ошибка! При создании задачи указан ID, это работа  TaskManager-а!");
            return false;
        }
        if (!task.isValid()) {
            return false;
        }
        setIdToTask(task);
        taskList.put(task.getId(), task);
        return true;
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
    public boolean createEpic(Epic epic) {
        if (epic.getId() != null) {
            System.out.println("Ошибка! При создании Эпика. Указан Id! это работа TaskManager-а!");
            return false;
        }
        if (!epic.isValid()) {
            return false;
        }
        setIdToTask(epic);
        epic.setState(States.NEW);
        epicList.put(epic.getId(), epic);
        return true;
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
    public boolean createSubTask(SubTask subTask) {
        if (subTask.getId() != null) {
            System.out.println("Ошибка! При создании подзадачи указан Id! это работа TaskManager-а!");
            return false;
        }
        if (!subTask.isValid()) {
            System.out.println("Не могу породить/изменить подзадачу: не прошли базовые проверки!");
            return false;
        }
        int parentEpicId = subTask.getParentEpic();
        /*Судя по ТЗ в историю пишем, если кто-то "смотрит" эпик
         Нам здесь не нужно записывать в историю, т.к. это чисто техническое получение Епика*/
        Epic epic = getEpicByIdWithoutHistory(parentEpicId);
        if (epic == null) {
            System.out.println("Не могу породить/изменить подзадачу! Не нашел родительский Эпик № " + parentEpicId);
            return false;
        }
        setIdToTask(subTask);
        epic.getChildSubTasks().add(subTask.getId());
        subTaskList.put(subTask.getId(), subTask);
        setState(epic);
        return true;
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
        }
        epic.getChildSubTasks().add(subTask.getId());
        setState(epic);
        subTaskList.put(subTask.getId(), subTask);
        return true;
    }

    @Override
    public void deleteTaskById(int taskId) {
        taskList.remove(taskId);
    }

    @Override
    public void deleteSubTaskById(int subTaskId) {
        SubTask subTask = subTaskList.remove(subTaskId);
        Epic epic = epicList.get(subTask.getParentEpic());
        //remove не по Index, а по Object!
        epic.getChildSubTasks().remove(Integer.valueOf(subTaskId));
        setState(epic);
    }

    @Override
    public void deleteEpicById(int epicId) {
        clearSubTaskListForEpic(epicId);
        epicList.remove(epicId);
    }

    @Override
    public void deleteAllTasks() {
        taskList.clear();
    }

    @Override
    public void deleteAllEpics() {
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
         if(task != null) {
             historyManager.addTask(task);
         }
        return task;
    }

    @Override
    public Epic getEpicById(int epicId) {
        Epic epic = epicList.get(epicId);
        if(epic != null) {
            historyManager.addTask(epic);
        }
        return epic;
    }

    @Override
    public SubTask getSubTaskById(int subTaskId) {
        SubTask subTask = subTaskList.get(subTaskId);
        if(subTask != null) {
            historyManager.addTask(subTask);
        }
        return subTask;
    }

    @Override
    public ArrayList<Task> getTaskList() {
        return new ArrayList<>(taskList.values());
    }

    @Override
    public ArrayList<Epic> getEpicList() {
        return new ArrayList<>(epicList.values());
    }

    @Override
    public ArrayList<SubTask> getSubTaskList() {
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

    private void setIdToTask(Task task) {
        task.setId(getNextId());
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
        }
        //удалить все его подзадачи из списка дочерних подзадач
        epic.getChildSubTasks().clear();
    }

    private void deleteSubTaskList() {
        subTaskList.clear();
    }

    private int getNextId() {
        return ++currentId;
    }

    private Epic getEpicByIdWithoutHistory(int epicId) {
        return epicList.get(epicId);

    }

}


