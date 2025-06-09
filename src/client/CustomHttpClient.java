package client;

import common.Managers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CustomHttpClient {
    private final HttpClient httpClient;
    private static final String HTTP_URI = "http://localhost:8080";

    public CustomHttpClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public HttpResponse<String> runGetRequest(String partOfPath) {
        URI url = URI.create(HTTP_URI + partOfPath);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        try {
            return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Во время выполнения запроса возникла ошибка!" + e.getMessage());
        }
    }

    public int runDeleteRequest(String partOfPath) {
        URI url = URI.create(HTTP_URI + partOfPath);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        return commonSendRequest(httpRequest);
    }

    public int runPostRequest(String partOfPath, String json) {
        URI url = URI.create(HTTP_URI + partOfPath);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json, Managers.getDefaultcharset()))
                .build();
        return commonSendRequest(httpRequest);
    }

    private int commonSendRequest(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Во время выполнения запроса возникла ошибка!" + e.getMessage());
        }
    }
}