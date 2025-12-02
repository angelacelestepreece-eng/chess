package ui;

import exception.ResponseException;
import server.ServerFacade;

import java.util.Arrays;

public class GamePlayClient extends StandardClient {
    private final String username;
    private final ServerFacade server;
    private final String serverUrl;

    public GamePlayClient(String username, String serverUrl, ServerFacade server) throws ResponseException {
        this.username = username;
        this.server = server;
        this.serverUrl = serverUrl;
    }

    public void run() {
        runLoop(username + " joined game!");
    }

    public String eval(String input) {
        try {
            String[] tokens = input.trim().split("\\s+");
            String cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "move" -> move(params);
                case "resign" -> resign();
                case "legal" -> legal(params);
                case "help" -> help();
                case "" -> "";
                default -> "Unknown command. Type 'help' for options.";
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String redraw() throws ResponseException {
        return "Redraw Result Here";
    }

    public String leave() throws ResponseException {
        return "Leave Result Here";
    }

    public String move(String... params) throws ResponseException {
        return "Move Result Here";
    }

    public String resign() throws ResponseException {
        return "Resign Result Here";
    }

    public String legal(String... params) throws ResponseException {
        return "Legal Result Here";
    }

    public String help() {
        return """
                redraw - redraws chess board
                leave - leave game
                move <source> <destination> <optional promotion> (e.g. f5 e4 q) - make move
                resign - forfeits the game
                legal <position> (e.g. f5) - highlights legal moves for selected piece
                help - show possible commands
                """;
    }
}