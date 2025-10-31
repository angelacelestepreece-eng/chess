package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.ResponseException;
import datamodel.*;
import io.javalin.*;
import io.javalin.http.Context;
import model.UserData;
import service.GameService;
import service.ServiceException;
import service.UserService;
import dataaccess.MySQLDataAccess;

public class Server {

    private final Javalin server;
    private final DataAccess dataAccess;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        server = Javalin.create(config -> config.staticFiles.add("web"));

        DataAccess dao;
        try {
            dao = new MySQLDataAccess();
        } catch (ResponseException e) {
            throw new RuntimeException("Failed to initialize database access", e);
        }

        this.dataAccess = dao;
        this.userService = new UserService(dataAccess);
        this.gameService = new GameService(dataAccess);

        server.delete("db", ctx -> {
            var serializer = new Gson();
            try {
                userService.clear();
                gameService.clear();
                ctx.status(200).result("{}");
            } catch (ServiceException e) {
                ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
            } catch (Exception e) {
                ctx.status(500).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
            }
        });

        server.post("user", this::register);
        server.post("session", this::login);
        server.delete("session", this::logout);
        server.get("game", this::listGames);
        server.post("game", this::createGame);
        server.put("game", this::joinGame);

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
            LoginResult res = userService.login(req);
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
            ListGamesResult res = gameService.listGames(authToken);
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
            CreateGameResult res = gameService.createGame(req.gameName(), authToken);
            ctx.status(200).result(serializer.toJson((res)));
        } catch (ServiceException e) {
            ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        } catch (Exception e) {
            ctx.status(500).result(serializer.toJson(new ErrorMessage("Error: " + e.getMessage())));
        }
    }

    public void joinGame(Context ctx) {
        var serializer = new Gson();
        try {
            var authToken = ctx.header("authorization");
            var req = serializer.fromJson(ctx.body(), JoinGameRequest.class);
            gameService.joinGame(authToken, req.playerColor(), req.gameID());
            ctx.status(200).result("{}");
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
