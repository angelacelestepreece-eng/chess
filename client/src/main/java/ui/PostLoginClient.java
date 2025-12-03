package ui;

import java.util.Arrays;
import java.util.Set;

import datamodel.CreateGameResult;
import datamodel.ListGamesResult;
import exception.ResponseException;
import model.GameData;
import server.ServerFacade;

import static ui.DrawBoard.drawBoard;

public class PostLoginClient extends StandardClient {
    private State state = State.SIGNEDIN;
    private final String username;
    private final ServerFacade server;
    private final String serverUrl;

    public PostLoginClient(String username, String serverUrl, ServerFacade server) throws ResponseException {
        this.username = username;
        this.server = server;
        this.serverUrl = serverUrl;
    }

    public void run() {
        runLoop("Logged in as" + username);
    }

    public String eval(String input) {
        try {
            String[] tokens = input.trim().split("\\s+");
            String cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "logout" -> logout();
                case "quit" -> "quit";
                case "help" -> help();
                case "" -> "";
                default -> "Unknown command. Type 'help' for options.";
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String createGame(String... params) throws ResponseException {
        if (params.length >= 1) {
            String gameName = String.join(" ", params);
            CreateGameResult result = server.createGame(gameName);
            return String.format("Game created with ID %d.%n", result.gameID());
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <NAME>");
    }

    public String listGames() throws ResponseException {
        ListGamesResult result = server.listGames();
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (GameData g : result.games()) {
            sb.append(String.format("%d. %s (ID: %d, White: %s, Black: %s)%n",
                    i++, g.gameName(), g.gameID(), g.whiteUsername(), g.blackUsername()));
        }
        return sb.toString();
    }

    public String joinGame(String... params) throws ResponseException {
        if (params.length >= 2) {
            int gameId = Integer.parseInt(params[0]);
            String color = params[1].toUpperCase();
            server.joinGame(gameId, color);
            return drawBoard(color, null);
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <ID> <WHITE|BLACK>");
    }

    public String observeGame(String... params) throws ResponseException {
        if (params.length >= 1) {
            int gameId = Integer.parseInt(params[0]);
            return drawBoard("OBSERVER", null);
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <ID>");
    }


    public String logout() throws ResponseException {
        state = State.SIGNEDOUT;
        new PreLoginClient(serverUrl).run();
        return "quit";
    }

    public String help() {
        return """
                create <NAME> - create a game
                list - list games
                join <ID> [WHITE|BLACK] - join a game
                observe <ID> - observe a game
                logout - when you are done
                quit - exit chess
                help - show possible commands
                """;
    }
}
