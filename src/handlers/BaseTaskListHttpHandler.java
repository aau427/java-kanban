package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.BadRequestException;
import exception.NotSupportedMethodException;
import managers.TaskManager;

import java.io.IOException;

public abstract class BaseTaskListHttpHandler extends HttpExchangeUtility {

    protected final TaskManager manager;

    public BaseTaskListHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Обрабатываю запрос на history");
        String method = httpExchange.getRequestMethod();
        try {
            if (method.equals("GET")) {
                sendTaskList(httpExchange);
            } else {
                throw new NotSupportedMethodException(method + " не поддерживается нашим API!");
            }
        } catch (NotSupportedMethodException e) {
            sendMessage(httpExchange, 405, e.getMessage());
        } catch (BadRequestException e) {
            sendMessage(httpExchange, 400, e.getMessage());
        } catch (Exception e) {
            sendMessage(httpExchange, 500, e.getMessage());
        }
    }

    protected abstract void sendTaskList(HttpExchange httpExchange) throws IOException;
}
