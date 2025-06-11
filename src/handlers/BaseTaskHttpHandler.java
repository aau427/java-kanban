package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.*;
import managers.TaskManager;

import java.io.IOException;

public abstract class BaseTaskHttpHandler extends HttpExchangeUtility {

    protected final TaskManager manager;

    public BaseTaskHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Обрабатываю запрос на /epics");
        String method = httpExchange.getRequestMethod();
        try {
            switch (method) {
                case "GET":
                    sendTaskOrTaskList(httpExchange);
                    break;
                case "POST":
                    createOrUpdateTask(httpExchange);
                    break;
                case "DELETE":
                    deleteTaskOrAllTasks(httpExchange);
                    break;
                default:
                    System.out.println("Хрен знает, что за метод");
                    throw new NotSupportedMethodException(method + " не поддерживается нашим API!");
            }
        } catch (NotSupportedMethodException e) {
            sendMessage(httpExchange, 405, e.getMessage());
        } catch (TaskNotFoundException e) {
            sendMessage(httpExchange, 404, e.getMessage());
        } catch (NoBodyException | BadRequestException e) {
            sendMessage(httpExchange, 400, e.getMessage());
        } catch (LogicalErrorException e) {
            sendMessage(httpExchange, 422, e.getMessage());
        } catch (ManagerIntervalException e) {
            sendMessage(httpExchange, 406, e.getMessage());
        } catch (Exception e) {
            sendMessage(httpExchange, 500, e.getMessage());
        }
    }

    protected abstract void sendTaskOrTaskList(HttpExchange httpExchange) throws IOException;

    protected abstract void createOrUpdateTask(HttpExchange httpExchange) throws IOException;

    protected abstract void deleteTaskOrAllTasks(HttpExchange httpExchange) throws IOException;
}