import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.*;
import referencebook.States;
import taskmanager.FileBackedTaskManager;
import taskmanager.TaskManager;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//Дополнительно для FileBackedTasksManager — проверка работы по сохранению и восстановлению состояния.

class FileBackedTaskManagerTest {
    private final static String prefix = "testdata";
    private final static String suffix = "csv";
    private static TaskManager manager;
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
        manager = new FileBackedTaskManager(fileName);
    }

    @AfterEach
    public void afterEach() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
    }

    @Test
    public void shouldEmptyFileLoadCorrect() {
        assertTrue(manager.getEpicList().isEmpty());
        assertTrue(manager.getTaskList().isEmpty());
        assertTrue(manager.getSubTaskList().isEmpty());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    @DisplayName("Эпик без истории загружается корректно")
    public void shouldLoadCorrectEpicWithoutSubTasks() {
        Epic epic = new Epic("Эпик", "Эпик без подзадач");
        int id = manager.createEpic(epic);
        TaskManager manager1 = new FileBackedTaskManager(fileName);
        Epic epic1 = manager1.getEpicById(1);

        assertTrue(manager1.getEpicList().size() == 1);
        assertEquals(id, manager1.getEpicList().get(0).getId());
        assertTrue(manager1.getSubTaskList().isEmpty());
        assertTrue(manager1.getHistory().size() == 0, "История не должны подгружаться!");
    }

    @Test
    @DisplayName("Эпик с историей загружается корректно")
    public void shouldLoadHistory() {
        Epic epic = new Epic("Эпик", "Эпик без подзадач");

        int id = manager.createEpic(epic);
        epic = manager.getEpicById(id);

        TaskManager manager1 = new FileBackedTaskManager(fileName);

        assertTrue(manager1.getEpicList().size() == 1);
        assertEquals(id, manager1.getEpicList().get(0).getId());
        assertTrue(manager1.getSubTaskList().isEmpty());
        assertTrue(manager1.getHistory().size() == 1, "История некорректно загрузилась!");
        assertEquals(id, manager1.getHistory().getFirst().getId(), "В истории не тот эпик!");
    }

    @Test
    @DisplayName("Задачи, подзадачи и эпики загружаются корректно")
    public void shouldLoadCorrectSomeTasks() {
        Epic epic = new Epic("Эпик", "Эпик");
        SubTask subTask = new SubTask("Подхадача", "Подзадача для Эпика №1", States.NEW, 1);
        Task task = new Task("Задача", "Просто задача", States.NEW);

        int epicId = manager.createEpic(epic);
        int subTaskId = manager.createSubTask(subTask);
        int taskId = manager.createTask(task);
        epic = manager.getEpicById(epicId);

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
}