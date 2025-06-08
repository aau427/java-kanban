package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.BadRequestException;
import exception.NotSupportedMethodException;
import managers.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public PrioritizedHandler(TaskManager manager) {
        super();
        this.manager = manager;
    }

    public void prioritizedHandler(HttpExchange httpExchange) throws IOException {
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
        String[] pathItems = super.getPathItems(httpExchange);
        String gson;
        if (pathItems.length == 2) {
            List<Task> prioritizedTasksList = manager.getPrioritizedTasks();
            gson = super.prepareGson().toJson(prioritizedTasksList);
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        super.sendMessage(httpExchange, 200, gson);
    }
}
