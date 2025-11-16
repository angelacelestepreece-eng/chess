package server;

import com.google.gson.Gson;
import exception.ResponseException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import datamodel.*;
import model.UserData;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private final Gson gson = new Gson();

    private String authToken;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public RegistrationResult register(String username, String password, String email) throws ResponseException {
        var req = new UserData(username, password, email);
        var request = buildRequest("POST", "/user", req, false);
        var response = sendRequest(request);
        RegistrationResult result = handleResponse(response, RegistrationResult.class);
        this.authToken = result.authToken();
        return result;
    }

    public LoginResult login(String username, String password) throws ResponseException {
        var req = new UserData(username, password, null);
        var request = buildRequest("POST", "/session", req, false);
        var response = sendRequest(request);
        LoginResult result = handleResponse(response, LoginResult.class);
        this.authToken = result.authToken();
        return result;
    }

    public void logout() throws ResponseException {
        var request = buildRequest("DELETE", "/session", null, true);
        var response = sendRequest(request);
        handleResponse(response, null);
        this.authToken = null;
    }

    public CreateGameResult createGame(String name) throws ResponseException {
        var req = new CreateGameRequest(name);
        var request = buildRequest("POST", "/game", req, true);
        var response = sendRequest(request);
        return handleResponse(response, CreateGameResult.class);
    }

    public ListGamesResult listGames() throws ResponseException {
        var request = buildRequest("GET", "/game", null, true);
        var response = sendRequest(request);
        return handleResponse(response, ListGamesResult.class);
    }

    public void joinGame(int gameId, String color) throws ResponseException {
        var req = new JoinGameRequest(color, gameId);
        var request = buildRequest("PUT", "/game", req, true);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public void clear() throws ResponseException {
        var request = buildRequest("DELETE", "/db", null, false);
        var response = sendRequest(request);
        handleResponse(response, null);
    }


    private HttpRequest buildRequest(String method, String path, Object body, boolean needsAuth) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));

        if (body != null) {
            builder.setHeader("Content-Type", "application/json");
        }
        if (needsAuth && authToken != null) {
            builder.setHeader("Authorization", authToken);
        }
        return builder.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(gson.toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null && !body.isEmpty()) {
                try {
                    ErrorMessage err = gson.fromJson(body, ErrorMessage.class);
                    throw new ResponseException(ResponseException.Code.ServerError, err.message());
                } catch (Exception e) {
                    throw new ResponseException(ResponseException.Code.ServerError, body);
                }
            }
            throw new ResponseException(ResponseException.Code.ServerError, "HTTP error: " + status);
        }

        if (responseClass != null && !response.body().isBlank()) {
            return gson.fromJson(response.body(), responseClass);
        }
        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
