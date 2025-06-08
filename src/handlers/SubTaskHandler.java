package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.*;
import managers.TaskManager;
import model.SubTask;

import java.io.IOException;
import java.util.List;

public class SubTaskHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public SubTaskHandler(TaskManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Обрабатываю запрос на /subtasks");
        String method = httpExchange.getRequestMethod();
        try {
            switch (method) {
                case "GET":
                    sendSubTaskListOr1SubTask(httpExchange);
                    break;
                case "POST":
                    createOrUpdateSubTask(httpExchange);
                    break;
                case "DELETE":
                    deleteOneOrAllSubTasks(httpExchange);
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

    private void sendSubTaskListOr1SubTask(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Get");
        String[] pathItems = super.getPathItems(httpExchange);
        String gson;
        if (pathItems.length == 2) {
            List<SubTask> subTaskList = manager.getSubTaskList();
            gson = super.prepareGson().toJson(subTaskList);
        } else if (pathItems.length == 3) {
            int subTaskId = getTaskIdFromUriRequest(httpExchange.getRequestURI().getPath());
            SubTask subTask = manager.getSubTaskById(subTaskId);
            gson = super.prepareGson().toJson(subTask);
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        super.sendMessage(httpExchange, 200, gson);
    }

    void createOrUpdateSubTask(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Post");
        String message;
        String[] pathItems = super.getPathItems(httpExchange);
        String bodyStr = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        if (bodyStr.isBlank()) {
            throw new NoBodyException("При обновлении/изменении подзадачи не указан Body ");
        }
        SubTask subTask = super.prepareGson().fromJson(bodyStr, SubTask.class);
        int subTaskId;
        if (pathItems.length == 2) {
            subTaskId = manager.createSubTask(subTask);
            message = "Задача id = " + subTaskId + " успешно создана";
        } else if (pathItems.length == 3) {
            subTaskId = super.getTaskIdFromUriRequest(httpExchange.getRequestURI().getPath());
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

    private void deleteOneOrAllSubTasks(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Delete");
        String[] pathItems = super.getPathItems(httpExchange);
        String message;
        if (pathItems.length == 2) {
            manager.deleteAllSubTasks();
            message = "Все подзадачи успешно удалены!";
        } else if (pathItems.length == 3) {
            int subTaskId = super.getTaskIdFromUriRequest(httpExchange.getRequestURI().getPath());
            manager.deleteSubTaskById(subTaskId);
            message = "Подзадача id = " + subTaskId + " успешно удалена!";
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        super.sendMessage(httpExchange, 200, message);
    }
}
