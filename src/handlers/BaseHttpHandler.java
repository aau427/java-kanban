package handlers;

import adapter.DurationAdapter;
import adapter.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler {

    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public void handle(HttpExchange httpExchange) throws IOException {
    }

    protected void sendMessage(HttpExchange h, int rCode, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(rCode, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected int getTaskIdFromUriRequest(String path) throws NumberFormatException {
        String[] uriRequestPath = path.split("/");
        String idStr = uriRequestPath[2];
        return Integer.parseInt(idStr);
    }

    protected Gson prepareGson() {
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
