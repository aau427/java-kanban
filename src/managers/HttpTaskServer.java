package managers;

import com.sun.net.httpserver.HttpServer;
import common.Managers;
import handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer httpServer;


    public static void main(String[] args) throws IOException {
        //при запуске программы должен стартовать экземпляр HttpServer
        new HttpTaskServer(Managers.getDefaultTaskManager()).start();
    }

    public HttpTaskServer(TaskManager manager) throws IOException {
        TaskHandler taskHandler = new TaskHandler(manager);
        EpicHandler epicHandler = new EpicHandler(manager);
        SubTaskHandler subTaskHandler = new SubTaskHandler(manager);
        HistoryHandler historyHandler = new HistoryHandler(manager);
        PrioritizedHandler prioritizedHandler = new PrioritizedHandler(manager);
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", taskHandler::handle);
        httpServer.createContext("/epics", epicHandler::handle);
        httpServer.createContext("/subtasks", subTaskHandler::handle);
        httpServer.createContext("/history", historyHandler::handle);
        httpServer.createContext("/prioritized", prioritizedHandler::handle);
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

}
