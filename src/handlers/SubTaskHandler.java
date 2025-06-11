package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.BadRequestException;
import exception.LogicalErrorException;
import exception.NoBodyException;
import managers.TaskManager;
import model.SubTask;

import java.io.IOException;
import java.util.List;

public class SubTaskHandler extends BaseTaskHttpHandler {

    public SubTaskHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    protected void sendTaskOrTaskList(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Get");
        String[] pathItems = getPathItems(httpExchange);
        String gson;
        if (pathItems.length == 2) {
            List<SubTask> subTaskList = manager.getSubTaskList();
            gson = GSON.toJson(subTaskList);
        } else if (pathItems.length == 3) {
            int subTaskId = getTaskIdFromUriRequest(httpExchange);
            SubTask subTask = manager.getSubTaskById(subTaskId);
            gson = GSON.toJson(subTask);
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
            throw new NoBodyException("При обновлении/изменении подзадачи не указан Body ");
        }
        SubTask subTask = GSON.fromJson(bodyStr, SubTask.class);
        int subTaskId;
        if (pathItems.length == 2) {
            subTaskId = manager.createSubTask(subTask);
            message = "Задача id = " + subTaskId + " успешно создана";
        } else if (pathItems.length == 3) {
            subTaskId = getTaskIdFromUriRequest(httpExchange);
            if (subTaskId != subTask.getId()) {
                //в строке запроса переделали один id, в теле другой
                throw new LogicalErrorException("Кривая логика запроса при обновлении задачи " +
                        "в строке запроса передан Id = " + subTaskId + " , а в теле id = " + subTask.getId());
            }
            subTaskId = manager.updateSubTask(subTask);
            message = "Подзадача id = " + subTaskId + " успешно изменена!";
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
            manager.deleteAllSubTasks();
            message = "Все подзадачи успешно удалены!";
        } else if (pathItems.length == 3) {
            int subTaskId = getTaskIdFromUriRequest(httpExchange);
            manager.deleteSubTaskById(subTaskId);
            message = "Подзадача id = " + subTaskId + " успешно удалена!";
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        sendMessage(httpExchange, 200, message);
    }
}
