package tests;

import managers.*;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import referencebook.States;

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

        taskManager.createTask(task);
        Task task1 = taskManager.getTaskById(task.getId());

        assertEquals(task.getName(), task1.getName(), "Не равны наименования");
        assertEquals(task.getDescription(), task1.getDescription(), "Не равны комментарии");
        assertEquals(task.getState(), task1.getState(), "Не совпали статусы");
    }

    @DisplayName("Экземпляры Epic равны друг другу, если равен их Id")
    @Test
    public void shouldEpicsInstancesEqualsIfIdsIsEquals() {
        Epic epic = new Epic("Эпик1", "Коммент к эпику 1");

        taskManager.createEpic(epic);
        Epic epic1 = taskManager.getEpicById(epic.getId());

        assertEquals(epic.getName(), epic1.getName(), "Не равны наименования");
        assertEquals(epic.getDescription(), epic1.getDescription(), "Не равны комментарии");
        assertEquals(epic.getState(), epic1.getState(), "Не совпали статусы");
    }

    @DisplayName("Экземпляры SubTask равны друг другу, если равен их Id")
    @Test
    public void shouldSubTasksInstancesEqualsIfIdsIsEquals() {
        Epic epic = createOneEpic();
        taskManager.createEpic(epic);

        SubTask subTask = createOneSubtask(epic.getId());
        taskManager.createSubTask(subTask);

        SubTask subTask1 = taskManager.getSubTaskById(subTask.getId());

        assertEquals(subTask.getName(), subTask1.getName(), "Не равны наименования задач");
        assertEquals(subTask.getDescription(), subTask1.getDescription(), "Не равны комментарии");
        assertEquals(subTask.getState(), subTask1.getState(), "Не совпали комментарии");
        assertEquals(subTask.getState(), subTask1.getState(), "Не совпали статусы");
    }

    @DisplayName("SubTask нельзя сделать своим же эпиком")
    @Test
    public void shouldNotSubTaskMakeItOwnEpic() {
        Epic epic = createOneEpic();
        taskManager.createEpic(epic);

        SubTask subTask = createOneSubtask(epic.getId());
        taskManager.createSubTask(subTask);
        subTask.setParentEpic(subTask.getId());

        boolean isDone = taskManager.updateSubTask(subTask);

        assertFalse(isDone, "Сделал подзадачу своим же эпиком!");
    }

    @DisplayName("Эпик нельзя сделать своей же подзадачей")
    @Test
    public void shouldNotEpicMakeIdOwnSubTask() {
        Epic epic = createOneEpic();
        taskManager.createEpic(epic);

        SubTask subTask = createOneSubtask(epic.getId());
        taskManager.createSubTask(subTask);

        subTask.setId(epic.getId());
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
        Task task1 = new Task("Задача2", "Комментарий к задаче 2", States.DONE);

        taskManager.createTask(task);
        taskManager.createTask(task1);

        Task task3 = taskManager.getTaskById(task.getId());

        assertTrue(task3.equals(task), "Задача 1 не порождена!");
    }

    @DisplayName("TaskManager действительно добавляет Эпик")
    @Test
    public void shouldTaskManagerAddEpic() {
        Epic epic = createOneEpic();
        Epic epic1 = new Epic("Эпик2", "Комментарий к Эпику 2");

        taskManager.createEpic(epic);
        taskManager.createEpic(epic1);

        Epic epic3 = taskManager.getEpicById(epic1.getId());

        assertTrue(epic1.equals(epic3), "Эпик 2 не порожден!");
    }

    @DisplayName("TaskManager действительно добавляет подзадачу")
    @Test
    public void shouldTaskManagerAddSubTask() {
        Epic epic = createOneEpic();
        Epic epic1 = new Epic("Эпик2", "Комментарий к Эпику 2");

        taskManager.createEpic(epic);
        taskManager.createEpic(epic1);

        SubTask subTask = createOneSubtask(epic.getId());
        taskManager.createSubTask(subTask);

        SubTask subTask1 = taskManager.getSubTaskById(subTask.getId());

        assertTrue(subTask1.equals(subTask), "Подзадача 1 не порождена!");
    }

    @DisplayName("Задача с заданным ID и сгенерированным ID не конфликтуют внутри менеджера")
    @Test
    public void ShouldTasksWithSetIdAndGeneratedIdWillNotConflict() {
        Task task = createOneTask();
        taskManager.createTask(task);

        Task task1 = new Task("Какая-то другая задача", "Потихоньку едет крыша!!!!", States.DONE);
        task1.setId(task.getId());

        taskManager.createTask(task1);

        assertFalse(task.getId() != task1.getId(), "Задачи конфликтуют внутри TaskManager!");

    }

    @DisplayName("Задача, добавляемая в HistoryManager, сохраняют предыдущую версию задачи  и ее данных")
    @Test
    public void shouldTaskAddToHistoryAndSavePrevState() {
        Task task = createOneTask();
        taskManager.createTask(task);

        Task task1 = taskManager.getTaskById(task.getId());

        Task task2 = new Task("Изменил наименование", "Изменил комментарий", States.DONE);
        task2.setId(task.getId());


        taskManager.updateTask(task2);

        Task actualTask = taskManager.getHistory().getLast();

        assertEquals(actualTask.getId(), task.getId(), "неправильный ID в истории");
        assertEquals(actualTask.getName(), task.getName(), "Неправильное имя в истории");
        assertEquals(actualTask.getDescription(), task.getDescription(), "Неправильный комментарий в истории");
        assertEquals(actualTask.getState(), task.getState(), "Неправильное состояние в истории");
    }

    @DisplayName("Статус эпика автоматически перерассчитывается")
    @Test
    public void shouldEpicStateAutoRecalc() {
        Epic epic = createOneEpic();
        taskManager.createEpic(epic);

        SubTask subTask = createOneSubtask(epic.getId());
        taskManager.createSubTask(subTask);

        subTask.setState(States.DONE);
        taskManager.updateSubTask(subTask);

        Epic epic1 = taskManager.getEpicById(epic.getId());

        assertEquals(States.DONE, epic1.getState(), "Статус Эпика не перерассчитался");
    }

    @DisplayName("Подзадача с несуществующим эпиком не порождается")
    @Test
    public void shouldNotCreateSubTaskWithIncorrectEpic() {
        int epicId = 10000000;
        SubTask subTask = createOneSubtask(epicId);
        boolean isCreate = taskManager.createSubTask(subTask);
        assertFalse(isCreate, "Подзадача с несуществующим эпиком породилась!");
        assertTrue(taskManager.getSubTaskList().isEmpty(), "Подзадача с несуществующим эпиком породилась");

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