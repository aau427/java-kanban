import common.Managers;
import exception.LogicalErrorException;
import exception.ManagerIntervalException;
import exception.TaskNotFoundException;
import managers.TaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.*;
import referencebook.States;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected final DateTimeFormatter dateTimeFormatter = Managers.getDefaultDateTimeFormatter();

    @AfterEach
    public void afterEach() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
    }

    @Nested
    @DisplayName("Проверка задач")
    class CheckTasks {
        private Task task;
        private int taskId;

        @BeforeEach
        public void beforeEach() {
            LocalDateTime startDateTime = LocalDateTime.parse("10.01.2025 00:00", dateTimeFormatter);
            Duration duration = Duration.ofDays(10);
            task = new Task("Задача №1", "Коммент к задаче", States.NEW, startDateTime, duration);
        }

        @DisplayName("Менеджер действительно добавляет задачу")
        @Test
        public void shouldManagerAddTask() {
            taskId = taskManager.createTask(task);
            task = taskManager.getTaskById(taskId);

            assertEquals(taskId, task.getId(), "Задача 1 не порождена!");
            assertEquals(Duration.ofDays(10), task.getDuration());
            assertEquals(LocalDateTime.parse("10.01.2025 00:00", dateTimeFormatter), task.getStartTime());
            assertEquals(LocalDateTime.parse("20.01.2025 00:00", dateTimeFormatter), task.getEndTime());
        }

        @DisplayName("Менеджер не добавляет задачу, если  она ВНУТРИ интервала")
        @Test
        public void shouldManagerDoesNotAddTaskIfTasksCross() {
            taskId = taskManager.createTask(task);
            task = taskManager.getTaskById(taskId);
            LocalDateTime startDateTime1 = LocalDateTime.parse("12.01.2025 00:00", dateTimeFormatter);
            Duration duration1 = Duration.ofDays(3);
            Task task1 = new Task("Задача №2", "Коммент к задаче 2", States.NEW, startDateTime1,
                    duration1);

            assertThrows(ManagerIntervalException.class, () -> {
                int task1Id = taskManager.createTask(task1);
            }, "Добавление задачи, если интервал занят, должно приводить к исключению!");
            assertEquals(taskId, task.getId(), "Задача 1 не порождена!");
        }

        @DisplayName("Менеджер не добавляет задачу, если  интервалы пересекаются слева")
        @Test
        public void shouldManagerDontAddTaskIfTasksCrossLeft() {
            taskId = taskManager.createTask(task);
            task = taskManager.getTaskById(taskId);
            LocalDateTime startDateTime1 = LocalDateTime.parse("09.01.2025 00:00", dateTimeFormatter);
            Duration duration1 = Duration.ofDays(3);
            Task task1 = new Task("Задача №2", "Коммент к задаче 2", States.NEW, startDateTime1,
                    duration1);

            assertThrows(ManagerIntervalException.class, () -> {
                int task1Id = taskManager.createTask(task1);
            }, "Добавление задачи, если интервал занят, должно приводить к исключению!");
            assertEquals(taskId, task.getId(), "Задача 1 не порождена!");
        }

        @DisplayName("Менеджер не добавляет задачу, если  интервалы пересекаются справа")
        @Test
        public void shouldManagerDontAddTaskIfTasksCrossRight() {
            taskId = taskManager.createTask(task);
            task = taskManager.getTaskById(taskId);
            LocalDateTime startDateTime1 = LocalDateTime.parse("19.01.2025 00:00", dateTimeFormatter);
            Duration duration1 = Duration.ofDays(3);
            Task task1 = new Task("Задача №2", "Коммент к задаче 2", States.NEW, startDateTime1,
                    duration1);

            assertThrows(ManagerIntervalException.class, () -> {
                int task1Id = taskManager.createTask(task1);
            }, "Добавление задачи, если интервал занят, должно приводить к исключению!");
            assertEquals(taskId, task.getId(), "Задача 1 не порождена!");
        }

        @DisplayName("Менеджер корректно добавляет задачу с пустым startDate")
        @Test
        public void shouldManagerCorrectAddTaskWithNullStartDate() {
            taskId = taskManager.createTask(task);
            task = taskManager.getTaskById(taskId);
            Duration duration1 = Duration.ofDays(3);
            Task task1 = new Task("Задача №2", "Коммент к задаче 2", States.NEW, null,
                    duration1);

            int task1Id = taskManager.createTask(task1);

            assertTrue(task1Id > 0, "Задача не порождена");
            assertEquals(taskId, taskManager.getTaskList().getFirst().getId(),
                    "Задача 1 не добавлена в TaskList!");
            assertTrue(taskManager.getPrioritizedTasks().size() == 1,
                    "В cписке приоритетных задач болье 2!");
            assertEquals(taskId, taskManager.getPrioritizedTasks().getFirst().getId(),
                    "В приоритетный список добавили не то!");
        }

        @DisplayName("TaskManager добавляет 2 задачи, если их интервалы не пересекаются")
        @Test
        public void shouldManagerAddTaskWithNoCrossIntervals() {
            taskId = taskManager.createTask(task);
            task = taskManager.getTaskById(taskId);
            Duration duration1 = Duration.ofDays(3);
            Task task1 = new Task("Задача №2", "Коммент к задаче 2", States.NEW,
                    LocalDateTime.parse("25.05.2025 00:00", dateTimeFormatter),
                    duration1);

            int task1Id = taskManager.createTask(task1);

            assertTrue(taskManager.getTaskList().size() == 2, "С таксклистом беда!");
            assertTrue(taskManager.getTaskList().stream().anyMatch(task -> task.getId() == taskId),
                    "Список задач не содержит задачу 1");
            assertTrue(taskManager.getTaskList().stream().anyMatch(task -> task.getId() == task1Id),
                    "Список задач не содержит задачу 2");
        }

        @DisplayName("TaskManager добавляет корректно добавляет 2 задачи в список приоритизированных")
        @Test
        public void shouldManagerAddTasksToPrioritizedList() {
            taskId = taskManager.createTask(task);
            task = taskManager.getTaskById(taskId);
            Duration duration1 = Duration.ofDays(3);
            Task task1 = new Task("Задача №2", "Коммент к задаче 2", States.NEW,
                    LocalDateTime.parse("25.05.2025 00:00", dateTimeFormatter),
                    duration1);

            int task1Id = taskManager.createTask(task1);

            assertTrue(taskManager.getPrioritizedTasks().size() == 2,
                    "Со списком приоритетных задач беда!");
            assertEquals(taskId, taskManager.getPrioritizedTasks().getFirst().getId(),
                    "В списке приоритетных задач Задача №1 должны быть первой");
            assertEquals(task1Id, taskManager.getPrioritizedTasks().get(1).getId(),
                    "В списке приоритетных задач Задача №2 должны быть второй");
        }

        @DisplayName("TaskManager корректно удаляет задачу")
        @Test
        public void shouldManagerRemoveTask() {
            taskId = taskManager.createTask(task);
            task = taskManager.getTaskById(taskId);
            Duration duration1 = Duration.ofDays(3);
            Task task1 = new Task("Задача №2", "Коммент к задаче 2", States.NEW,
                    LocalDateTime.parse("25.05.2025 00:00", dateTimeFormatter),
                    duration1);
            int task1Id = taskManager.createTask(task1);

            taskManager.deleteTaskById(taskId);

            assertTrue(taskManager.getTaskList().size() == 1, "Со списком задач беда!");
            assertEquals(task1Id, taskManager.getTaskList().getFirst().getId(),
                    "Не ту задачу удалили из тасклиста!");
            assertTrue(taskManager.getPrioritizedTasks().size() == 1,
                    "Со списком приоритетных задач беда!");
            assertEquals(task1Id, taskManager.getPrioritizedTasks().getFirst().getId(),
                    "В списке приоритетных задач Задача №1 должны быть первой и единственной!");
        }

        @DisplayName("TaskManager корректно удаляет все задачи")
        @Test
        public void shouldManagerRemoveAllTasks() {
            taskId = taskManager.createTask(task);
            task = taskManager.getTaskById(taskId);
            Duration duration1 = Duration.ofDays(3);
            Task task1 = new Task("Задача №2", "Коммент к задаче 2", States.NEW,
                    LocalDateTime.parse("25.05.2025 00:00", dateTimeFormatter),
                    duration1);
            int task1Id = taskManager.createTask(task1);

            taskManager.deleteAllTasks();

            assertTrue(taskManager.getTaskList().size() == 0, "список задач должен быть пустым!");
            assertTrue(taskManager.getPrioritizedTasks().size() == 0,
                    "список приоритизированных задач должен быть пустым!");
        }

        @DisplayName("TaskManager действительно обновляет задачу")
        @Test
        public void shouldTaskManagerUpdateTask() {
            LocalDateTime newStartDateTime = LocalDateTime.parse("13.06.2025 00:00", dateTimeFormatter);
            Duration newDuration = Duration.ofDays(1);
            taskId = taskManager.createTask(task);

            Duration duration1 = Duration.ofDays(3);
            Task task1 = new Task("Задача №2", "Коммент к задаче 2", States.NEW,
                    LocalDateTime.parse("25.05.2025 00:00", dateTimeFormatter),
                    duration1);

            task = new Task(taskId, "изменил название", "изменил комментарий", States.DONE,
                    newStartDateTime, newDuration);

            assertDoesNotThrow(() -> {
                taskId = taskManager.updateTask(task);
            }, "Задача 2 должна добавляться, т.к. освободился интервал при изменении задачи 1");
            task = taskManager.getTaskById(taskId);
            assertEquals("изменил название", task.getName(), "Не поменял название!");
            assertEquals("изменил комментарий", task.getDescription(), "Не поменял название!");
            assertEquals(States.DONE, task.getState(), "Не поменял статус!");
            assertEquals(Duration.ofDays(1), task.getDuration(), "Не поменял продолжительность");
            assertEquals(LocalDateTime.parse("13.06.2025 00:00", dateTimeFormatter), task.getStartTime(),
                    "Не поменял дату начала");
            assertEquals(LocalDateTime.parse("14.06.2025 00:00", dateTimeFormatter), task.getEndTime(),
                    "Не поменял дату окончания");
        }

        @DisplayName("TaskManager не обновляет задачу, если ее нет в taskList")
        @Test
        public void shouldTaskManagerDoesNotUpdateTaskIfTaskNotCreated() {
            LocalDateTime newStartDateTime = LocalDateTime.parse("13.06.2025 00:00", dateTimeFormatter);
            Duration newDuration = Duration.ofDays(1);
            taskId = 666;
            task = new Task(666, "изменил название", "изменил комментарий", States.DONE,
                    newStartDateTime, newDuration);

            assertThrows(TaskNotFoundException.class, () -> {
                taskId = taskManager.updateTask(task);
            }, "Обновление ранее несозданной задачи должно приводить к исключению!");
        }

        @DisplayName("TaskManager не обновляет задачу без Id")
        @Test
        public void shouldTaskManagerDoesNotUpdateTaskWithoutId() {
            LocalDateTime newStartDateTime = LocalDateTime.parse("13.06.2025 00:00", dateTimeFormatter);
            Duration newDuration = Duration.ofDays(1);
            task = new Task("изменил название", "изменил комментарий", States.DONE,
                    newStartDateTime, newDuration);

            assertThrows(TaskNotFoundException.class, () -> {
                taskId = taskManager.updateTask(task);
            }, "Обновление задачи без Id должно приводить к исключению!");
        }

        @DisplayName("При изменении задачи высвобождается старый интервал")
        @Test
        public void shouldTaskManagerRefreshIntervalsAfterUpdateTask() {
            taskId = taskManager.createTask(task); // 10.01.2025 00:00  10 дней;

            task = new Task(taskId, "Задача №2", "комментарий к задаче №2", States.IN_PROGRESS,
                    LocalDateTime.parse("10.02.2025 00:00", dateTimeFormatter), Duration.ofDays(1));
            taskManager.updateTask(task);

            Task task1 = new Task("изменил название", "изменил комментарий", States.NEW,
                    LocalDateTime.parse("10.01.2025 00:00", dateTimeFormatter), Duration.ofDays(10));

            assertDoesNotThrow(() -> {
                int task1Id = taskManager.createTask(task1);
            }, "Задача 2 должна добавляться, т.к. освободился интервал при изменении задачи 1");

            assertTrue(taskManager.getTaskList().size() == 2, "Беда с тасклист");
        }


        @DisplayName("Экземпляры Task равны друг другу, если равен их Id")
        @Test
        public void shouldTasksInstancesEqualsIfIdsIsEquals() {
            taskId = taskManager.createTask(task);
            Task task1 = taskManager.getTaskById(taskId);

            assertEquals(task.getName(), task1.getName(), "Не равны наименования");
            assertEquals(task.getDescription(), task1.getDescription(), "Не равны комментарии");
            assertEquals(task.getState(), task1.getState(), "Не совпали статусы");
            assertEquals(task.getStartTime(), task1.getStartTime(), "Не равны даты начала");
            assertEquals(task.getEndTime(), task1.getEndTime(), "Не равны даты окончания");
            assertEquals(task.getDuration(), task1.getDuration(), "Не равны продолжительности");
        }

        @DisplayName("Задача с заданным ID и сгенерированным ID не конфликтуют внутри менеджера")
        @Test
        public void shouldTasksWithSetIdAndGeneratedIdWillNotConflict() {
            Task task1 = new Task(taskId, "Какая-то другая задача", "Потихоньку едет крыша!!!!", States.DONE,
                    task.getStartTime(), task.getDuration());
            assertThrows(LogicalErrorException.class, () -> {
                int task1Id = taskManager.createTask(task1);
            }, "Добавление задачи c заданным ID должно приводить к исключению!");
        }
    }

    @Nested
    @DisplayName("Проверка задач в истории")
    class TaskHistory {
        private Task task;
        private int taskId;
        private Task task1;
        private int task1Id;
        LocalDateTime startDateTime1 = LocalDateTime.parse("10.10.2025 00:00", dateTimeFormatter);
        Duration duration1 = Duration.ofDays(10);

        @BeforeEach
        public void beforeEach() {
            LocalDateTime startDateTime = LocalDateTime.parse("10.01.2025 00:00", dateTimeFormatter);
            Duration duration = Duration.ofDays(10);
            task = new Task("Задача №1", "Коммент к задаче", States.NEW, startDateTime, duration);
            taskId = taskManager.createTask(task);
            task1 = new Task(" Задача №1", "Комментарий к задачу 1", States.DONE,
                    startDateTime1, duration1);
            task1Id = taskManager.createTask(task1);
        }

        @DisplayName("Задача добавляется в HistoryManager и сохраняет свое состояние")
        @Test
        public void shouldTaskAddToHistoryAndSavePrevState() {
            task = taskManager.getTaskById(taskId);
            task1 = taskManager.getTaskById(task1Id);

            Task newTask = new Task(taskId, "Изменил название", "Изменил коммент", States.IN_PROGRESS,
                    LocalDateTime.now(), Duration.ofDays(10));
            taskManager.updateTask(newTask);

            Task taskFromHistory = taskManager.getHistory().getFirst();

            assertEquals(2, taskManager.getHistory().size());
            assertEquals(taskId, taskFromHistory.getId(), "неправильный ID в истории");
            assertEquals("Задача №1", taskFromHistory.getName(), "Неправильное имя в истории");
            assertEquals(task.getDescription(), taskFromHistory.getDescription(), "Неправильный комментарий в истории");
            assertEquals(task.getState(), taskFromHistory.getState(), "Неправильное состояние в истории");
        }

        @DisplayName("Задача удаляется из истории при ее удалении ")
        @Test
        public void shouldTaskRemoveFromHistoryIfItDelete() {
            task = taskManager.getTaskById(taskId);
            task1 = taskManager.getTaskById(task1Id);

            taskManager.deleteTaskById(taskId);

            Task task1 = taskManager.getHistory().getFirst();

            assertEquals(1, taskManager.getHistory().size(), "Не удалился!");
            assertEquals(task1Id, task1.getId(), "Не тот удалился");
        }

        @DisplayName("При удалении всех задач они удаляются из истории")
        @Test
        public void shouldHistoryClearIfTaskListClear() {
            taskManager.getTaskById(taskId);
            taskManager.getTaskById(task1Id);

            taskManager.deleteAllTasks();

            assertEquals(0, taskManager.getHistory().size(), "Не удалилась!");
        }
    }

    @Nested
    @DisplayName("Проверка Эпиков")
    class CommonCheckEpics {
        private Epic epic;
        private int epicId;
        private SubTask subTask1;
        private int subTask1Id;
        private SubTask subTask2;
        private int subTask2Id;
        LocalDateTime startDateTime = LocalDateTime.parse("01.01.2025 00:00", dateTimeFormatter);
        Duration duration = Duration.ofDays(3);

        @BeforeEach
        void beforeEach() {
            epic = new Epic("Эпик № 1", "Комментарий к эпику  №1 ");
            epicId = taskManager.createEpic(epic);
            subTask1 = new SubTask("Сабтаск 1", "Коммент к сабтаск1", States.NEW, epicId,
                    startDateTime, duration);
            subTask1Id = taskManager.createSubTask(subTask1);
        }

        @DisplayName("TaskManager действительно добавляет Эпик")
        @Test
        public void shouldTaskManagerAddEpic() {
            Epic epic1 = taskManager.getEpicById(epicId);

            assertEquals(epicId, (int) epic1.getId(), "Эпик 1 не порожден!");
            assertEquals(epic.getState(), epic1.getState(), "Не совпали статусы");
            assertEquals(duration, epic1.getDuration(), "Не совпала продолжительность");
            assertEquals(startDateTime, epic1.getStartTimeOptional().get(), "Не совпала продолжительность");
        }

        @DisplayName("TaskManager действительно удаляет Эпик")
        @Test
        public void shouldTaskManagerRemoveEpic() {
            taskManager.deleteEpicById(epicId);

            assertThrows(TaskNotFoundException.class, () -> {
                epic = taskManager.getEpicById(epicId);
            }, "Эпик не удален!");
        }

        @DisplayName("TaskManager действительно обновляет Эпик")
        @Test
        public void shouldTaskManagerUpdateEpic() {
            epic = new Epic(epicId, "изменил название", "изменил комментарий");

            assertDoesNotThrow(() -> {
                taskManager.updateEpic(epic);
            }, "Не обновил Эпик!");
            taskManager.updateEpic(epic);

            epic = taskManager.getEpicById(epicId);
            assertEquals("изменил название", epic.getName(), "Не поменял название!");
            assertEquals("изменил комментарий", epic.getDescription(), "Не поменял название!");
        }

        @DisplayName("TaskManager не обновляет Эпик без Id")
        @Test
        public void shouldTaskManagerDoesNotUpdateEpicWithoutId() {
            epic = new Epic("изменил название", "изменил комментарий");

            assertThrows(TaskNotFoundException.class, () -> {
                taskManager.updateEpic(epic);
            }, "Обновление Эпика без ID должно приводить к исключению!");
        }

        @DisplayName("TaskManager не обновляет несозданный Эпик")
        @Test
        public void shouldTaskManagerDoesNotUpdateNotCreatedEpic() {
            epic = new Epic(666, "изменил название", "изменил комментарий");

            assertThrows(TaskNotFoundException.class, () -> {
                taskManager.updateEpic(epic);
            }, "Обновление ранее несозданного Эпика должно приводить к исключению!");
        }

        @DisplayName("Эпик нельзя сделать своей же подзадачей")
        @Test
        public void shouldNotEpicMakeIdOwnSubTask() {
            subTask1 = new SubTask(epicId, "Подзадач", "Комментарий", States.NEW, epicId,
                    LocalDateTime.now(), Duration.ofDays(1));
            assertThrows(LogicalErrorException.class, () -> {
                int subTask1Id = taskManager.createSubTask(subTask1);
            }, "Порождение подзадачи с указанным ID должно привести к исключению!");
        }

        @DisplayName("Экземпляры Epic равны друг другу, если равен их Id")
        @Test
        public void shouldEpicsInstancesEqualsIfIdsIsEquals() {
            epic = taskManager.getEpicById(epicId);
            Epic epic1 = taskManager.getEpicById(epicId);

            assertEquals(epic.getName(), epic1.getName(), "Не равны наименования");
            assertEquals(epic.getDescription(), epic1.getDescription(), "Не равны комментарии");
            assertEquals(epic.getState(), epic1.getState(), "Не совпали статусы");
            assertEquals(epic.getDuration(), epic1.getDuration(), "Не совпала длительность");
            assertEquals(epic.getStartTimeOptional().get(), epic1.getStartTimeOptional().get(),
                    "Не совпала дата начала");
            assertEquals(epic.getEndTimeOptional().get(), epic1.getEndTimeOptional().get(),
                    "Не совпала дата окончания");
        }
    }

    @Nested
    @DisplayName("Когда все подзадачи у Эпика New")
    class EpicNew {
        private Epic epic;
        private SubTask subTask1;
        private SubTask subTask2;
        private int epicId;
        private int subTask1Id;
        private int subTask2Id;
        LocalDateTime startDateTime = LocalDateTime.parse("01.01.2025 00:00", dateTimeFormatter);
        Duration duration = Duration.ofDays(3);

        @BeforeEach
        void beforeEach() {
            epic = new Epic("Эпик", "Комментарий к эпику");
            epicId = taskManager.createEpic(epic);
            subTask1 = new SubTask("Сабтаск 1", "Коммент к сабтаск1", States.NEW, epicId,
                    startDateTime, duration);
            subTask1Id = taskManager.createSubTask(subTask1);
            subTask2 = new SubTask("Сабтаск2", "Комментарий к подзадаче", States.NEW, epicId,
                    LocalDateTime.now().minusDays(10), Duration.ofDays(20));
            subTask2Id = taskManager.createSubTask(subTask2);
        }

        @DisplayName("Статус Эпика New, если все подзадачи New")
        @Test
        public void shouldEpicNewIfAllSubTaskNew() {
            epic = taskManager.getEpicById(epicId);
            assertEquals(States.NEW, epic.getState(), "Статус эпика неправильный!");
        }

        @DisplayName("Статус Эпика New после удаления всех подзадач")
        @Test
        public void shouldEpicNewAfterSubTaskListClear() {
            taskManager.deleteAllSubTasks();
            epic = taskManager.getEpicById(epicId);

            assertEquals(States.NEW, epic.getState(), "Статус эпика неправильный!");
        }

        @DisplayName("Внутри эпика не остается неактуальных подзадач")
        @Test
        public void shouldEpicCorrectAfterSubTaskDelete() {
            epic = taskManager.getEpicById(epicId);
            taskManager.deleteSubTaskById(subTask1Id);

            epic = taskManager.getEpicById(epicId);

            Assertions.assertEquals(1, epic.getChildSubTasks().size());
            Assertions.assertEquals(subTask2Id, taskManager.getSubTaskList().getFirst().getId());
        }

        @DisplayName("Статус эпика автоматически перерасcчитывается c New на IN_PROGRESS")
        @Test
        public void shouldEpicStateAutoRecalc2InProgress() {
            SubTask subTask1 = new SubTask(subTask1Id, "Поменял название", "поменял комментарий",
                    States.IN_PROGRESS, epicId,
                    LocalDateTime.now().plusDays(50), Duration.ofDays(5));
            taskManager.updateSubTask(subTask1);

            Epic epic = taskManager.getEpicById(epicId);

            assertEquals(States.IN_PROGRESS, epic.getState(), "Статус Эпика не перерассчитался");
        }

        @DisplayName("Статус эпика автоматически перерасcчитывается c New на DONE")
        @Test
        public void shouldEpicStateAutoRecalc2Done() {
            subTask1 = new SubTask(subTask1Id, subTask1.getName(), subTask1.getDescription(),
                    States.DONE, epicId, subTask1.getStartTime(), subTask1.getDuration());
            taskManager.updateSubTask(subTask1);

            subTask2 = new SubTask(subTask2Id, subTask2.getName(), subTask2.getDescription(),
                    States.DONE, epicId, subTask2.getStartTime(), subTask2.getDuration());
            taskManager.updateSubTask(subTask2);

            Epic epic = taskManager.getEpicById(epicId);

            assertEquals(States.DONE, epic.getState(), "Статус Эпика не перерассчитался");
        }

        @DisplayName("Смена статуса Эпика сеттером не влияет на состояние Манагера")
        @Test
        public void shouldManagerStateNotChahgeAfetSetStateForEpic() {
            epic = taskManager.getEpicById(epicId);
            epic.setState(States.IN_PROGRESS);

            epic = taskManager.getEpicById(epicId);

            assertEquals(States.NEW, epic.getState(), "Повлиял");
            assertEquals(States.NEW, taskManager.getEpicList().getFirst().getState(), "Повлиял!");
        }

        @DisplayName("Смена статуса Эпика сеттером по ссылке из листа не влияет на состояние Манагера")
        @Test
        public void shouldManagerStateNotChahgeAfetSetStateForEpicFromList() {

            epic = taskManager.getEpicList().getFirst();
            epic.setState(States.IN_PROGRESS);
            epic = taskManager.getEpicById(epicId);

            assertEquals(States.NEW, epic.getState(), "Повлиял!");
            assertEquals(States.NEW, taskManager.getEpicList().getFirst().getState(), "Повлиял!");
        }
    }

    @DisplayName("Когда все подзадачи у Эпика DONE")
    @Nested
    class EpicDone {
        private Epic epic;
        private SubTask subTask1;
        private SubTask subTask2;
        private int epicId;
        private int subTask1Id;
        private int subTask2Id;

        @BeforeEach
        void beforeEach() {
            epic = new Epic("Эпик", "Комментарий к эпику");
            epicId = taskManager.createEpic(epic);
            subTask1 = new SubTask("Сабтаск 1", "Коммент к сабтаск1", States.DONE, epicId,
                    LocalDateTime.of(2025, Month.JANUARY, 1, 0, 0), Duration.ofDays(3));
            subTask1Id = taskManager.createSubTask(subTask1);
            subTask2 = new SubTask("Сабтаск2", "Комментарий к подзадаче", States.DONE, epicId,
                    LocalDateTime.of(2025, Month.JANUARY, 5, 0, 0),
                    Duration.ofDays(20));
            subTask2Id = taskManager.createSubTask(subTask2);
        }

        @DisplayName("Статус Эпика DONE, если все подзадачи DONE")
        @Test
        public void shouldEpicDONEIfAllSubTaskDONE() {
            epic = taskManager.getEpicById(epicId);

            assertEquals(States.DONE, epic.getState(), "Статус эпика неправильный!");
        }

        @DisplayName("Статус эпика автоматически перерасcчитывается c DONE на IN_PROGRESS")
        @Test
        public void shouldEpicStateAutoRecalc2InProgress() {
            subTask1 = new SubTask(subTask1Id, subTask1.getName(), subTask1.getDescription(),
                    States.IN_PROGRESS, epicId, subTask1.getStartTime(), subTask1.getDuration());
            taskManager.updateSubTask(subTask1);

            Epic epic = taskManager.getEpicById(epicId);

            assertEquals(States.IN_PROGRESS, epic.getState(), "Статус Эпика не перерассчитался");
        }

        @DisplayName("Статус эпика автоматически перерасcчитывается c DONE на NEW")
        @Test
        public void shouldEpicStateAutoRecalc2NEW() {
            subTask1 = new SubTask(subTask1Id, subTask1.getName(), subTask1.getDescription(),
                    States.NEW, epicId, LocalDateTime.of(2025, Month.FEBRUARY, 2, 0, 0),
                    subTask1.getDuration());
            taskManager.updateSubTask(subTask1);

            subTask2 = new SubTask(subTask2Id, subTask2.getName(), subTask2.getDescription(),
                    States.NEW, epicId, subTask2.getStartTime(), subTask2.getDuration());
            taskManager.updateSubTask(subTask2);

            Epic epic = taskManager.getEpicById(epicId);

            assertEquals(States.NEW, epic.getState(), "Статус Эпика не перерассчитался");
        }

        @DisplayName("Статус Эпика New после удаления всех выполненных задач")
        @Test
        public void shouldEpicNewAfterSubTaskListCleer() {
            taskManager.deleteAllSubTasks();
            epic = taskManager.getEpicById(epicId);

            assertEquals(States.NEW, epic.getState(), "Статус эпика неправильный!");
        }

        @DisplayName("Смена статуса Эпика сеттером не влияет на состояние Манагера")
        @Test
        public void shouldManagerStateNotChahgeAfetSetStateForEpic() {
            epic = taskManager.getEpicById(epicId);
            epic.setState(States.IN_PROGRESS);

            epic = taskManager.getEpicById(epicId);

            assertEquals(States.DONE, epic.getState(), "Повлиял");
            assertEquals(States.DONE, taskManager.getEpicList().getFirst().getState(), "Повлиял!");
        }

        @DisplayName("Смена статуса Эпика сеттером по ссылке из листа не влияет на состояние Манагера")
        @Test
        public void shouldManagerStateNotChahgeAfetSetStateForEpicFromList() {

            epic = taskManager.getEpicList().getFirst();
            epic.setState(States.IN_PROGRESS);
            epic = taskManager.getEpicById(epicId);

            assertEquals(States.DONE, epic.getState(), "Повлиял!");
            assertEquals(States.DONE, taskManager.getEpicList().getFirst().getState(), "Повлиял!");
        }
    }

    @DisplayName("Когда одна подзадача New, другая IN_Progress")
    @Nested
    class EpicNewAndInProgress {
        private Epic epic;
        private SubTask subTask1;
        private SubTask subTask2;
        private int epicId;
        private int subTask1Id;
        private int subTask2Id;
        LocalDateTime startDateTime = LocalDateTime.parse("01.01.2025 00:00", dateTimeFormatter);
        Duration duration = Duration.ofDays(3);

        @BeforeEach
        void beforeEach() {
            epic = new Epic("Эпик", "Комментарий к эпику");
            epicId = taskManager.createEpic(epic);
            subTask1 = new SubTask("Сабтаск 1", "Коммент к сабтаск1", States.NEW, epicId,
                    startDateTime, duration);
            subTask1Id = taskManager.createSubTask(subTask1);
            subTask2 = new SubTask("Сабтаск2", "Комментарий к подзадаче", States.IN_PROGRESS, epicId,
                    LocalDateTime.now().minusDays(10), Duration.ofDays(20));
            subTask2Id = taskManager.createSubTask(subTask2);
        }

        @DisplayName("Статус Эпика IN_PROGRESS")
        @Test
        public void shouldEpicInProgress() {
            epic = taskManager.getEpicById(epicId);

            assertEquals(States.IN_PROGRESS, epic.getState(), "Статус эпика неправильный!");
        }

        @DisplayName("Статус эпика автоматически перерасcчитывается c IN_PROGRESS на NEW")
        @Test
        public void shouldEpicStateAutoRecalc2InProgress() {
            subTask2 = new SubTask(subTask2Id, subTask2.getName(), subTask2.getDescription(),
                    States.NEW, epicId, subTask2.getStartTime(), subTask2.getDuration());
            taskManager.updateSubTask(subTask2);

            Epic epic = taskManager.getEpicById(epicId);

            assertEquals(States.NEW, epic.getState(), "Статус Эпика не перерассчитался");
        }

        @DisplayName("Статус Эпика New после удаления задачи IN_PROGRESS")
        @Test
        public void shouldEpicNewAfterDeleteSubTaskINProgress() {
            taskManager.deleteSubTaskById(subTask2Id);
            epic = taskManager.getEpicById(epicId);

            assertEquals(States.NEW, epic.getState(), "Статус эпика неправильный!");
        }

        @DisplayName("Статус эпика автоматически перерасcчитывается c IN_PROGRESS  на DONE")
        @Test
        public void shouldEpicStateAutoRecalc2Done() {
            subTask1 = new SubTask(subTask1Id, subTask1.getName(), subTask1.getDescription(),
                    States.DONE, epicId, subTask1.getStartTime(), subTask1.getDuration());
            taskManager.updateSubTask(subTask1);

            subTask2 = new SubTask(subTask2Id, subTask2.getName(), subTask2.getDescription(),
                    States.DONE, epicId, subTask2.getStartTime(), subTask2.getDuration());
            taskManager.updateSubTask(subTask2);

            Epic epic = taskManager.getEpicById(epicId);

            assertEquals(States.DONE, epic.getState(), "Статус Эпика не перерассчитался");
        }

        @DisplayName("Статус Эпика New после удаления всех задач")
        @Test
        public void shouldEpicNewAfterSubTaskListCleer() {
            taskManager.deleteAllSubTasks();
            epic = taskManager.getEpicById(epicId);

            assertEquals(States.NEW, epic.getState(), "Статус эпика неправильный!");
        }

        @DisplayName("Смена статуса Эпика сеттером не влияет на состояние Манагера")
        @Test
        public void shouldManagerStateNotChahgeAfetSetStateForEpic() {
            epic = taskManager.getEpicById(epicId);
            epic.setState(States.DONE);

            epic = taskManager.getEpicById(epicId);

            assertEquals(States.IN_PROGRESS, epic.getState(), "Повлиял");
            assertEquals(States.IN_PROGRESS, taskManager.getEpicList().getFirst().getState(), "Повлиял!");
        }

        @DisplayName("Смена статуса Эпика сеттером по ссылке из листа не влияет на состояние Манагера")
        @Test
        public void shouldManagerStateNotChahgeAfetSetStateForEpicFromList() {

            epic = taskManager.getEpicList().getFirst();
            epic.setState(States.DONE);
            epic = taskManager.getEpicById(epicId);

            assertEquals(States.IN_PROGRESS, epic.getState(), "Повлиял!");
            assertEquals(States.IN_PROGRESS, taskManager.getEpicList().getFirst().getState(), "Повлиял!");
        }
    }

    @Nested
    @DisplayName("Проверка эпиков в истории")
    class EpicHistory {
        private Epic epic1;
        private int epic1Id;
        private Epic epic2;
        private int epic2Id;

        @BeforeEach
        public void beforeEach() {
            epic1 = new Epic("Эпик 1", "Комментарий к эпику 1");
            epic2 = new Epic("Эпик 2", "Комментарий к эпику 2");
            epic1Id = taskManager.createEpic(epic1);
            epic2Id = taskManager.createEpic(epic2);
        }

        @DisplayName("Эпик  добавляется в HistoryManager и сохраняет свое состояние")
        @Test
        public void shouldEpicAddToHistoryAndSavePrevState() {
            epic1 = taskManager.getEpicById(epic1Id);
            epic2 = taskManager.getEpicById(epic2Id);
            epic1 = new Epic(epic1Id, "Изменил название эпика 1", "Изменил коммент эпика 1");
            taskManager.updateEpic(epic1);

            //отладка
            List<Task> ls = taskManager.getHistory();

            Task task = taskManager.getHistory().getFirst();

            assertEquals(2, taskManager.getHistory().size());
            assertEquals(task.getId(), epic1Id, "неправильный ID в истории");
            assertEquals(task.getName(), "Эпик 1", "Неправильное имя в истории");
            assertEquals(task.getDescription(), "Комментарий к эпику 1", "Неправильный комментарий в истории");
        }

        @DisplayName("Эпик  удаляется из истории при его удалении ")
        @Test
        public void shouldEpicRemoveFromHistoryIfItDelete() {
            epic1 = taskManager.getEpicById(epic1Id);
            epic2 = taskManager.getEpicById(epic2Id);

            taskManager.deleteEpicById(epic1Id);

            Task task = taskManager.getHistory().getFirst();

            assertEquals(1, taskManager.getHistory().size(), "Не удалился!");
            assertEquals(task.getId(), epic2Id, "Не тот удалился");
        }

        @DisplayName("При удалении всех эпиков они удаляются из истории")
        @Test
        public void shouldHistoryClearIfTaskListClear() {
            taskManager.getEpicById(epic1Id);
            taskManager.getEpicById(epic2Id);

            taskManager.deleteAllEpics();

            assertEquals(0, taskManager.getHistory().size(), "Не удалилась!");
        }
    }

    @DisplayName("Проверка подзадач")
    @Nested
    class CheckSubTasks {
        private Epic epic1;
        private Epic epic2;
        private int epic1Id;
        private int epic2Id;
        private SubTask subTask1;
        private SubTask subTask2;
        private int subTask1Id;
        private int subTask2Id;
        private LocalDateTime startDateTime1 = LocalDateTime.parse("01.01.2025 00:00", dateTimeFormatter);
        private LocalDateTime startDateTime2 = LocalDateTime.parse("10.01.2025 00:00", dateTimeFormatter);
        private Duration duration1 = Duration.ofDays(2);
        private Duration duration2 = Duration.ofDays(11);


        @BeforeEach
        public void beforeEach() {
            epic1 = new Epic("Эпик1", "Комментарий к Эпику 1");
            epic2 = new Epic("Эпик2", "Комментарий к Эпику 2");
            epic1Id = taskManager.createEpic(epic1);
            epic2Id = taskManager.createEpic(epic2);
            subTask1 = new SubTask("Подзадача1", "Коммент к подзадаче1", States.NEW, epic1Id,
                    startDateTime1, duration1);
            subTask2 = new SubTask("Подзадача2", "Коммент к подзадаче2", States.IN_PROGRESS, epic1Id,
                    startDateTime2, duration2);
            subTask1Id = taskManager.createSubTask(subTask1);
            subTask2Id = taskManager.createSubTask(subTask2);
        }

        @DisplayName("TaskManager действительно создает подзадачу")
        @Test
        public void shouldTaskManagerAddSubTask() {

            subTask1 = taskManager.getSubTaskById(subTask1Id);
            subTask2 = taskManager.getSubTaskById(subTask2Id);

            assertEquals(subTask1Id, subTask1.getId(), "Подзадача 1 не порождена!");
            assertEquals(subTask2Id, subTask2.getId(), "Подзадача 1 не порождена!");
        }

        @DisplayName("При добавлении подзадач корректно рассчитываются даты начала и дата конца Эпика")
        @Test
        public void shouldTaskManagerСorrectRecalcStartTimeAndAndTimeForEpicAfterInsertsSubTasks() {

            subTask1 = taskManager.getSubTaskById(subTask1Id);
            subTask2 = taskManager.getSubTaskById(subTask2Id);
            Epic epic = taskManager.getEpicById(subTask1.getParentEpic());

            assertEquals(LocalDateTime.of(2025, 1, 1, 0, 0),
                    epic.getStartTimeOptional().get(), "Дата начала Эпика рассчитана некорректно!");
            assertEquals(LocalDateTime.of(2025, 1, 21, 0, 0),
                    epic.getEndTimeOptional().get(), "Дата окончания Эпика рассчитана некорректно!");
            assertEquals(Duration.ofDays(13),
                    epic.getDuration(), "продолжительность эпика рассчитана некорректно");
        }

        @DisplayName("TaskManager действительно удаляет подзадачу")
        @Test
        public void shouldTaskManagerRemoveSubTask() {
            taskManager.deleteSubTaskById(subTask1Id);

            assertThrows(TaskNotFoundException.class, () -> {
                        subTask1 = taskManager.getSubTaskById(subTask1Id);
                    }, "Запрос удаленной задачи должен приводить к исключению TaskNotFoundException"
            );
            assertEquals(1, taskManager.getSubTaskList().size(), "Не удалилась задача");
            assertEquals(subTask2Id, taskManager.getSubTaskList().getFirst().getId(),
                    "удалилась не та подзадача");
        }

        @DisplayName("TaskManager действительно удаляет все подзадачи")
        @Test
        public void shouldTaskManagerRemoveAllSubTask() {
            taskManager.deleteAllSubTasks();

            assertThrows(TaskNotFoundException.class, () -> {
                subTask1 = taskManager.getSubTaskById(subTask1Id);
            }, "Запрос удаленной подзадачи должен приводить к исключению!");
            assertThrows(TaskNotFoundException.class, () -> {
                subTask1 = taskManager.getSubTaskById(subTask2Id);
            }, "Запрос удаленной подзадачи должен приводить к исключению!");
            assertEquals(0, taskManager.getSubTaskList().size(), "Подзадачи не удалены");
        }

        @DisplayName("При удалении подзадачи дата начала, дата окончания и duration эпика рассчитывается корректно")
        @Test
        public void shouldTaskManagerСorrectRecalcDatesAndDurationForEpicAfterRemoveSubTask() {

            subTask1 = taskManager.getSubTaskById(subTask1Id);
            subTask2 = taskManager.getSubTaskById(subTask2Id);
            taskManager.deleteSubTaskById(subTask1Id);
            Epic epic = taskManager.getEpicById(subTask2.getParentEpic());

            assertEquals(LocalDateTime.of(2025, 1, 10, 0, 0),
                    epic.getStartTimeOptional().get(), "Дата начала Эпика рассчитана некорректно!");
            assertEquals(LocalDateTime.of(2025, 1, 21, 0, 0),
                    epic.getEndTimeOptional().get(), "Дата окончания Эпика рассчитана некорректно!");
            assertEquals(Duration.ofDays(11),
                    epic.getDuration(), "продолжительность эпика рассчитана некорректно");
        }

        @DisplayName("TaskManager действительно изменяет подзадачу")
        @Test
        public void shouldTaskManagerUpdateSubTask() {
            subTask1 = new SubTask(subTask1Id, "Поменял имя", "Поменял коммент", States.DONE,
                    subTask1.getParentEpic(), subTask1.getStartTime().plusDays(1), subTask1.getDuration().minusDays(1));
            taskManager.updateSubTask(subTask1);
            subTask1 = taskManager.getSubTaskById(subTask1Id);
            SubTask subTask22 = taskManager.getSubTaskById(subTask2Id);

            assertEquals(subTask1Id, subTask1.getId(), "Подзадача 1 изменился ID!");
            assertEquals("Поменял имя", subTask1.getName(), "у подзадачи 1 не поменялось имя!");
            assertEquals("Поменял коммент", subTask1.getDescription(), "у подзадачи 1 не поменялся коммент!");
            assertEquals(States.DONE, subTask1.getState(), "у подзадачи 1 не поменялся статус!");
            assertEquals(LocalDateTime.parse("02.01.2025 00:00", dateTimeFormatter),
                    subTask1.getStartTime(), "Не поменялась дата начала");
            assertEquals(Duration.ofDays(1), subTask1.getDuration(), "Не поменялась длительность!");
            assertEquals(subTask2Id, subTask22.getId(), "поменялась подзадача2!!!");
            assertEquals(subTask2.getName(), subTask22.getName(), "поменялась подзадача2!!!");
            assertEquals(subTask2.getDescription(), subTask22.getDescription(), "поменялась подзадача2!!!");
            assertEquals(subTask2.getState(), subTask22.getState(), "поменялась подзадача2!!!");
            assertEquals(subTask2.getParentEpic(), subTask22.getParentEpic(), "поменялась подзадача2!!!");
        }

        @DisplayName("Подзадача с несуществующим эпиком не порождается")
        @Test
        public void shouldNotCreateSubTaskWithIncorrectEpic() {
            epic1Id = 666;
            subTask1 = new SubTask("Подзадача", "Коммент", States.DONE, epic1Id,
                    startDateTime1, duration1);
            assertThrows(TaskNotFoundException.class, () -> {
                int epic1Id = taskManager.createSubTask(subTask1);
            }, "Порождение подзадачи несуществующего Эпика должно приводить к исключению!");
        }

        @DisplayName("SubTask нельзя сделать своим же эпиком")
        @Test
        public void shouldNotSubTaskMakeItOwnEpic() {
            subTask1 = new SubTask(subTask1Id, subTask1.getName(), subTask1.getDescription(), subTask1.getState(),
                    subTask1Id, startDateTime1, duration1);

            assertThrows(TaskNotFoundException.class, () -> {
                taskManager.updateSubTask(subTask1);
            }, "Порождение подзадачи несуществующего Эпика должно приводить к исключению!");
        }

        @DisplayName("При обновлении подзадачи дата начала, дата окончания и duration эпика рассчитывается корректно")
        @Test
        public void shouldTaskManagerСorrectRecalcDatesAndDurationForEpicAfterUpdateSubTask() {

            subTask1 = taskManager.getSubTaskById(subTask1Id);
            subTask2 = taskManager.getSubTaskById(subTask2Id);
            subTask1 = new SubTask(subTask1Id, subTask1.getName(), subTask1.getDescription(), subTask1.getState(),
                    subTask1.getParentEpic(),
                    LocalDateTime.of(2025, Month.DECEMBER, 1, 0, 0),
                    Duration.ofDays(5));
            subTask2 = new SubTask(subTask2Id, subTask2.getName(), subTask2.getDescription(), subTask2.getState(),
                    subTask2.getParentEpic(),
                    LocalDateTime.of(2025, Month.APRIL, 15, 0, 0),
                    Duration.ofDays(14));
            subTask1Id = taskManager.updateSubTask(subTask1);
            subTask2Id = taskManager.updateSubTask(subTask2);

            Epic epic = taskManager.getEpicById(subTask2.getParentEpic());

            assertEquals(LocalDateTime.of(2025, Month.APRIL, 15, 0, 0),
                    epic.getStartTimeOptional().get(), "Дата начала Эпика рассчитана некорректно!");
            assertEquals(LocalDateTime.of(2025, Month.DECEMBER, 6, 0, 0),
                    epic.getEndTimeOptional().get(), "Дата окончания Эпика рассчитана некорректно!");
            assertEquals(Duration.ofDays(19),
                    epic.getDuration(), "продолжительность эпика рассчитана некорректно");
        }

        @DisplayName("Проверка истории")
        @Nested
        class checkHistory {
            private SubTask subTask3;
            private int subTask3Id;
            private LocalDateTime startDateTime3 = LocalDateTime.parse("01.03.2025 00:00", dateTimeFormatter);
            private Duration duration3 = Duration.ofDays(3);

            @BeforeEach
            public void beforeEach() {
                subTask3 = new SubTask("Подзадача 3", "коммент к подзадаче 3", States.DONE, epic2Id,
                        startDateTime3, duration3);
                subTask3Id = taskManager.createSubTask(subTask3);
            }

            @DisplayName("Подзадача добавляется в историю")
            @Test
            public void shouldSubTaskAddToHistory() {
                subTask1 = taskManager.getSubTaskById(subTask1Id);

                assertEquals(1, taskManager.getHistory().size(), "не добавилась в историю");
                assertEquals(subTask1Id, taskManager.getHistory().getFirst().getId(), "в историю добавиласт не та подзадача");
            }

            @DisplayName("Подзадача из истории удаляются при удалении подзадачи")
            @Test
            public void shouldHistoryClearWhenSubTaskDelete() {
                subTask1 = taskManager.getSubTaskById(subTask1Id);
                subTask2 = taskManager.getSubTaskById(subTask2Id);
                subTask3 = taskManager.getSubTaskById(subTask3Id);
                taskManager.deleteSubTaskById(subTask1Id);

                assertEquals(2, taskManager.getHistory().size(), "подзадача из истории не удалилась!");
                assertEquals(subTask2Id, taskManager.getHistory().getFirst().getId(), "Удалилась 2 подзадача");
                assertEquals(subTask3Id, taskManager.getHistory().getLast().getId(), "Удалилась 2 подзадача");
            }

            @DisplayName("Подзадачи из истории удаляются при очистке всех подзадач")
            @Test
            public void shouldHistoryClearWhenSubTaskListClear() {
                subTask1 = taskManager.getSubTaskById(subTask1Id);
                subTask2 = taskManager.getSubTaskById(subTask2Id);
                subTask3 = taskManager.getSubTaskById(subTask3Id);
                taskManager.deleteAllSubTasks();

                assertEquals(0, taskManager.getHistory().size(), "История не очистилась!");
            }

            @DisplayName("История не содержит повторов")
            @Test
            public void shouldHistoryCorrectAfterRepeat() {
                taskManager.getEpicById(epic1Id); //минус
                taskManager.getSubTaskById(subTask1Id); //минус
                taskManager.getSubTaskById(subTask2Id);
                taskManager.getEpicById(epic1Id);
                taskManager.getSubTaskById(subTask3Id);
                taskManager.getEpicById(epic2Id); //минус
                taskManager.getSubTaskById(subTask1Id);
                taskManager.getEpicById(epic2Id);

                assertEquals(5, taskManager.getHistory().size(), "История точно содержит повторы!");
                assertEquals(subTask2Id, taskManager.getHistory().get(0).getId(), "История формируется неверно!");
                assertEquals(epic1Id, taskManager.getHistory().get(1).getId(), "История формируется неверно!");
                assertEquals(subTask3Id, taskManager.getHistory().get(2).getId(), "История формируется неверно!");
                assertEquals(subTask1Id, taskManager.getHistory().get(3).getId(), "История формируется неверно!");
                assertEquals(epic2Id, taskManager.getHistory().get(4).getId(), "История формируется неверно!");
            }
        }
    }
}
