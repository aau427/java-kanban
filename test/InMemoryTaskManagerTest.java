package test;

import common.Managers;
import history.HistoryManager;
import history.InMemoryHistoryManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.*;
import referencebook.States;
import taskmanager.FileBackedTaskManager;
import taskmanager.InMemoryTaskManager;
import taskmanager.TaskManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager taskManager;
    private final String fileName = System.getProperty("user.home") + "\\" + "test.csv";

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }

    @DisplayName("Managers  возвращает проинициализированный и готовый к работе HistoryManager")
    @Test
    public void shouldManagersReturnInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "HistoryManager не проинициализирован");
        assertEquals(InMemoryHistoryManager.class, historyManager.getClass(), "Объект не того класса");
    }

    @Nested
    @DisplayName("Проверка задач")
    class CheckTasks {
        private Task task;
        private int taskId;

        @BeforeEach
        public void beforeEach() {
            task = new Task("Задача №1", "Коммент к задаче", States.NEW);
            taskId = taskManager.createTask(task);
        }

        @DisplayName("TaskManager действительно добавляет задачу")
        @Test
        public void shouldTaskManagerAddTask() {

            task = taskManager.getTaskById(taskId);

            assertEquals(taskId, (int) task.getId(), "Задача 1 не порождена!");
        }

        @DisplayName("TaskManager действительно удаляет задачу")
        @Test
        public void shouldTaskManagerRemoveTask() {
            taskManager.deleteTaskById(taskId);
            task = taskManager.getTaskById(taskId);

            assertNull(task, "задача не удалена!");
        }

        @DisplayName("TaskManager действительно обновляет задачу")
        @Test
        public void shouldTaskManagerUpdateTask() {
            task = new Task(taskId, "изменил название", "изменил комментарий", States.DONE);
            boolean wasUpdated = taskManager.updateTask(task);
            task = taskManager.getTaskById(taskId);

            assertTrue(wasUpdated, "Не поменял");
            assertEquals("изменил название", task.getName(), "Не поменял название!");
            assertEquals("изменил комментарий", task.getDescription(), "Не поменял название!");
            assertEquals(States.DONE, task.getState(), "Не поменял статус!");

        }

        @DisplayName("Экземпляры Task равны друг другу, если равен их Id")
        @Test
        public void shouldTasksInstancesEqualsIfIdsIsEquals() {

            Task task1 = taskManager.getTaskById(taskId);

            assertEquals(task.getName(), task1.getName(), "Не равны наименования");
            assertEquals(task.getDescription(), task1.getDescription(), "Не равны комментарии");
            assertEquals(task.getState(), task1.getState(), "Не совпали статусы");
        }

        @DisplayName("Задача с заданным ID и сгенерированным ID не конфликтуют внутри менеджера")
        @Test
        public void shouldTasksWithSetIdAndGeneratedIdWillNotConflict() {
            Task task1 = new Task(taskId, "Какая-то другая задача", "Потихоньку едет крыша!!!!", States.DONE);

            int task1Id = taskManager.createTask(task1);

            assertNotEquals(taskId, task1Id, "Задачи конфликтуют внутри TaskManager!");
        }

        @Nested
        @DisplayName("Проверка задач в истории")
        class TaskHistory {
            private Task task1;
            private int task1Id;

            @BeforeEach
            public void beforeEach() {
                task1 = new Task(" Задача №1", "Комментарий к задачу 1", States.DONE);
                task1Id = taskManager.createTask(task1);
            }

            @DisplayName("Задача добавляется в HistoryManager и сохраняет свое состояние")
            @Test
            public void shouldTaskAddToHistoryAndSavePrevState() {
                task = taskManager.getTaskById(taskId);
                task1 = taskManager.getTaskById(task1Id);

                Task newTask = new Task(taskId, "Изменил название", "Изменил коммент", States.IN_PROGRESS);
                taskManager.updateTask(newTask);

                newTask = taskManager.getHistory().getFirst();

                assertEquals(2, taskManager.getHistory().size());
                assertEquals(taskId, newTask.getId(), "неправильный ID в истории");
                assertEquals(task.getName(), newTask.getName(), "Неправильное имя в истории");
                assertEquals(task.getDescription(), newTask.getDescription(), "Неправильный комментарий в истории");
                assertEquals(task.getState(), newTask.getState(), "Неправильное состояние в истории");
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
    }

    @Nested
    @DisplayName("Проверка Эпиков")
    class CheckEpics {
        private Epic epic;
        private int epicId;
        private SubTask subTask1;
        private int subTask1Id;
        private SubTask subTask2;
        private int subTask2Id;

        @BeforeEach
        void beforeEach() {
            epic = new Epic("Эпик № 1", "Комментарий к эпику  №1 ");
            epicId = taskManager.createEpic(epic);
        }

        @DisplayName("TaskManager действительно добавляет Эпик")
        @Test
        public void shouldTaskManagerAddEpic() {
            Epic epic1 = taskManager.getEpicById(epicId);

            assertEquals(epicId, (int) epic1.getId(), "Эпик 1 не порожден!");
        }

        @DisplayName("TaskManager действительно удаляет Эпик")
        @Test
        public void shouldTaskManagerRemoveEpic() {
            taskManager.deleteEpicById(epicId);
            epic = taskManager.getEpicById(epicId);

            assertNull(epic, "Эпик не удален!");
        }

        @DisplayName("TaskManager действительно обновляет Эпик")
        @Test
        public void shouldTaskManagerUpdateEpic() {
            epic = new Epic(epicId, "изменил название", "изменил комментарий");
            boolean wasUpdated = taskManager.updateEpic(epic);
            epic = taskManager.getEpicById(epicId);

            assertTrue(wasUpdated, "Не поменял");
            assertEquals("изменил название", epic.getName(), "Не поменял название!");
            assertEquals("изменил комментарий", epic.getDescription(), "Не поменял название!");
        }

        @DisplayName("Эпик нельзя сделать своей же подзадачей")
        @Test
        public void shouldNotEpicMakeIdOwnSubTask() {
            subTask1 = new SubTask(epicId, "Подазадач", "Комментарий", States.NEW, epicId);
            int subTask1Id = taskManager.createSubTask(subTask1);

            assertEquals(-1, subTask1Id, "Эпик нельзя сделать своей же подзадачей!");
        }

        @DisplayName("Экземпляры Epic равны друг другу, если равен их Id")
        @Test
        public void shouldEpicsInstancesEqualsIfIdsIsEquals() {
            Epic epic1 = taskManager.getEpicById(epicId);

            assertEquals(epic.getName(), epic1.getName(), "Не равны наименования");
            assertEquals(epic.getDescription(), epic1.getDescription(), "Не равны комментарии");
            assertEquals(epic.getState(), epic1.getState(), "Не совпали статусы");
        }

        @Nested
        @DisplayName("Когда все подзадачи у Эпика New")
        class EpicNew {
            @BeforeEach
            void beforeEach() {
                subTask1 = new SubTask("Сабтаск1", "Комментарий к подзадаче", States.NEW, epicId);
                subTask2 = new SubTask("Сабтаск2", "Комментарий к подзадаче", States.NEW, epicId);
                subTask1Id = taskManager.createSubTask(subTask1);
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
                taskManager.deleteSubTaskById(subTask1Id);

                epic = taskManager.getEpicById(epicId);

                Assertions.assertEquals(1, epic.getChildSubTasks().size());
                Assertions.assertEquals(subTask2Id, taskManager.getSubTaskList().getFirst().getId());
            }

            @DisplayName("Статус эпика автоматически перерасcчитывается c New на IN_PROGRESS")
            @Test
            public void shouldEpicStateAutoRecalc2InProgress() {
                subTask1 = new SubTask(subTask1Id, subTask1.getName(), subTask1.getDescription(),
                        States.IN_PROGRESS, epicId);
                taskManager.updateSubTask(subTask1);

                Epic epic = taskManager.getEpicById(epicId);

                assertEquals(States.IN_PROGRESS, epic.getState(), "Статус Эпика не перерассчитался");
            }

            @DisplayName("Статус эпика автоматически перерасcчитывается c New на DONE")
            @Test
            public void shouldEpicStateAutoRecalc2Done() {
                subTask1 = new SubTask(subTask1Id, subTask1.getName(), subTask1.getDescription(),
                        States.DONE, epicId);
                taskManager.updateSubTask(subTask1);

                subTask2 = new SubTask(subTask2Id, subTask2.getName(), subTask2.getDescription(),
                        States.DONE, epicId);
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
            @BeforeEach
            void beforeEach() {
                subTask1 = new SubTask("Сабтаск1", "Комментарий к подзадаче", States.DONE, epicId);
                subTask2 = new SubTask("Сабтаск2", "Комментарий к подзадаче", States.DONE, epicId);
                subTask1Id = taskManager.createSubTask(subTask1);
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
                        States.IN_PROGRESS, epicId);
                taskManager.updateSubTask(subTask1);

                Epic epic = taskManager.getEpicById(epicId);

                assertEquals(States.IN_PROGRESS, epic.getState(), "Статус Эпика не перерассчитался");
            }

            @DisplayName("Статус эпика автоматически перерасcчитывается c DONE на NEW")
            @Test
            public void shouldEpicStateAutoRecalc2NEW() {
                subTask1 = new SubTask(subTask1Id, subTask1.getName(), subTask1.getDescription(),
                        States.NEW, epicId);
                taskManager.updateSubTask(subTask1);

                subTask2 = new SubTask(subTask2Id, subTask2.getName(), subTask2.getDescription(),
                        States.NEW, epicId);
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

        @DisplayName("Когда одна подpадача New, другая IN_Progress")
        @Nested
        class EpicNewAndInProgress {
            @BeforeEach
            void beforeEach() {
                subTask1 = new SubTask("Сабтаск1", "Комментарий к подзадаче", States.NEW, epicId);
                subTask2 = new SubTask("Сабтаск2", "Комментарий к подзадаче", States.IN_PROGRESS, epicId);
                subTask1Id = taskManager.createSubTask(subTask1);
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
                        States.NEW, epicId);
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
                        States.DONE, epicId);
                taskManager.updateSubTask(subTask1);

                subTask2 = new SubTask(subTask2Id, subTask2.getName(), subTask2.getDescription(),
                        States.DONE, epicId);
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

            @BeforeEach
            public void beforeEach() {
                epic1 = new Epic("Эпик №1", "Комментарий к эпику 1");
                epic1Id = taskManager.createEpic(epic1);
            }

            @DisplayName("Эпик  добавляется в HistoryManager и сохраняет свое состояние")
            @Test
            public void shouldEpicAddToHistoryAndSavePrevState() {
                epic = taskManager.getEpicById(epicId);
                epic1 = taskManager.getEpicById(epic1Id);

                Epic newEpic = new Epic(epicId, "Изменил название", "Изменил коммент");
                taskManager.updateEpic(newEpic);

                Task task = taskManager.getHistory().getFirst();

                assertEquals(2, taskManager.getHistory().size());
                assertEquals(task.getId(), epic.getId(), "неправильный ID в истории");
                assertEquals(task.getName(), epic.getName(), "Неправильное имя в истории");
                assertEquals(task.getDescription(), epic.getDescription(), "Неправильный комментарий в истории");
                assertEquals(task.getState(), epic.getState(), "Неправильное состояние в истории");
            }

            @DisplayName("Эпик  удаляется из истории при его удалении ")
            @Test
            public void shouldEpicRemoveFromHistoryIfItDelete() {
                epic = taskManager.getEpicById(epicId);
                epic1 = taskManager.getEpicById(epic1Id);

                taskManager.deleteEpicById(epicId);

                Task task = taskManager.getHistory().getFirst();

                assertEquals(1, taskManager.getHistory().size(), "Не удалился!");
                assertEquals(task.getId(), epic1.getId(), "Не тот удалился");
            }

            @DisplayName("При удалении всех эпиков они удаляются из истории")
            @Test
            public void shouldHistoryClearIfTaskListClear() {
                taskManager.getEpicById(epicId);
                taskManager.getEpicById(epic1Id);

                taskManager.deleteAllEpics();

                assertEquals(0, taskManager.getHistory().size(), "Не удалилась!");
            }
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

        @BeforeEach
        public void beforeEach() {
            epic1 = new Epic("Эпик1", "Комментарий к Эпику 1");
            epic2 = new Epic("Эпик2", "Комментарий к Эпику 2");
            epic1Id = taskManager.createEpic(epic1);
            epic2Id = taskManager.createEpic(epic2);
            subTask1 = new SubTask("Подзадача1", "Коммент к подзадаче1", States.NEW, epic1Id);
            subTask2 = new SubTask("Подзадача2", "Коммент к подзадаче2", States.IN_PROGRESS, epic1Id);
            subTask1Id = taskManager.createSubTask(subTask1);
            subTask2Id = taskManager.createSubTask(subTask2);
        }

        @DisplayName("TaskManager действительно добавляет подзадачу")
        @Test
        public void shouldTaskManagerAddSubTask() {

            subTask1 = taskManager.getSubTaskById(subTask1Id);
            subTask2 = taskManager.getSubTaskById(subTask2Id);

            assertEquals(subTask1Id, subTask1.getId(), "Подзадача 1 не порождена!");
            assertEquals(subTask2Id, subTask2.getId(), "Подзадача 1 не порождена!");
        }

        @DisplayName("TaskManager действительно удаляет подзадачу")
        @Test
        public void shouldTaskManagerRemoveSubTask() {
            taskManager.deleteSubTaskById(subTask1Id);
            subTask1 = taskManager.getSubTaskById(subTask1Id);
            subTask2 = taskManager.getSubTaskById(subTask2Id);

            assertNull(subTask1, "Подзадача 1 не удалена!");
            assertNotEquals(null, subTask2, "Ошибочно удалена подзадача2");
        }

        @DisplayName("TaskManager действительно удаляет все подзадачи")
        @Test
        public void shouldTaskManagerRemoveAllSubTask() {
            taskManager.deleteAllSubTasks();
            subTask1 = taskManager.getSubTaskById(subTask1Id);
            subTask2 = taskManager.getSubTaskById(subTask2Id);

            assertNull(subTask1, "Подзадача 1 не удалена!");
            assertNull(subTask2, "Подзадача 2 не  удалена ");
            assertEquals(0, taskManager.getSubTaskList().size(), "Подзадачи не удалены");
        }

        @DisplayName("TaskManager действительно изменяет подзадачу")
        @Test
        public void shouldTaskManagerUpdateSubTask() {
            subTask1 = new SubTask(subTask1Id, "Поменял имя", "Поменял коммент", States.DONE, subTask1.getParentEpic());
            taskManager.updateSubTask(subTask1);
            subTask1 = taskManager.getSubTaskById(subTask1Id);
            SubTask subTask22 = taskManager.getSubTaskById(subTask2Id);

            assertEquals(subTask1Id, subTask1.getId(), "Подзадача 1 изменился ID!");
            assertEquals("Поменял имя", subTask1.getName(), "у подзадачи 1 не поменялось имя!");
            assertEquals("Поменял коммент", subTask1.getDescription(), "у подзадачи 1 не поменялся коммент!");
            assertEquals(States.DONE, subTask1.getState(), "у подзадачи 1 не поменялся статус!");
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
            SubTask subTask = new SubTask("Подзадача", "Коммент", States.DONE, epic1Id);
            int isCreate = taskManager.createSubTask(subTask);

            assertNotEquals(1, isCreate, "Подзадача с несуществующим эпиком породилась!");
        }

        @DisplayName("SubTask нельзя сделать своим же эпиком")
        @Test
        public void shouldNotSubTaskMakeItOwnEpic() {
            subTask1 = new SubTask(subTask1Id, subTask1.getName(), subTask1.getDescription(), subTask1.getState(), subTask1Id);
            boolean isDone = taskManager.updateSubTask(subTask1);

            assertFalse(isDone, "Сделал подзадачу своим же эпиком!");
        }

        @DisplayName("Проверка истории")
        @Nested
        class checkHistory {
            private SubTask subTask3;
            private int subTask3Id;

            @BeforeEach
            public void beforeEach() {
                subTask3 = new SubTask("Подзадача 3", "коммент к подзадаче 3", States.DONE, epic2Id);
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