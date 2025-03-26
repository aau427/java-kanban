import managers.Managers;
import managers.TaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import referencebook.States;

public class Main {
    private static int numErrors = 0;

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefaultTaskManager();
        //тесты спринту 5
        Task task11 = new Task("Задача 11", "Коммент к задаче 11", States.NEW);
        Task task12 = new Task("Задача 12", "Коммент к задаче 12", States.DONE);
        taskManager.createTask(task11);
        taskManager.createTask(task12);

        Epic epic11 = new Epic("Эпик11", "Комментарий к эпик 11");
        Epic epic12 = new Epic("Эпик12", "Комментарий к эпик 12");
        taskManager.createEpic(epic11);
        taskManager.createEpic(epic12);

        SubTask subTask11 = new SubTask("Подзадача 11", "Коммент", States.IN_PROGRESS, epic11.getId());
        taskManager.createSubTask(subTask11);

        task11 = taskManager.getTaskById(task11.getId());
        task12 = taskManager.getTaskById(task12.getId());
        epic11 = taskManager.getEpicById(epic11.getId());
        subTask11 = taskManager.getSubTaskById(subTask11.getId());
        printAll(taskManager);

        taskManager.deleteAllTasks();
        taskManager.deleteAllSubTasks();
        taskManager.deleteAllEpics();

        //тесты спринт 4


        Task task1 = new Task("Задача 1", "Коммент к задаче 1", States.NEW);
        Task task2 = new Task("Задача 2", "Коммент к задаче 2", States.NEW);

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        Task task3 = taskManager.getTaskById(task1.getId());
        System.out.println("Печатаю историю, т.к. посмотрели Задачу 1");
        System.out.println(taskManager.getHistory());


        Epic epic1 = new Epic("Эпик1", "Комментарий к эпик 1");
        Epic epic2 = new Epic("Эпик2", "Пустой эпик");

        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);
        if (taskManager.getEpicList().size() != 2) {
            System.out.println("Ошибка! количество созданных Эпиков <> 2");
            numErrors++;
        }

        SubTask subTask1 = new SubTask("Подзадача1", "Комментарий к подзадаче 1", States.NEW, epic1.getId());
        SubTask subTask2 = new SubTask("Подзадача2", "Комментарий к подзадаче 2", States.NEW, epic1.getId());
        SubTask subTask3 = new SubTask("Подзадача3", "Комментарий к подзадаче 3", States.IN_PROGRESS, epic1.getId());

        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);
        if (taskManager.getSubTaskList().size() != 3) {
            System.out.println("Ошибка! количество созданных подзадач <> 3");
        }
        epic1 = taskManager.getEpicById(epic1.getId());
        epic2 = taskManager.getEpicById(epic2.getId());
        checkState(epic1.getState(), States.IN_PROGRESS);
        checkState(epic2.getState(), States.NEW);

        System.out.println("Создал задачи: ");
        System.out.println(taskManager.getTaskList());
        System.out.println("Создал  подзадачи:");
        System.out.println(taskManager.getSubTaskList());
        System.out.println("Создал эпики");
        System.out.println(taskManager.getEpicList());

        System.out.println("Поменяем у подзадачи 3 эпик. Был 1, станет 2");
        subTask3 = createSubTaskCopy(subTask3);
        subTask3.setParentEpic(epic2.getId());
        subTask3.setDescription("Поменял эпик. Был 1, стал 2!");
        taskManager.updateSubTask(subTask3);
        subTask3 = taskManager.getSubTaskById(subTask3.getId());
        if (!subTask3.getDescription().equals("Поменял эпик. Был 1, стал 2!")) {
            System.out.println("Ошибка! не смог изменить комментарий к Эпику!");
            numErrors++;
        }
        epic1 = taskManager.getEpicById(epic1.getId());
        epic2 = taskManager.getEpicById(epic2.getId());
        checkState(epic1.getState(), States.NEW);
        checkState(epic2.getState(), States.IN_PROGRESS);
        if (!epic1.getChildSubTasks().contains(subTask1.getId())
                || !epic1.getChildSubTasks().contains(subTask2.getId())
                || epic1.getChildSubTasks().size() != 2) {
            System.out.println("Ошибка! Криво изменил эпик у подзадачи, поехал Эпик 1!");
            numErrors++;
        }
        if (!epic2.getChildSubTasks().contains(subTask3.getId())
                || epic2.getChildSubTasks().size() != 1) {
            System.out.println("Ошибка! криво поменял эпик у подзадачи! поехал Эпик2");
        }
        System.out.println(taskManager.getEpicList());

        task1 = createTaskCopy(task1);
        task1.setState(States.IN_PROGRESS);
        task2 = createTaskCopy(task2);
        task2.setState(States.DONE);
        taskManager.updateTask(task1);
        taskManager.updateTask(task2);
        System.out.println("Проверяем статусы задач после изменения!");
        task1 = taskManager.getTaskById(task1.getId());
        task2 = taskManager.getTaskById(task2.getId());
        checkState(task1.getState(), States.IN_PROGRESS);
        checkState(task2.getState(), States.DONE);

        epic1 = createEpicCopy(epic1);
        epic2 = createEpicCopy(epic2);
        epic1.setState(States.DONE);
        epic2.setState(States.IN_PROGRESS);
        taskManager.updateEpic(epic1);
        taskManager.updateEpic(epic2);
        epic1 = taskManager.getEpicById(epic1.getId());
        epic2 = taskManager.getEpicById(epic2.getId());
        System.out.println("Проверяем статусы эпиков, они не должны измениться");
        checkState(epic1.getState(), States.NEW);
        checkState(epic2.getState(), States.IN_PROGRESS);

        subTask1 = createSubTaskCopy(subTask1);
        subTask2 = createSubTaskCopy(subTask2);
        subTask1.setState(States.IN_PROGRESS);
        subTask2.setState(States.DONE);
        subTask3.setState(States.DONE);

        taskManager.updateSubTask(subTask1);
        taskManager.updateSubTask(subTask2);
        taskManager.updateSubTask(subTask3);
        subTask1 = taskManager.getSubTaskById(subTask1.getId());
        subTask2 = taskManager.getSubTaskById(subTask2.getId());
        subTask3 = taskManager.getSubTaskById(subTask3.getId());
        System.out.println("Проверяем статусы subTask после изменений:");
        checkState(subTask1.getState(), States.IN_PROGRESS);
        checkState(subTask2.getState(), States.DONE);
        checkState(subTask3.getState(), States.DONE);
        System.out.println("Проверяем статусы эпиков после изменения статусов подзадач");
        epic1 = taskManager.getEpicById(epic1.getId());
        epic2 = taskManager.getEpicById(epic2.getId());
        checkState(epic1.getState(), States.IN_PROGRESS);
        checkState(epic2.getState(), States.DONE);
        System.out.println("Печатаю список задач после изменений статусов");
        System.out.println(taskManager.getSubTaskList());
        System.out.println("Печатаю список эпиков после изменений статусов");
        System.out.println(taskManager.getEpicList());
        System.out.println("Печатаю список подзадач после изменений статусов");
        System.out.println(taskManager.getSubTaskList());

        int task2Id = task2.getId();
        taskManager.deleteTaskById(task2Id);
        if (taskManager.getTaskById(task2Id) != null) {
            System.out.println(String.format("Ошибка, задача № %d не удалилась!", task2Id));
            numErrors++;
        }
        System.out.println("Печатаю список задач после удаления!");
        System.out.println(taskManager.getTaskList());

        int epic1Id = epic1.getId();
        taskManager.deleteEpicById(epic1Id);
        if (taskManager.getEpicById(epic1Id) != null) {
            System.out.println(String.format("Ошибка, эпик № %d не удалился!", epic1Id));
            numErrors++;
        }

        if (taskManager.getSubTaskList().size() > 1) {
            System.out.println("Ошибка, подзадачи эпика не удалились");
            numErrors++;
        }
        System.out.println("Печатаю список эпиков после удаления");
        System.out.println(taskManager.getEpicList());
        System.out.println("Печатаю список подзадач после удалений эпика");
        System.out.println(taskManager.getSubTaskList());

        System.out.println("Удаляю все задачи!");
        taskManager.deleteAllTasks();
        if (taskManager.getTaskList().size() > 0) {
            System.out.println("Ошибка! не все задачи удалились!");
            numErrors++;
        }

        System.out.println("Удаляю все подзадачи!");
        taskManager.deleteAllSubTasks();
        if (taskManager.getSubTaskList().size() > 0) {
            System.out.println("Ошибка! не все подзадачи удалились!");
            numErrors++;
        }
        epic2 = taskManager.getEpicById(epic2.getId());
        if (epic2.getChildSubTasks().size() > 0) {
            System.out.println("Ошибка! некорректно удалил подзадачи, оставил ссылки в Эпик2");
            numErrors++;
        }
        checkState(epic2.getState(), States.NEW);

        if (numErrors > 0) {
            System.out.println(String.format("Количество ошибок = %d", numErrors));
            System.out.println(String.format("Внимание! Количество ошибок = %d", numErrors));
        } else {
            System.out.println("Все тесты прошли отлично, Андрей!");
        }
    }

    private static void checkState(States currentState, States expectedState) {
        if (currentState != expectedState) {
            System.out.println(String.format("Ошибка!!!! Ожидали: %s, получили: %s",
                    expectedState.name(), currentState.name()));
            numErrors++;
        } else {
            System.out.println(String.format("Все верно! Ожидали: %s, получили: %s",
                    expectedState.name(), currentState.name()));
        }
    }

    private static Task createTaskCopy(Task task) {
        Task newTask = new Task(task.getName(), task.getDescription(), task.getState());
        newTask.setId(task.getId());
        return newTask;
    }

    private static Epic createEpicCopy(Epic epic) {
        Epic newEpic = new Epic(epic.getName(), epic.getDescription());
        newEpic.setId(epic.getId());
        newEpic.setState(epic.getState());
        for (Integer childSubTaskId : epic.getChildSubTasks()) {
            newEpic.getChildSubTasks().add(childSubTaskId);
        }
        return newEpic;
    }

    private static SubTask createSubTaskCopy(SubTask subTask) {
        SubTask newSubTask = new SubTask(subTask.getName(), subTask.getDescription(), subTask.getState(), subTask.getParentEpic());
        newSubTask.setId(subTask.getId());
        return newSubTask;
    }

    private static void printAll(TaskManager taskManager) {
        System.out.println("список задач");
        System.out.println(taskManager.getTaskList());
        System.out.println("Список эпиков");
        System.out.println(taskManager.getEpicList());
        System.out.println("Список подзадач");
        System.out.println(taskManager.getSubTaskList());
        System.out.println("История просмотра без одного Эпика 12");
        System.out.println(taskManager.getHistory());
    }
}
