package test;

import common.Managers;
import history.HistoryManager;
import history.InMemoryHistoryManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import referencebook.States;
import taskmanager.InMemoryTaskManager;
import taskmanager.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefaultTaskManager();
    }

    @DisplayName("Экземпляры Task равны друг другу, если равен их Id")
    @Test
    public void shouldTasksInstancesEqualsIfIdsIsEquals() {
        Task task = createOneTask();

        int taskId = taskManager.createTask(task);
        Task task1 = taskManager.getTaskById(taskId);

        assertEquals(task.getName(), task1.getName(), "Не равны наименования");
        assertEquals(task.getDescription(), task1.getDescription(), "Не равны комментарии");
        assertEquals(task.getState(), task1.getState(), "Не совпали статусы");
    }

    @DisplayName("Экземпляры Epic равны друг другу, если равен их Id")
    @Test
    public void shouldEpicsInstancesEqualsIfIdsIsEquals() {
        Epic epic = new Epic("Эпик1", "Коммент к эпику 1");

        int epicId = taskManager.createEpic(epic);
        Epic epic1 = taskManager.getEpicById(epicId);

        assertEquals(epic.getName(), epic1.getName(), "Не равны наименования");
        assertEquals(epic.getDescription(), epic1.getDescription(), "Не равны комментарии");
        assertEquals(epic.getState(), epic1.getState(), "Не совпали статусы");
    }

    @DisplayName("Экземпляры SubTask равны друг другу, если равен их Id")
    @Test
    public void shouldSubTasksInstancesEqualsIfIdsIsEquals() {
        Epic epic = createOneEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask = createOneSubtask(epicId);
        int subTaskId = taskManager.createSubTask(subTask);

        SubTask subTask1 = taskManager.getSubTaskById(subTaskId);

        assertEquals(subTask.getName(), subTask1.getName(), "Не равны наименования задач");
        assertEquals(subTask.getDescription(), subTask1.getDescription(), "Не равны комментарии");
        assertEquals(subTask.getState(), subTask1.getState(), "Не совпали комментарии");
        assertEquals(subTask.getState(), subTask1.getState(), "Не совпали статусы");
    }

    @DisplayName("SubTask нельзя сделать своим же эпиком")
    @Test
    public void shouldNotSubTaskMakeItOwnEpic() {
        Epic epic = createOneEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask = createOneSubtask(epicId);
        int subTaskId = taskManager.createSubTask(subTask);
        subTask = new SubTask(subTaskId, subTask.getName(), subTask.getDescription(), subTask.getState(), subTaskId);

        boolean isDone = taskManager.updateSubTask(subTask);

        assertFalse(isDone, "Сделал подзадачу своим же эпиком!");
    }

    @DisplayName("Эпик нельзя сделать своей же подзадачей")
    @Test
    public void shouldNotEpicMakeIdOwnSubTask() {
        Epic epic = createOneEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask = createOneSubtask(epicId);
        taskManager.createSubTask(subTask);
        boolean isDone = taskManager.updateSubTask(subTask);

        assertFalse(isDone, "Эпик нельзя сделать своей же подзадачей!");
    }

    @DisplayName("Managers  возвращает проинициализированный и готовый к работе HistoryManager")
    @Test
    public void shouldManagersReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "HistoryManager не проинициализирован");
        assertEquals(InMemoryHistoryManager.class, historyManager.getClass(), "Объект не того класса");

    }

    @DisplayName("Managers  возвращает проинициализированный и готовый к работе TaskManager")
    @Test
    public void shouldManagersReturnInitializedTaskManager() {
        TaskManager taskManager = Managers.getDefaultTaskManager();

        assertNotNull(taskManager, "TaskManager не проинициализирован");
        assertEquals(InMemoryTaskManager.class, taskManager.getClass(), "Объект не того класса");

    }

    @DisplayName("TaskManager действительно добавляет задачу")
    @Test
    public void shouldTaskManagerAddTask() {
        Task task = createOneTask();

        int taskId = taskManager.createTask(task);

        Task task1 = taskManager.getTaskById(taskId);

        assertEquals(taskId, (int) task1.getId(), "Задача 1 не порождена!");
    }

    @DisplayName("TaskManager действительно добавляет Эпик")
    @Test
    public void shouldTaskManagerAddEpic() {
        Epic epic1 = createOneEpic();

        int epic1Id = taskManager.createEpic(epic1);

        Epic epic3 = taskManager.getEpicById(epic1Id);

        assertEquals(epic1Id, (int) epic3.getId(), "Эпик 1 не порожден!");
    }

    @DisplayName("TaskManager действительно добавляет подзадачу")
    @Test
    public void shouldTaskManagerAddSubTask() {
        Epic epic1 = new Epic("Эпик1", "Комментарий к Эпику 1");
        Epic epic2 = new Epic("Эпик2", "Комментарий к Эпику 2");

        int epic1Id = taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);

        SubTask subTask = createOneSubtask(epic1Id);
        int subTaskId = taskManager.createSubTask(subTask);

        SubTask subTask1 = taskManager.getSubTaskById(subTaskId);

        assertEquals(subTask1.getId(), subTaskId, "Подзадача 1 не порождена!");
    }

    @DisplayName("Задача с заданным ID и сгенерированным ID не конфликтуют внутри менеджера")
    @Test
    public void shouldTasksWithSetIdAndGeneratedIdWillNotConflict() {
        Task task1 = createOneTask();
        int task1Id = taskManager.createTask(task1);

        Task task2 = new Task(task1Id, "Какая-то другая задача", "Потихоньку едет крыша!!!!", States.DONE);

        int task2Id = taskManager.createTask(task2);

        assertNotEquals(task1Id, task2Id, "Задачи конфликтуют внутри TaskManager!");
        assertNotEquals(task1.getId(), task2.getId(), "Задачи конфликтуют внутри TaskManager!");
    }

    @DisplayName("Задача, добавляемая в HistoryManager, сохраняют предыдущую версию задачи  и ее данных")
    @Test
    public void shouldTaskAddToHistoryAndSavePrevState() {
        Task task = createOneTask();
        int taskId = taskManager.createTask(task);

        task = taskManager.getTaskById(taskId);

        Task task2 = new Task(taskId, "Изменил наименование", "Изменил комментарий", States.DONE);


        taskManager.updateTask(task2);

        Task actualTask = taskManager.getHistory().getLast();

        assertEquals(actualTask.getId(), task.getId(), "неправильный ID в истории");
        assertEquals(actualTask.getName(), task.getName(), "Неправильное имя в истории");
        assertEquals(actualTask.getDescription(), task.getDescription(), "Неправильный комментарий в истории");
        assertEquals(actualTask.getState(), task.getState(), "Неправильное состояние в истории");
    }

    @DisplayName("Статус эпика автоматически перерасcчитывается")
    @Test
    public void shouldEpicStateAutoRecalc() {
        Epic epic = createOneEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask = createOneSubtask(epicId);
        int subTaskId = taskManager.createSubTask(subTask);

        SubTask updateSubTask = new SubTask(subTaskId, subTask.getName(), subTask.getDescription(),
                States.DONE, subTask.getParentEpic());
        taskManager.updateSubTask(updateSubTask);

        Epic epic1 = taskManager.getEpicById(epicId);

        assertEquals(States.DONE, epic1.getState(), "Статус Эпика не перерассчитался");
    }

    @DisplayName("Подзадача с несуществующим эпиком не порождается")
    @Test
    public void shouldNotCreateSubTaskWithIncorrectEpic() {
        int epicId = 10000000;
        SubTask subTask = createOneSubtask(epicId);
        int isCreate = taskManager.createSubTask(subTask);
        assertNotEquals(1, isCreate, "Подзадача с несуществующим эпиком породилась!");
        assertTrue(taskManager.getSubTaskList().isEmpty(), "Подзадача с несуществующим эпиком породилась");

    }

    @DisplayName("Внутри эпика не остается неактуальных подзадач")
    @Test
    public void shouldEpicCorrectAfterSubTaskDelete() {
        Epic epic = createOneEpic();
        int epicId = taskManager.createEpic(epic);

        SubTask subTask1 = createOneSubtask(epicId);
        int subTask1Id = taskManager.createSubTask(subTask1);

        SubTask subTask2 = createOneSubtask(epicId);
        taskManager.createSubTask(subTask2);

        taskManager.deleteSubTaskById(subTask1Id);

        epic = taskManager.getEpicById(epicId);

        Assertions.assertEquals(1, epic.getChildSubTasks().size());
        //Assertions.assertNotEquals(2, taskManager.getSubTaskList().getFirst().getId());
    }

    @DisplayName("При удалении задачи она удаляется из истории")
    @Test
    public void shouldTaskRemoveFromHistoryIfDelete() {
        Task task1 = new Task("Задача1", "Комментарий1", States.IN_PROGRESS);
        int task1Id = taskManager.createTask(task1);

        Task task2 = new Task("Задача2", "Комментарий2", States.DONE);
        Integer task2Id = taskManager.createTask(task2);

        taskManager.getTaskById(task1Id);
        taskManager.getTaskById(task2Id);

        taskManager.deleteTaskById(task1Id);

        assertEquals(1, taskManager.getHistory().size(), "Не удалилась!");
        assertEquals(task2Id, taskManager.getHistory().getFirst().getId(), "Удалилась, но не та!");
    }

    @DisplayName("При удалении всех задач они удаляются из истории")
    @Test
    public void shouldHistoryClearIfTaskListClear() {
        Task task1 = new Task("Задача1", "Комментарий1", States.IN_PROGRESS);
        int task1Id = taskManager.createTask(task1);

        Task task2 = new Task("Задача2", "Комментарий2", States.DONE);
        int task2Id = taskManager.createTask(task2);

        taskManager.getTaskById(task1Id);
        taskManager.getTaskById(task2Id);

        taskManager.deleteAllTasks();

        assertEquals(0, taskManager.getHistory().size(), "Не удалилась!");
    }

    @DisplayName("Статус Эпика New, если все подзадачи New")
    @Test
    public void shouldEpicNewIfAllSubTaskNew() {
        Epic epic = new Epic("Эпик1", "Комментарий к эпику 1");
        int epicId = taskManager.createEpic(epic);

        SubTask subTask1 = new SubTask("Сабтаск1", "Комментарий", States.NEW, epicId);
        SubTask subTask2 = new SubTask("Сабтаск2", "Комментарий", States.NEW, epicId);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        epic = taskManager.getEpicById(epicId);

        assertEquals(States.NEW, epic.getState(), "Статус эпика неправильный!");
    }

    @DisplayName("Статус Эпика In_Progres, если хотя бы одна подзадача IN_Progress")
    @Test
    public void shouldEpicInProgressIfOneSubTaskInProgres() {
        Epic epic = new Epic("Эпик1", "Комментарий к эпику 1");
        int epicId = taskManager.createEpic(epic);

        SubTask subTask1 = new SubTask("Сабтаск1", "Комментарий", States.NEW, epicId);
        SubTask subTask2 = new SubTask("Сабтаск2", "Комментарий", States.IN_PROGRESS, epicId);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        epic = taskManager.getEpicById(epicId);

        assertEquals(States.IN_PROGRESS, epic.getState(), "Статус эпика неправильный!");
    }

    @DisplayName("Статус Эпика Done, если все подзадачи Done")
    @Test
    public void shouldEpicDoneIfAllSubTaskDone() {
        Epic epic = new Epic("Эпик1", "Комментарий к эпику 1");
        int epicId = taskManager.createEpic(epic);

        SubTask subTask1 = new SubTask("Сабтаск1", "Комментарий", States.DONE, epicId);
        SubTask subTask2 = new SubTask("Сабтаск2", "Комментарий", States.DONE, epicId);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        epic = taskManager.getEpicById(epicId);

        assertEquals(States.DONE, epic.getState(), "Статус эпика неправильный!");
    }

    @DisplayName("Статус Эпика New после удаления всех выполненных задач")
    @Test
    public void shouldEpicNewAfterSubTaskListCleer() {
        Epic epic = new Epic("Эпик1", "Комментарий к эпику 1");
        int epicId = taskManager.createEpic(epic);

        SubTask subTask1 = new SubTask("Сабтаск1", "Комментарий", States.DONE, epicId);
        SubTask subTask2 = new SubTask("Сабтаск2", "Комментарий", States.DONE, epicId);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        taskManager.deleteAllSubTasks();
        epic = taskManager.getEpicById(epicId);

        assertEquals(States.NEW, epic.getState(), "Статус эпика неправильный!");
    }

    @DisplayName("Статус Эпика New после удаления задачи IN_PROGRESS")
    @Test
    public void shouldEpicNewAfterDeleteSubTaskINProgress() {
        Epic epic = new Epic("Эпик1", "Комментарий к эпику 1");
        int epicId = taskManager.createEpic(epic);

        SubTask subTask1 = new SubTask("Сабтаск1", "Комментарий", States.NEW, epicId);
        SubTask subTask2 = new SubTask("Сабтаск2", "Комментарий", States.IN_PROGRESS, epicId);
        taskManager.createSubTask(subTask1);
        int subTask2Id = taskManager.createSubTask(subTask2);

        taskManager.deleteSubTaskById(subTask2Id);
        epic = taskManager.getEpicById(epicId);

        assertEquals(States.NEW, epic.getState(), "Статус эпика неправильный!");
    }

    @DisplayName("Смена статуса Эпика сеттером не влияет на состояние Манагера")
    @Test
    public void shouldManagerStateNotChahgeAfetSetStateForEpic() {
        Epic epic = new Epic("Эпик1", "Комментарий к эпику 1");
        int epicId = taskManager.createEpic(epic);

        SubTask subTask1 = new SubTask("Сабтаск1", "Комментарий", States.DONE, epicId);
        SubTask subTask2 = new SubTask("Сабтаск2", "Комментарий", States.DONE, epicId);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        epic = taskManager.getEpicById(epicId);
        epic.setState(States.NEW);

        epic = taskManager.getEpicById(epicId);

        assertEquals(States.DONE, epic.getState(), "Повлиял");
        assertEquals(States.DONE, taskManager.getEpicList().getFirst().getState(), "Повлиял!");
    }

    @DisplayName("Смена статуса Эпика сеттером по ссылке из листа не влияет на состояние Манагера")
    @Test
    public void shouldManagerStateNotChahgeAfetSetStateForEpicFromList() {
        Epic epic = new Epic("Эпик1", "Комментарий к эпику 1");
        int epicId = taskManager.createEpic(epic);

        SubTask subTask1 = new SubTask("Сабтаск1", "Комментарий", States.DONE, epicId);
        SubTask subTask2 = new SubTask("Сабтаск2", "Комментарий", States.DONE, epicId);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        epic = taskManager.getEpicList().getFirst();
        epic.setState(States.IN_PROGRESS);
        epic = taskManager.getEpicById(epicId);

        assertEquals(States.DONE, epic.getState(), "Повлиял!");
        assertEquals(States.DONE, taskManager.getEpicList().getFirst().getState(), "Повлиял!");
    }

    @DisplayName("История не содержит повторов")
    @Test
    public void shouldHistoryCorrectAfterRepeat() {
        Epic epic = new Epic("Эпик1", "Комментарий к эпику 1");
        int epicId = taskManager.createEpic(epic);

        SubTask subTask1 = new SubTask("Сабтаск1", "Комментарий", States.DONE, epicId);
        SubTask subTask2 = new SubTask("Сабтаск2", "Комментарий", States.DONE, epicId);
        int subTask1Id = taskManager.createSubTask(subTask1);
        int subTask2Id = taskManager.createSubTask(subTask2);

        taskManager.getEpicById(epicId);
        taskManager.getSubTaskById(subTask1Id);
        taskManager.getSubTaskById(subTask2Id);
        taskManager.getEpicById(epicId);
        taskManager.getSubTaskById(subTask1Id);

        assertEquals(3, taskManager.getHistory().size(), "История точно содержит повторы!");
        assertEquals(subTask2Id, taskManager.getHistory().get(0).getId(), "История формируется неверно!");
        assertEquals(epicId, taskManager.getHistory().get(1).getId(), "История формируется неверно!");
        assertEquals(subTask1Id, taskManager.getHistory().get(2).getId(), "История формируется неверно!");

    }

    private Task createOneTask() {
        return new Task("Задача1", "Коммент к задаче 1", States.NEW);
    }

    private Epic createOneEpic() {
        return new Epic("Эпик1", "Коммент к эпику 1");
    }

    private SubTask createOneSubtask(Integer epicId) {
        return new SubTask("Подзадача1", "Комментарий к подзадаче 1", States.NEW, epicId);
    }
}