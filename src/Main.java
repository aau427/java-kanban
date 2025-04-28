import common.Managers;
import model.Epic;
import referencebook.States;
import taskmanager.TaskManager;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefaultTaskManager();
        Epic epic1 = new Epic("Эпик1", "Комментарий 1");
        int epic1Id = taskManager.createEpic(epic1);

        epic1  = taskManager.getEpicList().get(0);

        epic1.setState(States.DONE);


        System.out.println(taskManager.getEpicList());

    }

}
