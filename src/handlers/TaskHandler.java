package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.BadRequestException;
import exception.LogicalErrorException;
import exception.NoBodyException;
import managers.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class TaskHandler extends BaseTaskHttpHandler {

    public TaskHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    protected void sendTaskOrTaskList(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Get");
        String[] pathItems = getPathItems(httpExchange);
        String gson;
        if (pathItems.length == 2) {
            List<Task> taskList = manager.getTaskList();
            gson = GSON.toJson(taskList);
        } else if (pathItems.length == 3) {
            int taskId = getTaskIdFromUriRequest(httpExchange);
            Task task = manager.getTaskById(taskId);
            gson = GSON.toJson(task);
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        sendMessage(httpExchange, 200, gson);
    }

    @Override
    protected void createOrUpdateTask(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Post");
        String message;
        String[] pathItems = getPathItems(httpExchange);
        String bodyStr = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        if (bodyStr.isBlank()) {
            throw new NoBodyException("При обновлении/изменении задачи не указан Body ");
        }
        Task task = GSON.fromJson(bodyStr, Task.class);
        int taskId;
        if (pathItems.length == 2) {
            taskId = manager.createTask(task);
            message = "Задача id = " + taskId + " успешно создана";
        } else if (pathItems.length == 3) {
            taskId = getTaskIdFromUriRequest(httpExchange);
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

    @Override
    protected void deleteTaskOrAllTasks(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Delete");
        String[] pathItems = getPathItems(httpExchange);
        String message;
        if (pathItems.length == 2) {
            manager.deleteAllTasks();
            message = "Все задачи успешно удалены!";
        } else if (pathItems.length == 3) {
            int taskId = getTaskIdFromUriRequest(httpExchange);
            manager.deleteTaskById(taskId);
            message = "Задача id = " + taskId + " успешно удалена!";
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        sendMessage(httpExchange, 200, message);
    }
}
