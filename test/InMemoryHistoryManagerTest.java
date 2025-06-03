package test;

import common.Managers;
import history.HistoryManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import referencebook.States;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();
    }

    @Nested
    class checkTasks {
        private Task task1;
        private Task task2;
        private int task1Id = 1;
        private int task2Id = 2;

        @BeforeEach
        public void beforeEach() {
            task1 = new Task(task1Id, "Задача1", "коммент1", States.IN_PROGRESS,
                    LocalDateTime.of(2025, Month.JUNE, 13, 0, 0),
                    Duration.ofDays(10));
            task2 = new Task(task2Id, "Задача2", "коммент2", States.IN_PROGRESS,
                    LocalDateTime.of(2025, Month.APRIL, 1, 0, 0),
                    Duration.ofDays(10));
        }

        @Test
        @DisplayName("Задача добавляется в HistoryManager")
        void shouldTaskAdd2History() {
            historyManager.addTask(task1);

            assertEquals(1, historyManager.getHistoryList().size());
            assertEquals(task1Id, (int) historyManager.getHistoryList().getFirst().getId());
        }

        @Test
        @DisplayName("Задача удаляется из HistoryManager")
        void shouldTaskRemoveFromHistory() {
            historyManager.addTask(task1);
            historyManager.addTask(task2);

            historyManager.remove(task1Id);

            assertEquals(1, historyManager.getHistoryList().size());
            assertEquals(task2Id, (int) historyManager.getHistoryList().getFirst().getId());
        }
    }

    @Nested
    class checkEpics {
        private Epic epic1;
        private Epic epic2;
        private int epic1Id = 1;
        private int epic2Id = 2;

        @BeforeEach
        public void beforeEach() {
            epic1 = new Epic(epic1Id, "Эпик1", "Коммент1");
            epic2 = new Epic(epic2Id, "Эпик2", "Коммент2");
        }

        @Test
        @DisplayName("Эпик добавляется в историю")
        void shouldEpicAdd2History() {
            historyManager.addTask(epic1);

            assertEquals(1, historyManager.getHistoryList().size());
            assertEquals(epic1Id, (int) historyManager.getHistoryList().getFirst().getId());
        }

        @Test
        @DisplayName("Эпик удаляется из истории")
        void shouldEpicRemoveFromHistory() {
            historyManager.addTask(epic1);
            historyManager.addTask(epic2);
            historyManager.remove(epic1Id);

            assertEquals(1, historyManager.getHistoryList().size());
            assertEquals(epic2Id, (int) historyManager.getHistoryList().getFirst().getId());
        }
    }

    @Nested
    class checkSubTasks {
        private SubTask subTask1;
        private SubTask subTask2;
        private int subTask1Id = 1;
        private int subTask2Id = 2;

        @BeforeEach
        public void beforeEach() {
            subTask1 = new SubTask(subTask1Id, "Подзадача1", "Коммент1",
                    States.IN_PROGRESS, 777,
                    LocalDateTime.of(2025, Month.JUNE, 13, 0, 0),
                    Duration.ofDays(10));
            subTask2 = new SubTask(subTask2Id, "Подзадача2", "Коммент2",
                    States.IN_PROGRESS, 777,
                    LocalDateTime.of(2025, Month.APRIL, 13, 0, 0),
                    Duration.ofDays(2));
        }

        @Test
        @DisplayName("Подзадача добавляется в историю")
        void shouldSubTaskAdd2History() {
            historyManager.addTask(subTask1);

            assertEquals(1, historyManager.getHistoryList().size());
            assertEquals(subTask1Id, (int) historyManager.getHistoryList().getFirst().getId());
        }

        @Test
        @DisplayName("Подзадача удаляется из историю")
        void shouldSubRemoveFromHistory() {
            historyManager.addTask(subTask1);
            historyManager.addTask(subTask2);

            historyManager.remove(subTask2Id);

            assertEquals(1, historyManager.getHistoryList().size());
            assertEquals(subTask1Id, (int) historyManager.getHistoryList().getFirst().getId());
        }
    }

    @DisplayName("HistoryList формируется корректно для разных задач")
    @Test
    void shouldGetHistoryList() {
        Task task = new Task(1, "Задача1", "коммент1", States.IN_PROGRESS,
                LocalDateTime.of(2025, Month.JANUARY, 10, 0, 0), Duration.ofDays(1));
        Epic epic = new Epic(2, "Эпик1", "Коммент");
        SubTask subTask = new SubTask(3, "Подзадача1", "Коммент", States.IN_PROGRESS,
                2, LocalDateTime.of(2025, Month.FEBRUARY, 10, 0, 0),
                Duration.ofDays(8));

        historyManager.addTask(task);
        historyManager.addTask(epic);
        historyManager.addTask(subTask);

        assertEquals(3, historyManager.getHistoryList().size());
        assertEquals(1, historyManager.getHistoryList().get(0).getId());
        assertEquals(2, historyManager.getHistoryList().get(1).getId());
        assertEquals(3, historyManager.getHistoryList().get(2).getId());
    }

}