package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.BadRequestException;
import exception.NotSupportedMethodException;
import managers.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Обрабатываю запрос на prioritized");
        String method = httpExchange.getRequestMethod();
        if (method.equals("GET")) {
            sendPrioritizedList(httpExchange);
        } else {
            throw new NotSupportedMethodException(method + " не поддерживается нашим API!");
        }
    }

    private void sendPrioritizedList(HttpExchange httpExchange) throws IOException {
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
