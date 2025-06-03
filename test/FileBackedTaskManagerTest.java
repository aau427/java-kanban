import common.Managers;
import managers.FileBackedTaskManager;
import managers.TaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.*;
import referencebook.States;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//Дополнительно для FileBackedTasksManager — проверка работы по сохранению и восстановлению состояния.

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private final static String prefix = "testdata";
    private final static String suffix = "csv";
    private static String fileName;


    @BeforeAll
    public static void beforeAll() {
        File file = null;
        try {
            file = File.createTempFile("test", "csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileName = file.getName();
    }

    @BeforeEach
    public void beforeEach() {
        taskManager = new FileBackedTaskManager(fileName);
    }

    @AfterEach
    public void afterEach() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
    }

    @Test
    public void shouldEmptyFileLoadCorrect() {
        assertTrue(taskManager.getEpicList().isEmpty());
        assertTrue(taskManager.getTaskList().isEmpty());
        assertTrue(taskManager.getSubTaskList().isEmpty());
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    @DisplayName("Эпик без истории загружается корректно")
    public void shouldLoadCorrectEpicWithoutSubTasks() {
        Epic epic = new Epic("Эпик", "Эпик без подзадач");
        int id = taskManager.createEpic(epic);
        TaskManager manager1 = new FileBackedTaskManager(fileName);

        assertTrue(manager1.getEpicList().size() == 1);
        assertEquals(id, manager1.getEpicList().get(0).getId());
        assertTrue(manager1.getSubTaskList().isEmpty());
        assertTrue(manager1.getHistory().size() == 0, "История не должны подгружаться!");
    }

    @Test
    @DisplayName("Эпик с историей загружается корректно")
    public void shouldLoadHistory() {
        Epic epic = new Epic("Эпик", "Эпик без подзадач");

        int id = taskManager.createEpic(epic);
        epic = taskManager.getEpicById(id);

        TaskManager manager1 = new FileBackedTaskManager(fileName);

        assertTrue(manager1.getEpicList().size() == 1);
        assertEquals(id, manager1.getEpicList().get(0).getId());
        assertTrue(manager1.getSubTaskList().isEmpty());
        assertTrue(manager1.getHistory().size() == 1, "История некорректно загрузилась!");
        assertEquals(id, manager1.getHistory().getFirst().getId(), "В истории не тот эпик!");
    }

    @Test
    @DisplayName("Задача загружается корректно")
    public void shouldLoadCorrectTask() {
        DateTimeFormatter dateTimeFormatter = Managers.getDefaultDateTimeFormatter();
        LocalDateTime startDateTime = LocalDateTime.parse("01.01.2025 00:00", dateTimeFormatter);
        Duration duration = Duration.ofDays(3);
        Task task = new Task("Задача", "Просто задача", States.NEW,
                startDateTime.plusDays(1), duration.plusDays(1));

        int taskId = taskManager.createTask(task);

        TaskManager manager1 = new FileBackedTaskManager(fileName);

        assertTrue(manager1.getTaskList().size() == 1, "Задачи не загрузились");
        assertEquals(taskId, manager1.getTaskList().getFirst().getId(), "Не та подзадача загрузилась");
    }

    @Test
    @DisplayName("Подзадача загружаются корректно")
    public void shouldLoadCorrectSubTask() {
        Epic epic = new Epic("Эпик", "Эпик");
        int epicId = taskManager.createEpic(epic);
        DateTimeFormatter dateTimeFormatter = Managers.getDefaultDateTimeFormatter();
        LocalDateTime startDateTime = LocalDateTime.parse("01.01.2025 00:00", dateTimeFormatter);
        Duration duration = Duration.ofDays(3);
        SubTask subTask = new SubTask("Подзадача", "Подзадача для Эпика №1", States.NEW, epicId,
                startDateTime, duration);

        int subTaskId = taskManager.createSubTask(subTask);
        epic = taskManager.getEpicById(epicId);

        TaskManager manager1 = new FileBackedTaskManager(fileName);

        assertTrue(manager1.getEpicList().size() == 1, "Эпики не загрузились");
        assertEquals(epicId, manager1.getEpicList().getFirst().getId(), "Не тот Эпик загрузился");
        assertTrue(manager1.getSubTaskList().size() == 1, "Подзадачи не загрузились");
        assertEquals(subTaskId, manager1.getSubTaskList().getFirst().getId(), "Не та подзадача загрузилась");
    }

    @Test
    @DisplayName("Задачи, подзадачи и эпики загружаются корректно")
    public void shouldLoadCorrectSomeTasks() {
        Epic epic = new Epic("Эпик", "Эпик");
        int epicId = taskManager.createEpic(epic);
        DateTimeFormatter dateTimeFormatter = Managers.getDefaultDateTimeFormatter();
        SubTask subTask = new SubTask("Подзадача", "Подзадача для Эпика №1", States.NEW, epicId,
                LocalDateTime.of(2025, Month.APRIL, 1, 0, 0), Duration.ofDays(2));
        Task task = new Task("Задача", "Просто задача", States.NEW,
                LocalDateTime.of(2025, Month.MAY, 24, 0, 0), Duration.ofDays(10));

        int subTaskId = taskManager.createSubTask(subTask);
        int taskId = taskManager.createTask(task);
        epic = taskManager.getEpicById(epicId);

        TaskManager manager1 = new FileBackedTaskManager(fileName);

        assertTrue(manager1.getEpicList().size() == 1, "Эпики не загрузились");
        assertEquals(epicId, manager1.getEpicList().getFirst().getId(), "Не тот Эпик загрузился");
        assertTrue(manager1.getSubTaskList().size() == 1, "Подзадачи не загрузились");
        assertEquals(subTaskId, manager1.getSubTaskList().getFirst().getId(), "Не та подзадача загрузилась");
        assertTrue(manager1.getTaskList().size() == 1, "Задачи не загрузились");
        assertEquals(taskId, manager1.getTaskList().getFirst().getId(), "Не та подзадача загрузилась");
        assertTrue(manager1.getHistory().size() == 1, "История не загрузилась");
        assertEquals(epicId, manager1.getHistory().getFirst().getId(), "Не тот эпик в истории");
    }

    @Test
    @DisplayName("Задача с пустым startTine загружается корректно")
    public void shouldTaskWithNullStartTimeLoadCorrect() {
        Task task = new Task("Задача", "Комментарий", States.IN_PROGRESS,
                null, Duration.ofDays(1));
        int taskId = taskManager.createTask(task);

        TaskManager manager1 = new FileBackedTaskManager(fileName);
        Task task1 = manager1.getTaskById(taskId);

        assertTrue(manager1.getTaskList().size() == 1, "Боль с TaskList");
        assertEquals(taskId, manager1.getTaskList().getFirst().getId(), "Не та задача загрузилась");
        assertTrue(manager1.getPrioritizedTasks().size() == 0,
                "Боль со списком приоритезированных задач");
    }

}