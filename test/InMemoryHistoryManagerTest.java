package test;

import common.Managers;
import history.HistoryManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import referencebook.States;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    @DisplayName("Задача добавляется в HistoryManager")
    void shouldTaskAdd2History() {
        Task task = new Task(666, "Задача", "Ее коммент", States.IN_PROGRESS);
        historyManager.addTask(task);

        Assertions.assertEquals(1, historyManager.getHistoryList().size());
        Assertions.assertEquals(666, (int) historyManager.getHistoryList().getFirst().getId());
    }

    @Test
    @DisplayName("Эпик добавляется в HistoryManager")
    void shouldEpicAdd2History() {
        Epic epic = new Epic(666, "Эпик", "Ее коммент");
        historyManager.addTask(epic);

        Assertions.assertEquals(1, historyManager.getHistoryList().size());
        Assertions.assertEquals(666, (int) historyManager.getHistoryList().getFirst().getId());
    }

    @Test
    @DisplayName("Подзадача добавляется в HistoryManager")
    void shouldSubTaskAdd2History() {
        SubTask subTask = new SubTask(666, "Подзадача", "Коммент", States.IN_PROGRESS, 777);
        historyManager.addTask(subTask);

        Assertions.assertEquals(1, historyManager.getHistoryList().size());
        Assertions.assertEquals(666, (int) historyManager.getHistoryList().getFirst().getId());
    }

    @Test
    @DisplayName("Задача удаляется из HistoryManager")
    void shouldTaskRemoveFromHistory() {
        Task task1 = new Task(1, "Задача1", "коммент1", States.IN_PROGRESS);
        Task task2 = new Task(2, "Задача2", "коммент2", States.IN_PROGRESS);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.remove(task1.getId());

        Assertions.assertEquals(1, historyManager.getHistoryList().size());
        Assertions.assertEquals(2, historyManager.getHistoryList().getFirst().getId());
    }

    @Test
    @DisplayName("Подзадача удаляется из HistoryManager")
    void shouldSubTaskRemoveFromHistory() {
        SubTask subTask1 = new SubTask(1, "Подзадача1", "Коммент", States.IN_PROGRESS, 777);
        SubTask subTask2 = new SubTask(2, "Подзадача2", "Коммент", States.IN_PROGRESS, 777);

        historyManager.addTask(subTask1);
        historyManager.addTask(subTask2);
        historyManager.remove(subTask1.getId());

        Assertions.assertEquals(1, historyManager.getHistoryList().size());
        Assertions.assertEquals(2, (int) historyManager.getHistoryList().getFirst().getId());
    }

    @Test
    @DisplayName("Эпик удаляется из HistoryManager")
    void shouldEpicRemoveFromHistory() {
        Epic epic1 = new Epic(1, "Эпик1", "Коммент");
        Epic epic2 = new Epic(2, "Эпик2", "Коммент2");
        Epic epic3 = new Epic(3, "Эпик3", "Коммент3");


        historyManager.addTask(epic1);
        historyManager.addTask(epic2);
        historyManager.addTask(epic3);
        historyManager.remove(epic2.getId());

        Assertions.assertEquals(2, historyManager.getHistoryList().size());
        Assertions.assertEquals(1, (int) historyManager.getHistoryList().get(0).getId());
        Assertions.assertEquals(3, (int) historyManager.getHistoryList().get(1).getId());
    }


    @DisplayName("История возвращается в List")
    @Test
    void shouldGetHistoryList() {
        Task task = new Task(1, "Задача1", "коммент1", States.IN_PROGRESS);
        Epic epic = new Epic(2, "Эпик1", "Коммент");
        SubTask subTask = new SubTask(3, "Подзадача1", "Коммент", States.IN_PROGRESS, 2);

        historyManager.addTask(task);
        historyManager.addTask(epic);
        historyManager.addTask(subTask);

        Assertions.assertEquals(3, historyManager.getHistoryList().size());
        Assertions.assertEquals(1, historyManager.getHistoryList().get(0).getId());
        Assertions.assertEquals(2, historyManager.getHistoryList().get(1).getId());
        Assertions.assertEquals(3, historyManager.getHistoryList().get(2).getId());
    }


}