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
            case CONNECT -> {
                gameSessions
                        .computeIfAbsent(command.getGameID(), id -> new ConcurrentHashMap<>())
                        .put(command.getAuthToken(), ctx);

                try {
                    AuthData authData = ServiceHelper.validateAuth(dataAccess, command.getAuthToken());
                    GameData gameData = gameService.getGame(command.getGameID());
                    ServerMessage loadGame = new ServerMessage(gameData);
                    safeSend(ctx, loadGame);
                    String username = authData.username();
                    String color = username.equals(gameData.whiteUsername()) ? "white" : "black";
                    ServerMessage notify = new ServerMessage(username + " joined the game as " + color);
                    broadcast(command.getGameID(), notify, command.getAuthToken());
                } catch (ServiceException e) {
                    ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
                    safeSend(ctx, error);
                }
            }

            case MAKE_MOVE -> {
                try {
                    ChessMove move = command.getMove();
                    if (move == null) {
                        throw new ServiceException(400, "Error: missing move");
                    }
                    AuthData authData = ServiceHelper.validateAuth(dataAccess, command.getAuthToken());
                    GameData gameData = gameService.getGame(command.getGameID());
                    if (gameData.game() == null) {
                        throw new ServiceException(400, "Error: game is over");
                    }

                    String username = authData.username();
                    String white = gameData.whiteUsername();
                    String black = gameData.blackUsername();
                    boolean isWhite = username != null && username.equals(white);
                    boolean isBlack = username != null && username.equals(black);
                    if (!isWhite && !isBlack) {
                        throw new ServiceException(400, "Error: unauthorized");
                    }

                    ChessGame.TeamColor turn = gameData.game().getTeamTurn();
                    if ((turn == ChessGame.TeamColor.WHITE && !isWhite) ||
                            (turn == ChessGame.TeamColor.BLACK && !isBlack)) {
                        throw new ServiceException(400, "Error: invalid move");
                    }
                    gameService.makeMove(command.getAuthToken(), command.getGameID(), move);
                    GameData updatedGame = gameService.getGame(command.getGameID());
                    ServerMessage loadGame = new ServerMessage(updatedGame);
                    broadcast(command.getGameID(), loadGame, null);
                    ServerMessage notify = new ServerMessage(username + " made a move");
                    broadcast(command.getGameID(), notify, command.getAuthToken());

                } catch (ServiceException e) {
                    ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
                    safeSend(ctx, error);
                }
            }

            case LEAVE -> {
                Map<String, WsContext> sessions = gameSessions.get(command.getGameID());
                if (sessions != null) {
                    sessions.remove(command.getAuthToken());
                }
                try {
                    AuthData authData = ServiceHelper.validateAuth(dataAccess, command.getAuthToken());
                    GameData gameData = gameService.getGame(command.getGameID());
                    String username = authData.username();

                    GameData updated;
                    if (username.equals(gameData.whiteUsername())) {
                        updated = new GameData(gameData.gameID(), null, gameData.blackUsername(),
                                gameData.gameName(), gameData.game());
                    } else if (username.equals(gameData.blackUsername())) {
                        updated = new GameData(gameData.gameID(), gameData.whiteUsername(), null,
                                gameData.gameName(), gameData.game());
                    } else {
                        updated = gameData;
                    }
                    gameService.updateGame(updated);

                    ServerMessage notify = new ServerMessage(username + " left the game");
                    broadcast(command.getGameID(), notify, command.getAuthToken());
                } catch (ServiceException e) {
                    ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
                    safeSend(ctx, error);
                }
            }

            case RESIGN -> {
                try {
                    AuthData authData = ServiceHelper.validateAuth(dataAccess, command.getAuthToken());
                    GameData gameData = gameService.getGame(command.getGameID());
                    String username = authData.username();

                    String white = gameData.whiteUsername();
                    String black = gameData.blackUsername();
                    boolean isWhite = username.equals(white);
                    boolean isBlack = username.equals(black);
                    if (!isWhite && !isBlack) {
                        throw new ServiceException(400, "Error: unauthorized");
                    }
                    if (gameData.game() == null) {
                        throw new ServiceException(400, "Error: game is over");
                    }
                    gameService.resignGame(command.getAuthToken(), command.getGameID());
                    GameData freed = new GameData(
                            gameData.gameID(),
                            isWhite ? null : gameData.whiteUsername(),
                            isBlack ? null : gameData.blackUsername(),
                            gameData.gameName(),
                            null
                    );
                    gameService.updateGame(freed);
                    ServerMessage notify = new ServerMessage(username + " resigned");
                    broadcast(command.getGameID(), notify, null);

                } catch (ServiceException e) {
                    ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, e.getMessage());
                    safeSend(ctx, error);
                }
            }

        }
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
