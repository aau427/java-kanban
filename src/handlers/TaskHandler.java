package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.*;
import managers.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public TaskHandler(TaskManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Обрабатываю запрос на /tasks");
        String method = httpExchange.getRequestMethod();
        try {
            switch (method) {
                case "GET":
                    sendTaskListOr1Task(httpExchange);
                    break;
                case "POST":
                    createOrUpdateTask(httpExchange);
                    break;
                case "DELETE":
                    deleteOneOrAllTasks(httpExchange);
                    break;
                default:
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

    private void sendTaskListOr1Task(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Get");
        String[] pathItems = super.getPathItems(httpExchange);
        String gson;
        if (pathItems.length == 2) {
            List<Task> taskList = manager.getTaskList();
            gson = super.prepareGson().toJson(taskList);
        } else if (pathItems.length == 3) {
            int taskId = getTaskIdFromUriRequest(httpExchange.getRequestURI().getPath());
            Task task = manager.getTaskById(taskId);
            gson = super.prepareGson().toJson(task);
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        super.sendMessage(httpExchange, 200, gson);
    }

    void createOrUpdateTask(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Post");
        String message;
        String[] pathItems = super.getPathItems(httpExchange);
        String bodyStr = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        if (bodyStr.isBlank()) {
            throw new NoBodyException("При обновлении/изменении задачи не указан Body ");
        }
        Task task = super.prepareGson().fromJson(bodyStr, Task.class);
        int taskId;
        if (pathItems.length == 2) {
            taskId = manager.createTask(task);
            message = "Задача id = " + taskId + " успешно создана";
        } else if (pathItems.length == 3) {
            taskId = super.getTaskIdFromUriRequest(httpExchange.getRequestURI().getPath());
            if (taskId != task.getId()) {
                //в строке запроса переделали один id, в теле другой
                throw new LogicalErrorException("Кривая логика запроса при обновлении задачи " +
                        "в строке запроса передан Id = " + taskId + " , а в теле id = " + task.getId());
            }
            taskId = manager.updateTask(task);
            message = "Задача id = " + taskId + " успешно изменена!";
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        sendMessage(httpExchange, 201, message);
    }

    private void deleteOneOrAllTasks(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Delete");
        String[] pathItems = super.getPathItems(httpExchange);
        String message;
        if (pathItems.length == 2) {
            manager.deleteAllTasks();
            message = "Все задачи успешно удалены!";
        } else if (pathItems.length == 3) {
            int taskId = super.getTaskIdFromUriRequest(httpExchange.getRequestURI().getPath());
            manager.deleteTaskById(taskId);
            message = "Задача id = " + taskId + " успешно удалена!";
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        super.sendMessage(httpExchange, 200, message);
    }
}
