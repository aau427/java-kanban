package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.BadRequestException;
import managers.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseTaskListHttpHandler {

    public PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    protected void sendTaskList(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Get");
        String[] pathItems = getPathItems(httpExchange);
        String gson;
        if (pathItems.length == 2) {
            List<Task> prioritizedTasksList = manager.getPrioritizedTasks();
            gson = GSON.toJson(prioritizedTasksList);
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        sendMessage(httpExchange, 200, gson);
    }
}
