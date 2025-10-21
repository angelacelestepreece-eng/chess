package server;

import com.google.gson.Gson;
import datamodel.CreateGameRequest;
import datamodel.ErrorMessage;
import io.javalin.*;
import io.javalin.http.Context;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.ServiceException;
import service.UserService;

import java.security.Provider;

public class Server {

    private final Javalin server;
    private UserService userService = new UserService();

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> {
            userService.clear();
            ctx.status(200).result("{}");
        });
        server.post("user", this::register);
        server.post("session", this::login);
        server.delete("session", this::logout);
        server.get("game", this::listGames);
        server.post("game", this::createGame);

    }

    private void register(Context ctx) {
        var serializer = new Gson();
        try {
            var req = serializer.fromJson(ctx.body(), UserData.class);
            var res = userService.register(req);
            ctx.status(200).result(serializer.toJson(res));
        } catch (ServiceException e) {
            ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        } catch (Exception e) {
            ctx.status(500).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        }
    }

    private void login(Context ctx) {
        var serializer = new Gson();
        try {
            var req = serializer.fromJson(ctx.body(), UserData.class);
            var res = userService.login(req);
            ctx.status(200).result(serializer.toJson(res));
        } catch (ServiceException e) {
            ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        } catch (Exception e) {
            ctx.status(500).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        }
    }

    private void logout(Context ctx) {
        var serializer = new Gson();
        try {
            var authToken = ctx.header("authorization");
            userService.logout(authToken);
            ctx.status(200).result("{}");
        } catch (ServiceException e) {
            ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        } catch (Exception e) {
            ctx.status(500).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        }
    }

    private void listGames(Context ctx) {
        var serializer = new Gson();
        try {
            var authToken = ctx.header("authorization");
            var res = userService.listGames(authToken);
            ctx.status(200).result(serializer.toJson(res));
        } catch (ServiceException e) {
            ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        } catch (Exception e) {
            ctx.status(500).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        }
    }

    private void createGame(Context ctx) {
        var serializer = new Gson();
        try {
            var authToken = ctx.header("authorization");
            var req = serializer.fromJson(ctx.body(), CreateGameRequest.class);
            var res = userService.createGame(req.gameName(), authToken);
            record GameResponse(int gameID) {
            }
            ctx.status(200).result(serializer.toJson(new GameResponse(res)));
        } catch (ServiceException e) {
            ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        } catch (Exception e) {
            ctx.status(500).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        }
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
