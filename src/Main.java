import managers.TaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import referencebook.States;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        Task task1 = new Task("Задача 1", "Коммент к задаче 1", States.NEW);
        Task task2 = new Task("Задача 2", "Коммент к задаче 2", States.NEW);

        taskManager.createOrUpdateTask(task1);
        taskManager.createOrUpdateTask(task2);

        Epic epic1 = new Epic("Эпик1", "Комментарий к эпик 1"); //3
        Epic epic2 = new Epic("Эпик2", "Комментарий к эпик 2"); //4

        taskManager.createOrUpdateEpic(epic1);
        taskManager.createOrUpdateEpic(epic2);

        SubTask subTask1 = new SubTask("Подзадача1", "Комментарий к подзадаче 1", States.NEW, 3);
        SubTask subTask2 = new SubTask("Подзадача2", "Комментарий к подзадаче 2", States.NEW, 3);
        SubTask subTask3 = new SubTask("Подзадача3", "Комментарий к подзадаче 3", States.NEW, 4);

        taskManager.createOrUpdateSubTask(subTask1);//5
        taskManager.createOrUpdateSubTask(subTask2);//6
        taskManager.createOrUpdateSubTask(subTask3);//7

        System.out.println("Списки задач:");
        System.out.println(taskManager.getTaskList());
        System.out.println("Списки подзадач:");
        System.out.println(taskManager.getSubTaskList());
        System.out.println("Списки эпиков");
        System.out.println(taskManager.getEpicList());

        task1.setState(States.IN_PROGRESS);
        task2.setState(States.DONE);
        taskManager.createOrUpdateTask(task1);
        taskManager.createOrUpdateTask(task2);
        System.out.println("Проверяем статусы задач после изменения!");
        checkState(task1.getState(), States.IN_PROGRESS);
        checkState(task2.getState(), States.DONE);


        epic1.setState(States.DONE);
        epic2.setState(States.IN_PROGRESS);
        taskManager.createOrUpdateEpic(epic1);
        taskManager.createOrUpdateEpic(epic2);
        System.out.println("Проверяем статусы эпиков, они не должны измениться");
        checkState(epic1.getState(), States.NEW);
        checkState(epic2.getState(), States.NEW);

        subTask1.setState(States.IN_PROGRESS);
        subTask2.setState(States.DONE);
        subTask3.setState(States.DONE);
        taskManager.createOrUpdateSubTask(subTask1);
        taskManager.createOrUpdateSubTask(subTask2);
        taskManager.createOrUpdateSubTask(subTask3);
        System.out.println("Проверяем статусы subTask после изменений:");
        checkState(subTask1.getState(), States.IN_PROGRESS);
        checkState(subTask2.getState(), States.DONE);
        checkState(subTask3.getState(), States.DONE);
        System.out.println("Проверяем статусы эпиков после изменения статусов подзадач");
        checkState(epic1.getState(), States.IN_PROGRESS);
        checkState(epic2.getState(), States.DONE);
        System.out.println("Печатаю список задач после изменений статусов");
        System.out.println(taskManager.getSubTaskList());
        System.out.println("Печатаю список эпиков после изменений статусов");
        System.out.println(taskManager.getEpicList());
        System.out.println("Печатаю список подзадач после изменений статусов");
        System.out.println(taskManager.getSubTaskList());

        taskManager.deleteTaskById(task2.getId());
        if (taskManager.getTaskList().size() > 1) {
            System.out.println("Ошибка, задача не удалилась. ");
        }
        System.out.println("Печатаю список задач после удаления!");
        System.out.println(taskManager.getTaskList());

        taskManager.deleteEpicById(epic1.getId());
        if (taskManager.getEpicList().size() > 1) {
            System.out.println("Ошибка, эпик не удалился");
        }
        if (taskManager.getSubTaskList().size() > 1) {
            System.out.println("Ошибка, подзадачи эпика не удалились");
        }
        System.out.println("Печатаю список эпиков после удаления");
        System.out.println(taskManager.getEpicList());
        System.out.println("Печатаю список подзадач после удалений эпика");
        System.out.println(taskManager.getSubTaskList());
    }

    private static void checkState(States currentState, States expectedState) {
        if (currentState != expectedState) {
            System.out.println(String.format("Ошибка!!!! Ожидали: %s, получили: %s",
                    expectedState.name(), currentState.name()));
        } else {
            System.out.println(String.format("Все верно! Ожидали: %s, получили: %s",
                    expectedState.name(), currentState.name()));
        }
    }
}
