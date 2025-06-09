import adapter.DurationAdapter;
import adapter.LocalDateTimeAdapter;
import client.CustomHttpClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import managers.HttpTaskServer;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.*;
import referencebook.States;
import typetoken.EpicListTypeToken;
import typetoken.SubTaskListTypeToken;
import typetoken.TaskListTypeToken;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class HttpTaskManagerTasksTest {

    private final TaskManager manager;
    private final HttpTaskServer taskServer;
    private final CustomHttpClient httpClient;
    private final Gson gson;

    public HttpTaskManagerTasksTest() {
        manager = new InMemoryTaskManager();
        try {
            taskServer = new HttpTaskServer(manager);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        httpClient = new CustomHttpClient();
    }

    @BeforeEach
    public void setUp() {
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        taskServer.stop();
    }

    private Gson prepareGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                //.serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    @Nested
    @DisplayName("Проверяем /tasks")
    class Tasks {
        private Task task;

        @BeforeEach
        public void setUp() {
            task = new Task("Задача 1", "Комментарий 1", States.IN_PROGRESS,
                    LocalDateTime.of(2025, Month.MAY, 1, 0, 0),
                    Duration.ofDays(25));
        }

        @DisplayName("Создает задачу")
        @Test
        public void shouldAddTask() {
            String taskJson = gson.toJson(task);

            int statusCode = httpClient.runPostRequest("/tasks", taskJson);

            assertEquals(201, statusCode);
            assertEquals(1, manager.getTaskList().size(), "Некорректное количество задач");
            assertEquals("Задача 1", manager.getTaskList().getFirst().getName(), "Некорректное наименование");
        }

        @DisplayName("Не создает задачу с плохим интервалом (пересечения!)")
        @Test
        public void shouldDoNotAddTaskWithBadInterval() {
            String taskJson = gson.toJson(task);
            httpClient.runPostRequest("/tasks", taskJson);

            task = new Task("Задача 2", "Комментарий 2", States.IN_PROGRESS,
                    LocalDateTime.of(2025, Month.MAY, 10, 0, 0),
                    Duration.ofDays(1));

            int statusCode = httpClient.runPostRequest("/tasks", taskJson);

            assertEquals(406, statusCode);
            assertEquals(1, manager.getTaskList().size(), "Некорректное количество задач");
            assertEquals("Задача 1", manager.getTaskList().getFirst().getName(), "Некорректное наименование");
        }

        @DisplayName("Обновляет задачу")
        @Test
        public void shouldUpdateTask() {
            String taskJson = gson.toJson(task);
            httpClient.runPostRequest("/tasks", taskJson);
            task = new Task(1, "Измененное название", "измененный комментарий", States.DONE,
                    LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0),
                    Duration.ofDays(10));
            taskJson = gson.toJson(task);


            int statusCode = httpClient.runPostRequest("/tasks/1", taskJson);

            assertEquals(201, statusCode);
            assertEquals(1, manager.getTaskList().size(), "Некорректное количество задач");
            assertEquals("Измененное название", manager.getTaskList().getFirst().getName(),
                    "не изменил название");
            assertEquals("измененный комментарий", manager.getTaskList().getFirst().getDescription(),
                    "не изменил комментарий");
            assertEquals(LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0),
                    manager.getTaskList().getFirst().getStartTime(),
                    "не поменял дату начала");
            assertEquals(Duration.ofDays(10), manager.getTaskList().getFirst().getDuration(),
                    "не поменял длительность");
        }

        @DisplayName("Не обновляет несуществующую задачу")
        @Test
        public void shouldNotUpdateTask() {
            String taskJson = gson.toJson(task);

            httpClient.runPostRequest("/tasks/", taskJson);
            task = new Task(666, "Измененное название", "измененный комментарий", States.DONE,
                    LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0),
                    Duration.ofDays(10));
            taskJson = gson.toJson(task);

            int statusCode = httpClient.runPostRequest("/tasks/666", taskJson);


            assertEquals(404, statusCode);
        }

        @DisplayName("Удаляет задачу")
        @Test
        public void shouldDeleteTask() {
            String taskJson = gson.toJson(task);

            httpClient.runPostRequest("/tasks", taskJson);

            int statusCode = httpClient.runDeleteRequest("/tasks/1");

            assertEquals(200, statusCode);
            assertEquals(0, manager.getTaskList().size(), "Некорректное количество задач");
        }

        @DisplayName("Не удаляет несуществующую задачу")
        @Test
        public void shouldNotDeleteTask() {
            String taskJson = gson.toJson(task);

            httpClient.runPostRequest("/tasks", taskJson);

            int statusCode = httpClient.runDeleteRequest("/tasks/666");

            assertEquals(404, statusCode);
        }

        @DisplayName("Возвращает задачу")
        @Test
        public void shouldGetTask() {
            String taskJson = gson.toJson(task);
            httpClient.runPostRequest("/tasks", taskJson);


            HttpResponse<String> httpResponse = httpClient.runGetRequest("/tasks/1");
            task = prepareGson().fromJson(httpResponse.body(), Task.class);

            assertEquals(200, httpResponse.statusCode());
            assertEquals(1, task.getId(), "Возвратил не ту задачу!");
        }

        @DisplayName("Не возвращает несуществующую задачу")
        @Test
        public void shouldDoesNotGetTask() {
            String taskJson = gson.toJson(task);
            httpClient.runPostRequest("/tasks", taskJson);

            int statusCode = httpClient.runGetRequest("/tasks/666").statusCode();

            assertEquals(404, statusCode);
        }

        @DisplayName("Возвращает все задачи")
        @Test
        public void shouldGetTasks() {
            String taskJson = gson.toJson(task);
            httpClient.runPostRequest("/tasks", taskJson);

            task = new Task("задача 2", "комментарий", States.DONE,
                    LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0),
                    Duration.ofDays(10));
            taskJson = gson.toJson(task);
            httpClient.runPostRequest("/tasks", taskJson);

            HttpResponse<String> httpResponse = httpClient.runGetRequest("/tasks");
            List<Task> taskList = prepareGson().fromJson(httpResponse.body(), new TaskListTypeToken().getType());

            assertEquals(200, httpResponse.statusCode());
            assertEquals(2, taskList.size(), "Возвратил кривой список");
            assertEquals(1, taskList.getFirst().getId(), "нет задачи 1");
            assertEquals(2, taskList.get(1).getId(), "нет задачи 2");
        }

    }

    @Nested
    @DisplayName("Проверяем /epics")
    class Epics {
        private Epic epic;

        @BeforeEach
        public void setUp() {
            epic = new Epic("Эпик1", "Комментарий 1");
        }

        @DisplayName("Создает эпик")
        @Test
        public void shouldAddEpic() {
            String taskJson = gson.toJson(epic);
            int statusCode = httpClient.runPostRequest("/epics", taskJson);

            assertEquals(201, statusCode);
            assertEquals(1, manager.getEpicList().size(), "Некорректное количество эпиков");
            assertEquals("Эпик1", manager.getEpicList().getFirst().getName(), "Некорректное наименование");
        }

        @DisplayName("Обновляет эпик")
        @Test
        public void shouldUpdateEpic() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);
            epic = new Epic(1, "Измененное название", "измененный комментарий");
            taskJson = gson.toJson(epic);


            int statusCode = httpClient.runPostRequest("/epics/1", taskJson);

            assertEquals(201, statusCode);
            assertEquals(1, manager.getEpicList().size(), "Некорректное количество эпиков");
            assertEquals("Измененное название", manager.getEpicList().getFirst().getName(),
                    "не изменил название");
            assertEquals("измененный комментарий", manager.getEpicList().getFirst().getDescription(),
                    "не изменил комментарий");
        }

        @DisplayName("Не обновляет несуществующий эпик")
        @Test
        public void shouldNotUpdateEpic() {
            String taskJson = gson.toJson(epic);

            httpClient.runPostRequest("/epics", taskJson);
            epic = new Epic(666, "Измененное название", "измененный комментарий");
            taskJson = gson.toJson(epic);

            int statusCode = httpClient.runPostRequest("/epics/666", taskJson);

            assertEquals(404, statusCode);
        }

        @DisplayName("Удаляет эпик")
        @Test
        public void shouldDeleteEpic() {
            String taskJson = gson.toJson(epic);

            httpClient.runPostRequest("/epics", taskJson);

            int statusCode = httpClient.runDeleteRequest("/epics/1");

            assertEquals(200, statusCode);
            assertEquals(0, manager.getEpicList().size(), "Некорректное количество задач");
        }

        @DisplayName("Не удаляет несуществующий эпик")
        @Test
        public void shouldNotDeleteEpic() {
            String taskJson = gson.toJson(epic);

            httpClient.runPostRequest("/epics", taskJson);

            int statusCode = httpClient.runDeleteRequest("/epics/666");

            assertEquals(404, statusCode);
        }

        @DisplayName("Возвращает epic")
        @Test
        public void shouldGetEpic() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);


            HttpResponse<String> httpResponse = httpClient.runGetRequest("/epics/1");
            epic = prepareGson().fromJson(httpResponse.body(), Epic.class);

            assertEquals(200, httpResponse.statusCode());
            assertEquals(1, epic.getId(), "Возвратил не ту задачу!");
        }

        @DisplayName("Не возвращает несуществующий эпик")
        @Test
        public void shouldDoesNotGetTask() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);

            int statusCode = httpClient.runGetRequest("/epics/666").statusCode();

            assertEquals(404, statusCode);
        }

        @DisplayName("Возвращает все эпики")
        @Test
        public void shouldGetEpics() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);

            epic = new Epic("задача 2", "комментарий");
            taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);


            HttpResponse<String> httpResponse = httpClient.runGetRequest("/epics");
            List<Epic> epicList = prepareGson().fromJson(httpResponse.body(), new EpicListTypeToken().getType());

            assertEquals(200, httpResponse.statusCode());
            assertEquals(2, epicList.size(), "Возвратил кривой список");
            assertEquals(1, epicList.getFirst().getId(), "нет эпика' 1");
            assertEquals(2, epicList.get(1).getId(), "нет эпика 2");
        }

    }

    @Nested
    @DisplayName("Проверяем /subTasks")
    class SubTasks {
        private Epic epic;
        private SubTask subTask;

        @BeforeEach
        public void setUp() {
            epic = new Epic("Эпик1", "Комментарий к эпику 1");
            subTask = new SubTask("Подзадача 1", "Комментарий 1", States.NEW, 1,
                    LocalDateTime.of(2025, Month.MAY, 1, 0, 0),
                    Duration.ofDays(25));
        }

        @DisplayName("Создает подзадачу к Эпику")
        @Test
        public void shouldAddSubTask() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);
            taskJson = gson.toJson(subTask);
            int statusCode = httpClient.runPostRequest("/subtasks", taskJson);

            assertEquals(201, statusCode);
            assertEquals(1, manager.getSubTaskList().size(), "Некорректное количество задач");
            assertEquals("Подзадача 1", manager.getSubTaskList().getFirst().getName(), "Некорректное наименование");
        }

        @DisplayName("Не создает подзадачу с плохим интервалом (пересечения!)")
        @Test
        public void shouldDoNotAddSubTaskWithBadInterval() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);
            taskJson = gson.toJson(subTask);
            httpClient.runPostRequest("/subtasks", taskJson);

            subTask = new SubTask("Подзадача 2", "Комментарий 2", States.IN_PROGRESS, 1,
                    LocalDateTime.of(2025, Month.MAY, 10, 0, 0),
                    Duration.ofDays(1));
            taskJson = gson.toJson(subTask);
            int statusCode = httpClient.runPostRequest("/subtasks", taskJson);

            assertEquals(406, statusCode);
            assertEquals(1, manager.getSubTaskList().size(), "Некорректное количество подзадач");
            assertEquals("Подзадача 1", manager.getSubTaskList().getFirst().getName(), "Некорректное наименование");
        }

        @DisplayName("Обновляет подзадачу")
        @Test
        public void shouldUpdateSubTask() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);
            taskJson = gson.toJson(subTask);
            httpClient.runPostRequest("/subtasks", taskJson);


            subTask = new SubTask(2, "Измененное название", "измененный комментарий", States.DONE,
                    1, LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0),
                    Duration.ofDays(10));
            taskJson = gson.toJson(subTask);


            int statusCode = httpClient.runPostRequest("/subtasks/2", taskJson);

            assertEquals(201, statusCode);
            assertEquals(1, manager.getSubTaskList().size(), "Некорректное количество подзадач");
            assertEquals("Измененное название", manager.getSubTaskList().getFirst().getName(),
                    "не изменил название");
            assertEquals("измененный комментарий", manager.getSubTaskList().getFirst().getDescription(),
                    "не изменил комментарий");
            assertEquals(LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0),
                    manager.getSubTaskList().getFirst().getStartTime(),
                    "не поменял дату начала");
            assertEquals(Duration.ofDays(10), manager.getSubTaskList().getFirst().getDuration(),
                    "не поменял длительность");
        }

        @DisplayName("Не обновляет несуществующую подзадачу")
        @Test
        public void shouldNotUpdateSubTask() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);
            taskJson = gson.toJson(subTask);
            httpClient.runPostRequest("/subtasks", taskJson);

            subTask = new SubTask(666, "Измененное название", "измененный комментарий",
                    States.DONE, 1,
                    LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0),
                    Duration.ofDays(10));
            taskJson = gson.toJson(subTask);

            int statusCode = httpClient.runPostRequest("/subtasks/666", taskJson);


            assertEquals(404, statusCode);
        }

        @DisplayName("Удаляет подзадачу")
        @Test
        public void shouldDeleteSubTask() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);
            taskJson = gson.toJson(subTask);
            httpClient.runPostRequest("/subtasks", taskJson);

            System.out.println(manager.getSubTaskList());

            int statusCode = httpClient.runDeleteRequest("/subtasks/2");

            assertEquals(200, statusCode);
            assertEquals(0, manager.getSubTaskList().size(), "Некорректное количество подзадач");
        }

        @DisplayName("Не удаляет несуществующую подзадачу")
        @Test
        public void shouldNotDeleteTask() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);
            taskJson = gson.toJson(subTask);
            httpClient.runPostRequest("/subtasks", taskJson);

            int statusCode = httpClient.runDeleteRequest("/subtasks/666");

            assertEquals(404, statusCode);
        }

        @DisplayName("Возвращает подзадачу")
        @Test
        public void shouldGetSubTask() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);
            taskJson = gson.toJson(subTask);
            httpClient.runPostRequest("/subtasks", taskJson);


            HttpResponse<String> httpResponse = httpClient.runGetRequest("/subtasks/2");
            subTask = prepareGson().fromJson(httpResponse.body(), SubTask.class);

            assertEquals(200, httpResponse.statusCode());
            assertEquals(2, subTask.getId(), "Возвратил не ту задачу!");
        }

        @DisplayName("Не возвращает несуществующую подзадачу")
        @Test
        public void shouldDoesNotGetTask() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);
            taskJson = gson.toJson(subTask);
            httpClient.runPostRequest("/subtasks", taskJson);

            int statusCode = httpClient.runGetRequest("/subtasks/666").statusCode();

            assertEquals(404, statusCode);
        }

        @DisplayName("Возвращает все подзадачи")
        @Test
        public void shouldGetTasks() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);
            taskJson = gson.toJson(subTask);
            httpClient.runPostRequest("/subtasks", taskJson);

            subTask = new SubTask("подзадача 2", "комментарий", States.DONE, 1,
                    LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0),
                    Duration.ofDays(10));
            taskJson = gson.toJson(subTask);
            httpClient.runPostRequest("/subtasks", taskJson);


            HttpResponse<String> httpResponse = httpClient.runGetRequest("/subtasks");
            List<SubTask> subTaskList = prepareGson().fromJson(httpResponse.body(),
                    new SubTaskListTypeToken().getType());

            assertEquals(200, httpResponse.statusCode());
            assertEquals(2, subTaskList.size(), "Возвратил кривой список");
            assertEquals(2, subTaskList.getFirst().getId(), "нет подзадачи 1");
            assertEquals(3, subTaskList.get(1).getId(), "нет задачи 2");
        }

    }

    @Nested
    @DisplayName("Проверяем /history")
    class History {
        private Epic epic;
        private SubTask subTask;
        private Task task;

        @BeforeEach
        public void setUp() {
            epic = new Epic("Эпик1", "Комментарий к эпику 1");
            subTask = new SubTask("Подзадача 1", "Комментарий 1", States.NEW, 1,
                    LocalDateTime.of(2025, Month.MAY, 1, 0, 0),
                    Duration.ofDays(25));
            task = new Task("Задача 1", "Комментарий 1", States.NEW,
                    LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0),
                    Duration.ofDays(3));
        }

        @DisplayName("Возвращает историю")
        @Test
        public void shouldGetHistory() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);
            taskJson = gson.toJson(subTask);
            httpClient.runPostRequest("/subtasks", taskJson);
            taskJson = gson.toJson(task);
            httpClient.runPostRequest("/tasks", taskJson);

            httpClient.runGetRequest("/tasks/3");
            httpClient.runGetRequest("/epics/1");
            httpClient.runGetRequest("/subtasks/2");

            HttpResponse<String> httpResponse = httpClient.runGetRequest("/history");
            List<Task> historyList = prepareGson().fromJson(httpResponse.body(),
                    new TaskListTypeToken().getType());

            assertEquals(200, httpResponse.statusCode());

            assertEquals(3, historyList.getFirst().getId(), "Кривая история");
            assertEquals(1, historyList.get(1).getId(), "Кривая история");
            assertEquals(2, historyList.get(2).getId(), "Кривая история");
        }
    }

    @Nested
    @DisplayName("Проверяем /prioritized")
    class PrioritizedTasks {
        private Epic epic;
        private SubTask subTask;
        private Task task;

        @BeforeEach
        public void setUp() {
            epic = new Epic("Эпик1", "Комментарий к эпику 1");
            subTask = new SubTask("Подзадача 1", "Комментарий 1", States.NEW, 1,
                    LocalDateTime.of(2025, Month.MAY, 1, 0, 0),
                    Duration.ofDays(25));
            task = new Task("Задача 1", "Комментарий 1", States.NEW,
                    LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0),
                    Duration.ofDays(3));
        }

        @DisplayName("Возвращает список приоритетных задач")
        @Test
        public void shouldGetHistory() {
            String taskJson = gson.toJson(epic);
            httpClient.runPostRequest("/epics", taskJson);
            taskJson = gson.toJson(subTask);
            httpClient.runPostRequest("/subtasks", taskJson);
            taskJson = gson.toJson(task);
            httpClient.runPostRequest("/tasks", taskJson);


            HttpResponse<String> httpResponse = httpClient.runGetRequest("/prioritized");
            List<Task> prioritizedList = prepareGson().fromJson(httpResponse.body(),
                    new TaskListTypeToken().getType());

            assertEquals(200, httpResponse.statusCode());
            assertEquals(2, prioritizedList.size(), "список приоритетных задач кривой");
        }
    }


}