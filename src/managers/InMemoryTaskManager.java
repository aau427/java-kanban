package managers;

import common.Managers;
import comparators.TaskByStartDateComparator;
import exception.LogicalErrorException;
import exception.ManagerSaveException;
import exception.TaskNotFoundException;
import history.HistoryManager;
import model.Epic;
import model.SubTask;
import model.Task;
import referencebook.States;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> taskList = new HashMap<>();
    private final Map<Integer, Epic> epicList = new HashMap<>();
    private final Map<Integer, SubTask> subTaskList = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final IntervalManager intervalManager = Managers.getDefaultIntervalManager();
    private Integer currentId = 0;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(new TaskByStartDateComparator());

    @Override
    public int createTask(Task task) throws LogicalErrorException {
        if (task.getId() != null) {
            throw new LogicalErrorException("При создании задачи указан ID, это работа  TaskManager-а!");
        }
        if (!task.hasValidFields()) {
            throw new LogicalErrorException("При создании задачи не прошли базовые проверки!");
        }
        int taskId = getNextId();
        Duration duration = task.getDuration() != null ? task.getDuration() : Duration.ZERO;
        return createTaskCommon(new Task(taskId, task.getName(), task.getDescription(), task.getState(), task.getStartTime(),
                duration));
    }

    protected int createTaskCommon(Task task) {
        if (task.getStartTime() != null) {
            occupyIntervalsForTask(task);
            prioritizedTasks.add(task);
        }
        taskList.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public int updateTask(Task task) throws TaskNotFoundException, LogicalErrorException {
        if (task.getId() == null) {
            throw new TaskNotFoundException("При обновлении задачи не указан ее Id, не могу понять, что менять!");
        }
        if (!task.hasValidFields()) {
            throw new LogicalErrorException("При обновлении задачи не прошли базовые проверки!");
        }
        Task oldTask = getTaskByIdWithoutHistory(task.getId());
        if (oldTask == null) {
            throw new TaskNotFoundException("При обновлении " + task.getId() + " не нашел ее в списке!");
        }
        restoreIntervalsForTask(oldTask);
        return createTaskCommon(new Task(task.getId(), task.getName(), task.getDescription(), task.getState(),
                task.getStartTime(),
                task.getDuration()));
    }

    @Override
    public int createEpic(Epic epic) throws LogicalErrorException {
        if (epic.getId() != null) {
            throw new LogicalErrorException("При создании Эпика указан Id! это работа TaskManager-а!");
        }
        checkCalculatedFieldsForNewEpic(epic);

        if (!epic.hasValidFields()) {
            throw new LogicalErrorException("При создании Эпика не прошли базовые проверки");
        }
        int epicId = getNextId();
        return createEpicCommon(new Epic(epicId, epic.getName(), epic.getDescription()));
    }

    protected int createEpicCommon(Epic epic) {
        epicList.put(epic.getId(), epic);
        return epic.getId();
    }

    //пользователь может менять наименование и комментарий к эпику, остальное считается от подзадач
    @Override
    public int updateEpic(Epic epic) throws TaskNotFoundException, LogicalErrorException {
        if (epic.getId() == null) {
            throw new TaskNotFoundException("При обновлении Эпика не указан его ID! не могу понять, что менять!");
        }
        if (!epic.hasValidFields()) {
            throw new LogicalErrorException("При обновлении Эпика не прошли базовые проверки!");
        }
        Epic oldEpic = epicList.get(epic.getId());
        if (oldEpic == null) {
            String errorMessage = String.format("Ошибка: не нашел Epic № %d в списке  при его обновлении!", epic.getId());
            throw new TaskNotFoundException(errorMessage);
        }
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());
        return epic.getId();
    }

    @Override
    public int createSubTask(SubTask subTask) throws LogicalErrorException {
        if (subTask.getId() != null) {
            throw new LogicalErrorException("При создании подзадачи указан Id! это работа TaskManager-а!");
        }
        if (!subTask.hasValidFields()) {
            throw new LogicalErrorException("Не могу породить/изменить подзадачу: не прошли базовые проверки!");
        }
        int parentEpicId = subTask.getParentEpic();
        /*Судя по ТЗ в историю пишем, если кто-то "смотрит" эпик
         Нам здесь не нужно записывать в историю, т.к. это чисто техническое получение Епика*/
        Epic epic = getEpicByIdWithoutHistory(parentEpicId);
        if (epic == null) {
            throw new TaskNotFoundException("Не могу породить/изменить подзадачу! Не нашел родительский Эпик № " + parentEpicId);
        }
        int subTaskId = getNextId();
        epic.getChildSubTasks().add(subTaskId);
        SubTask subTaskToInsert = new SubTask(subTaskId, subTask.getName(), subTask.getDescription(),
                subTask.getState(), subTask.getParentEpic(), subTask.getStartTime(), subTask.getDuration());
        return createSubTaskCommon(subTaskToInsert);
    }

    public int createSubTaskCommon(SubTask subTask) {
        occupyIntervalsForTask(subTask);
        prioritizedTasks.add(subTask);
        subTaskList.put(subTask.getId(), subTask);
        Epic epic = getEpicByIdWithoutHistory(subTask.getParentEpic());
        calcStateAndTimeForEpic(epic);
        return subTask.getId();
    }

    private void calcStateAndTimeForEpic(Epic epic) {
        LocalDateTime starTime = null;
        LocalDateTime endTime = null;
        Duration duration = Duration.ofDays(0);
        List<Integer> subTaskList = epic.getChildSubTasks();
        if (subTaskList == null) {
            epic.setState(States.NEW);
            epic.setStartTime(starTime);
            epic.setDuration(duration);
            epic.setEndTime(endTime);
            return;
        }
        boolean isAllSubTasksAreDone = true;
        boolean isAllSubTasksAreNew = true;
        for (Integer index : subTaskList) {
            SubTask subTask = getSubTaskByIdWithoutHistory(index);
            if ((isAllSubTasksAreDone) || (isAllSubTasksAreNew)) {
                if (subTask.getState() == States.DONE) {
                    isAllSubTasksAreNew = false;
                } else if (subTask.getState() == States.NEW) {
                    isAllSubTasksAreDone = false;
                } else {
                    isAllSubTasksAreDone = false;
                    isAllSubTasksAreNew = false;
                }
            }
            if (starTime == null) {
                starTime = subTask.getStartTime();
                endTime = subTask.getEndTime();
            } else {
                if (subTask.getStartTime().isBefore(starTime)) {
                    starTime = subTask.getStartTime();
                }
                if (subTask.getEndTime().isAfter(endTime)) {
                    endTime = subTask.getEndTime();
                }
            }
            duration = duration.plus(subTask.getDuration());
        }
        epic.setStartTime(starTime);
        epic.setDuration(duration);
        epic.setEndTime(endTime);
        if (isAllSubTasksAreNew) {
            epic.setState(States.NEW);
        } else if (isAllSubTasksAreDone) {
            epic.setState(States.DONE);
        } else {
            epic.setState(States.IN_PROGRESS);
        }
    }

    @Override
    public int updateSubTask(SubTask subTask) throws ManagerSaveException {
        if (subTask.getId() == null) {
            throw new TaskNotFoundException("Не указан ID подзадачи! не могу понять, что менять!");
        }
        if (!subTask.hasValidFields()) {
            throw new LogicalErrorException("Не могу породить/изменить подзадачу: не прошла базовые проверки!");
        }
        SubTask oldSubtask = subTaskList.get(subTask.getId());
        if (oldSubtask == null) {
            String errorMessage = String.format("Не нашел подзадачу № %d в списке  при ее обновлении!", subTask.getId());
            throw new TaskNotFoundException(errorMessage);
        }
        int parentEpicId = subTask.getParentEpic();
        Epic epic = getEpicByIdWithoutHistory(parentEpicId);
        if (epic == null) {
            String errorMessage = String.format("Не могу породить/изменить подзадачу №%d! Не нашел ee Эпик № %d",
                    subTask.getId(), parentEpicId);
            throw new TaskNotFoundException(errorMessage);
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
            calcStateAndTimeForEpic(oldEpic);
            epic.getChildSubTasks().add(subTask.getId());
        }
        restoreIntervalsForTask(oldSubtask);
        prioritizedTasks.remove(oldSubtask);
        return createSubTaskCommon(subTask.getSubTaskCopy());
    }

    @Override
    public void deleteTaskById(int taskId) {
        Task task = getTaskByIdWithoutHistory(taskId);
        if (task == null) {
            throw new TaskNotFoundException("Задача " + taskId + " не найдена!");
        }
        historyManager.remove(taskId);
        task = taskList.remove(taskId);
        prioritizedTasks.remove(task);
        restoreIntervalsForTask(task);
    }

    @Override
    public void deleteSubTaskById(int subTaskId) {
        SubTask subTask = getSubTaskByIdWithoutHistory(subTaskId);
        if (subTask == null) {
            throw new TaskNotFoundException("Не могу найти подзадачу " + subTaskId);
        }
        historyManager.remove(subTaskId);
        subTask = subTaskList.remove(subTaskId);
        prioritizedTasks.remove(subTask);
        restoreIntervalsForTask(subTask);
        Epic epic = epicList.get(subTask.getParentEpic());
        //remove не по Index, а по Object!
        epic.getChildSubTasks().remove(Integer.valueOf(subTaskId));
        calcStateAndTimeForEpic(epic);
    }

    @Override
    public void deleteEpicById(int epicId) {
        Epic epic = getEpicByIdWithoutHistory(epicId);
        if (epic == null) {
            throw new TaskNotFoundException("Не могу найти эпик " + epicId);
        }
        clearSubTaskListForEpic(epicId);
        epicList.remove(epicId);
        historyManager.remove(epicId);
    }

    @Override
    public void deleteAllTasks() {
        getTaskList().forEach(task -> {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
            restoreIntervalsForTask(task);
        });
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
            calcStateAndTimeForEpic(epic);
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
        } else {
            throw new TaskNotFoundException("Не найдена задача " + taskId);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int epicId) {
        Epic epic = epicList.get(epicId);
        if (epic != null) {
            historyManager.addTask(epic);
            return epic.getEpicCopy();
        } else {
            throw new TaskNotFoundException("Не найден эпик " + epicId);
        }
    }

    @Override
    public SubTask getSubTaskById(int subTaskId) {
        SubTask subTask = subTaskList.get(subTaskId);
        if (subTask != null) {
            historyManager.addTask(subTask);
        } else {
            throw new TaskNotFoundException("Не найдена подзадача " + subTaskId);
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

    @Override
    public List<SubTask> getEpicSubtasks(int epicId) {
        return getEpicByIdWithoutHistory(epicId)
                .getChildSubTasks()
                .stream()
                .map(subTaskList::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<Task>(prioritizedTasks);
    }


    private void clearSubTaskListForEpic(int epicId) {
        Epic epic = epicList.get(epicId);
        getAllSubTaskForEpic(epic).forEach(subTask -> {
            subTaskList.remove(subTask.getId());
            prioritizedTasks.remove(subTask);
            historyManager.remove(subTask.getId());
            restoreIntervalsForTask(subTask);
        });
        epic.getChildSubTasks().clear();
    }

    private void deleteSubTaskList() {
        getSubTaskList().forEach(subTask -> {
            historyManager.remove(subTask.getId());
            prioritizedTasks.remove(subTask);
            restoreIntervalsForTask(subTask);
        });
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

    private void checkCalculatedFieldsForNewEpic(Epic epic) throws LogicalErrorException {
        if (epic.getStartTimeOptional().isPresent()) {
            throw new LogicalErrorException("При создании эпика указан startDate! Это работа TaskManager-a");
        }
        if (epic.getEndTimeOptional().isPresent()) {
            throw new LogicalErrorException("При создании эпика указан endDate! Это работа TaskManager-a");
        }
    }

    private void occupyIntervalsForTask(Task task) {
        intervalManager.occupyIntervals(task.getStartTime(), task.getEndTime());
    }

    private void restoreIntervalsForTask(Task task) {
        if (task.getStartTime() == null) {
            return;
        }
        intervalManager.restoreIntervals(task.getStartTime(), task.getEndTime());
    }
}