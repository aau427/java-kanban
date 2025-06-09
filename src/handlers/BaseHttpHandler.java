package handlers;

import adapter.DurationAdapter;
import adapter.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import exception.*;
import managers.TaskManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler {

    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected static final Gson GSON = prepareGson();
    protected final TaskManager manager;

    public BaseHttpHandler(TaskManager manager) {
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

    protected void sendTaskOrTaskList(HttpExchange httpExchange) throws IOException {
    }

    protected void createOrUpdateTask(HttpExchange httpExchange) throws IOException {
    }

    protected void deleteTaskOrAllTasks(HttpExchange httpExchange) throws IOException {
    }


    protected void sendMessage(HttpExchange httpExchange, int statusCode, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(statusCode, resp.length);
        httpExchange.getResponseBody().write(resp);
        httpExchange.close();
    }

    protected int getTaskIdFromUriRequest(String path) throws NumberFormatException {
        String[] uriRequestPath = path.split("/");
        String idStr = uriRequestPath[2];
        return Integer.parseInt(idStr);
    }

    private static Gson prepareGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    protected String[] getPathItems(HttpExchange httpExchange) {
        return httpExchange.getRequestURI().getPath().split("/");
    }
}
