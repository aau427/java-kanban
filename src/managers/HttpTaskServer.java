package managers;

import com.sun.net.httpserver.HttpServer;
import common.Managers;
import handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final Charset charset = StandardCharsets.UTF_8;
    private final TaskManager manager;
    private final HttpServer httpServer;
    private final TaskHandler taskHandler;
    private final EpicHandler epicHandler;
    private final SubTaskHandler subTaskHandler;
    private final HistoryHandler historyHandler;
    private final PrioritizedHandler prioritizedHandler;


    public static void main(String[] args) throws IOException {
        //при запуске программы должен стартовать экземпляр HttpServer
        new HttpTaskServer(Managers.getDefaultTaskManager()).start();
    }

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        taskHandler = new TaskHandler(manager);
        epicHandler = new EpicHandler(manager);
        subTaskHandler = new SubTaskHandler(manager);
        historyHandler = new HistoryHandler(manager);
        prioritizedHandler = new PrioritizedHandler(manager);
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", taskHandler::handle);
        httpServer.createContext("/epics", epicHandler::handle);
        httpServer.createContext("/subtasks", subTaskHandler::handle);
        httpServer.createContext("/history", historyHandler::historyHandler);
        httpServer.createContext("/prioritized", prioritizedHandler::prioritizedHandler);
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

}
