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

public class HttpExchangeUtility {
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    public void sendMessage(HttpExchange httpExchange, int statusCode, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(statusCode, resp.length);
        httpExchange.getResponseBody().write(resp);
        httpExchange.close();
    }

    public int getTaskIdFromUriRequest(HttpExchange httpExchange) throws NumberFormatException {
        String[] uriRequestPath = httpExchange.getRequestURI().getPath().split("/");
        String idStr = uriRequestPath[2];
        return Integer.parseInt(idStr);
    }

    public String[] getPathItems(HttpExchange httpExchange) {
        return httpExchange.getRequestURI().getPath().split("/");
    }
}
