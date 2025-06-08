package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.BadRequestException;
import exception.NotSupportedMethodException;
import managers.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public HistoryHandler(TaskManager manager) {
        super();
        this.manager = manager;
    }

    public void historyHandler(HttpExchange httpExchange) throws IOException {
        System.out.println("Обрабатываю запрос на history");
        String method = httpExchange.getRequestMethod();
        if (method.equals("GET")) {
            sendHistoryList(httpExchange);
        } else {
            throw new NotSupportedMethodException(method + " не поддерживается нашим API!");
        }
    }

    private void sendHistoryList(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Get");
        String[] pathItems = super.getPathItems(httpExchange);
        String gson;
        if (pathItems.length == 2) {
            List<Task> historyList = manager.getHistory();
            gson = super.prepareGson().toJson(historyList);
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        super.sendMessage(httpExchange, 200, gson);
    }
}
