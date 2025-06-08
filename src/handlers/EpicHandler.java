package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.*;
import managers.TaskManager;
import model.Epic;
import model.SubTask;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler {
    private final TaskManager manager;

    public EpicHandler(TaskManager manager) {
        super();
        this.manager = manager;
    }


    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("Обрабатываю запрос на /epics");
        String method = httpExchange.getRequestMethod();
        try {
            switch (method) {
                case "GET":
                    sendTasksOrSubTaskForEpic(httpExchange);
                    break;
                case "POST":
                    createOrUpdateEpic(httpExchange);
                    break;
                case "DELETE":
                    deleteOneOrAllEpics(httpExchange);
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
        } catch (Exception e) {
            sendMessage(httpExchange, 500, e.getMessage());
        }

    }

    private void sendTasksOrSubTaskForEpic(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Get");
        String[] pathItems = super.getPathItems(httpExchange);
        String gson;
        //epics
        if (pathItems.length == 2) {
            List<Epic> epicList = manager.getEpicList();
            gson = super.prepareGson().toJson(epicList);
        } else if (pathItems.length == 3) {
            //ВЫТАЩИТЬ КОНКРЕТНЫЙ ЭПИК
            int epicId = getTaskIdFromUriRequest(httpExchange.getRequestURI().getPath());
            Epic epic = manager.getEpicById(epicId);
            gson = super.prepareGson().toJson(epic);
        } else if (pathItems.length == 4 && pathItems[3].equals("subtasks")) {
            int epicId = getTaskIdFromUriRequest(httpExchange.getRequestURI().getPath());
            List<SubTask> subTaskList = manager.getEpicSubtasks(epicId);
            gson = super.prepareGson().toJson(subTaskList);

        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }

        super.sendMessage(httpExchange, 200, gson);
    }

    private void createOrUpdateEpic(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Post");
        String message;
        String[] pathItems = super.getPathItems(httpExchange);
        String bodyStr = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
        if (bodyStr.isBlank()) {
            throw new NoBodyException("При обновлении/изменении Эпика не указан Body ");
        }
        Epic epic = super.prepareGson().fromJson(bodyStr, Epic.class);
        int epicId;
        if (pathItems.length == 2) {
            epicId = manager.createEpic(epic);
            message = "Эпик id = " + epicId + " успешно создан";
        } else if (pathItems.length == 3) {
            epicId = super.getTaskIdFromUriRequest(httpExchange.getRequestURI().getPath());
            if (epicId != epic.getId()) {
                //в строке запроса переделали один id, в теле другой
                throw new LogicalErrorException("Кривая логика запроса при обновлении эпика " +
                        "в строке запроса передан Id = " + epicId + " , а в теле id = " + epic.getId());
            }
            epicId = manager.updateEpic(epic);
            message = "Эпик id = " + epicId + " успешно изменен!";
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        sendMessage(httpExchange, 201, message);
    }

    private void deleteOneOrAllEpics(HttpExchange httpExchange) throws IOException {
        System.out.println("вызван метод Delete");
        String[] pathItems = super.getPathItems(httpExchange);
        String message;
        if (pathItems.length == 2) {
            manager.deleteAllEpics();
            message = "Все эпики успешно удалены!";
        } else if (pathItems.length == 3) {
            int epicId = super.getTaskIdFromUriRequest(httpExchange.getRequestURI().getPath());
            manager.deleteEpicById(epicId);
            message = "Эпик id = " + epicId + " успешно удален!";
        } else {
            throw new BadRequestException("Недопустимый формат запроса get: " + httpExchange.getRequestURI().getPath());
        }
        super.sendMessage(httpExchange, 200, message);
    }
}