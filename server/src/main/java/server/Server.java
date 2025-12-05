package server;

import com.google.gson.Gson;
import datamodel.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.websocket.WsContext;
import exception.ResponseException;
import dataaccess.DataAccess;
import dataaccess.MySQLDataAccess;
import model.AuthData;
import model.UserData;
import model.GameData;
import service.GameService;
import service.ServiceHelper;
import service.UserService;
import service.ServiceException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import chess.ChessGame;
import chess.ChessMove;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private final Javalin server;
    private final DataAccess dataAccess;
    private final UserService userService;
    private final GameService gameService;
    private final Gson gson = new Gson();
    private final Map<Integer, Map<String, WsContext>> gameSessions = new ConcurrentHashMap<>();

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
                String msg = e.getMessage();
                if (msg == null || msg.isBlank()) {
                    msg = "Error: Internal Server Error";
                }
                ctx.status(500).result(serializer.toJson(new ErrorMessage(msg)));
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

        server.ws("/ws", ws -> {
            ws.onConnect(ctx -> System.out.println("WebSocket connected: " + ctx.sessionId()));

            ws.onMessage(ctx -> {
                UserGameCommand command;
                try {
                    command = gson.fromJson(ctx.message(), UserGameCommand.class);
                } catch (Exception parseEx) {
                    ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Invalid command format");
                    safeSend(ctx, error);
                    return;
                }
                handleCommand(ctx, command);
            });

            ws.onClose(ctx -> {
                System.out.println("WebSocket closed: " + ctx.sessionId());
                for (Map<String, WsContext> sessions : gameSessions.values()) {
                    sessions.entrySet().removeIf(e -> e.getValue() == ctx);
                }
            });

            ws.onError(ctx -> System.out.println("WebSocket error: " + ctx.error()));
        });
    }

    private void handleCommand(WsContext ctx, UserGameCommand command) {
        switch (command.getCommandType()) {
            case CONNECT -> handleConnect(ctx, command);
            case MAKE_MOVE -> handleMakeMove(ctx, command);
            case LEAVE -> handleLeave(ctx, command);
            case RESIGN -> handleResign(ctx, command);
        }
    }

    private void handleConnect(WsContext ctx, UserGameCommand command) {
        gameSessions.computeIfAbsent(command.getGameID(), id -> new ConcurrentHashMap<>())
                .put(command.getAuthToken(), ctx);
        try {
            AuthData auth = ServiceHelper.validateAuth(dataAccess, command.getAuthToken());
            GameData game = gameService.getGame(command.getGameID());

            safeSend(ctx, new ServerMessage(game));

            String username = auth.username();
            String color = username.equals(game.whiteUsername()) ? "white" : "black";
            broadcast(command.getGameID(),
                    new ServerMessage(username + " joined the game as " + color),
                    command.getAuthToken());
        } catch (ServiceException e) {
            safeSend(ctx, new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage()));
        }
    }

    private void handleMakeMove(WsContext ctx, UserGameCommand command) {
        try {
            ChessMove move = command.getMove();
            if (move == null) {
                throw new ServiceException(400, "Error: missing move");
            }

            AuthData auth = ServiceHelper.validateAuth(dataAccess, command.getAuthToken());
            GameData game = gameService.getGame(command.getGameID());

            // validate turn + player
            validateMoveTurn(auth.username(), game);

            gameService.makeMove(command.getAuthToken(), command.getGameID(), move);
            GameData updated = gameService.getGame(command.getGameID());

            broadcast(command.getGameID(), new ServerMessage(updated), null);
            broadcast(command.getGameID(),
                    new ServerMessage(auth.username() + " made a move"),
                    command.getAuthToken());
        } catch (ServiceException e) {
            safeSend(ctx, new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage()));
        }
    }

    private void handleLeave(WsContext ctx, UserGameCommand command) {
        Map<String, WsContext> sessions = gameSessions.get(command.getGameID());
        if (sessions != null) {
            sessions.remove(command.getAuthToken());
        }

        try {
            AuthData auth = ServiceHelper.validateAuth(dataAccess, command.getAuthToken());
            GameData game = gameService.getGame(command.getGameID());

            GameData updated = removePlayerFromGame(auth.username(), game);
            gameService.updateGame(updated);

            broadcast(command.getGameID(),
                    new ServerMessage(auth.username() + " left the game"),
                    command.getAuthToken());
        } catch (ServiceException e) {
            safeSend(ctx, new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage()));
        }
    }

    private void handleResign(WsContext ctx, UserGameCommand command) {
        try {
            AuthData auth = ServiceHelper.validateAuth(dataAccess, command.getAuthToken());
            GameData game = gameService.getGame(command.getGameID());

            validateResign(auth.username(), game);

            gameService.resignGame(command.getAuthToken(), command.getGameID());
            GameData freed = freeGameSlot(auth.username(), game);
            gameService.updateGame(freed);

            broadcast(command.getGameID(),
                    new ServerMessage(auth.username() + " resigned"),
                    null);
        } catch (ServiceException e) {
            safeSend(ctx, new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage()));
        }
    }

    private void validateMoveTurn(String username, GameData game) throws ServiceException {
        if (game.game() == null) {
            throw new ServiceException(400, "Error: game is over");
        }

        String white = game.whiteUsername();
        String black = game.blackUsername();
        boolean isWhite = username.equals(white);
        boolean isBlack = username.equals(black);

        if (!isWhite && !isBlack) {
            throw new ServiceException(400, "Error: unauthorized");
        }

        ChessGame.TeamColor turn = game.game().getTeamTurn();
        if ((turn == ChessGame.TeamColor.WHITE && !isWhite) ||
                (turn == ChessGame.TeamColor.BLACK && !isBlack)) {
            throw new ServiceException(400, "Error: invalid move");
        }
    }

    private GameData removePlayerFromGame(String username, GameData game) {
        if (username.equals(game.whiteUsername())) {
            return new GameData(game.gameID(), null, game.blackUsername(),
                    game.gameName(), game.game());
        } else if (username.equals(game.blackUsername())) {
            return new GameData(game.gameID(), game.whiteUsername(), null,
                    game.gameName(), game.game());
        }
        return game;
    }

    private void validateResign(String username, GameData game) throws ServiceException {
        if (game.game() == null) {
            throw new ServiceException(400, "Error: game is over");
        }
        if (!username.equals(game.whiteUsername()) && !username.equals(game.blackUsername())) {
            throw new ServiceException(400, "Error: unauthorized");
        }
    }

    private GameData freeGameSlot(String username, GameData game) {
        boolean isWhite = username.equals(game.whiteUsername());
        boolean isBlack = username.equals(game.blackUsername());
        return new GameData(game.gameID(),
                isWhite ? null : game.whiteUsername(),
                isBlack ? null : game.blackUsername(),
                game.gameName(),
                null);
    }

    private void broadcast(int gameID, ServerMessage msg, String excludeAuthToken) {
        Map<String, WsContext> sessions = gameSessions.get(gameID);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String payload = gson.toJson(msg);
        for (var entry : sessions.entrySet()) {
            if (excludeAuthToken != null && entry.getKey().equals(excludeAuthToken)) {
                continue;
            }
            WsContext clientCtx = entry.getValue();
            try {
                clientCtx.send(payload);
            } catch (Exception ex) {
                sessions.remove(entry.getKey());
            }
        }
    }

    private void safeSend(WsContext ctx, ServerMessage msg) {
        try {
            String payload = gson.toJson(msg);
            ctx.send(payload);
        } catch (Exception ignored) {
        }
    }

    private void register(Context ctx) {
        var serializer = new Gson();
        try {
            var req = serializer.fromJson(ctx.body(), UserData.class);
            var res = userService.register(req);
            ctx.status(200).result(serializer.toJson(res));
        } catch (ServiceException e) {
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = "Error: Internal Server Error";
                ctx.status(500).result(serializer.toJson(new ErrorMessage(msg)));
            } else {
                ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage(msg)));
            }
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
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = "Error: Internal Server Error";
                ctx.status(500).result(serializer.toJson(new ErrorMessage(msg)));
            } else {
                ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage(msg)));
            }
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
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = "Error: Internal Server Error";
                ctx.status(500).result(serializer.toJson(new ErrorMessage(msg)));
            } else {
                ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage(msg)));
            }
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
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = "Error: Internal Server Error";
                ctx.status(500).result(serializer.toJson(new ErrorMessage(msg)));
            } else {
                ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage(msg)));
            }
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
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = "Error: Internal Server Error";
                ctx.status(500).result(serializer.toJson(new ErrorMessage(msg)));
            } else {
                ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage(msg)));
            }
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
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = "Error: Internal Server Error";
                ctx.status(500).result(serializer.toJson(new ErrorMessage(msg)));
            } else {
                ctx.status(e.getStatusCode()).result(serializer.toJson(new ErrorMessage(msg)));
            }
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
