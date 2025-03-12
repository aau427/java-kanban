import managers.TaskManager;
import model.Task;
import referencebook.States;

public class Main {

    public static void main(String[] args) {
        TaskManager tm = new TaskManager();
        Task task1 = new Task("Задача 1", "Сходить в магазин", States.NEW);
        tm.createOrUpdateTask(task1);
        Task task2 = new Task("Задача 2", "Пообедать", States.NEW);
        tm.createOrUpdateTask(task2);
        Task task3 = new Task("Задача 3", "Выгулять собаку", States.NEW);
        tm.createOrUpdateTask(task3);
        System.out.println(tm.getTaskList());
        tm.deleteTaskById(2);
        System.out.println(tm.getTaskList());
        Task taskForUpdate = new Task(3, "Задача 3", "Не выгуливать пса, наказан!", States.NEW);
        tm.createOrUpdateTask(taskForUpdate);
        System.out.println(tm.getTaskById(3));
        tm.deleteAllTasks();
        System.out.println(tm.getTaskList());
        //не, так не дело.... нужно тесты писать...


    }
}
