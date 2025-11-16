package ui;

import java.util.Arrays;
import java.util.Scanner;

import datamodel.CreateGameResult;
import datamodel.ListGamesResult;
import exception.ResponseException;
import model.GameData;
import server.ServerFacade;

import static ui.EscapeSequences.*;

public class PostLoginClient {
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
        System.out.printf("Logged in as %s%n", username);

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                if (!result.equals("quit")) {
                    System.out.print(BLUE + result);
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
    }

    private void printPrompt() {
        System.out.print("\n" + RESET + ">>> " + GREEN);
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
            return drawBoard(color);
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <ID> <WHITE|BLACK>");
    }

    public String observeGame(String... params) throws ResponseException {
        if (params.length >= 1) {
            int gameId = Integer.parseInt(params[0]);
            return drawBoard("OBSERVER");
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <ID>");
    }


    public String logout() throws ResponseException {
        state = State.SIGNEDOUT;
        new PreLoginClient(serverUrl).run();
        return "quit";
    }

    private final String[][] initialBoard = {
            {"wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR"},
            {"wP", "wP", "wP", "wP", "wP", "wP", "wP", "wP"},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"bP", "bP", "bP", "bP", "bP", "bP", "bP", "bP"},
            {"bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR"}
    };

    private String pieceSymbol(String piece) {
        return switch (piece) {
            case "wK" -> EscapeSequences.GREEN + " K " + EscapeSequences.BLUE;
            case "wQ" -> EscapeSequences.GREEN + " Q " + EscapeSequences.BLUE;
            case "wR" -> EscapeSequences.GREEN + " R " + EscapeSequences.BLUE;
            case "wB" -> EscapeSequences.GREEN + " B " + EscapeSequences.BLUE;
            case "wN" -> EscapeSequences.GREEN + " N " + EscapeSequences.BLUE;
            case "wP" -> EscapeSequences.GREEN + " P " + EscapeSequences.BLUE;
            case "bK" -> EscapeSequences.RED + " k " + EscapeSequences.BLUE;
            case "bQ" -> EscapeSequences.RED + " q " + EscapeSequences.BLUE;
            case "bR" -> EscapeSequences.RED + " r " + EscapeSequences.BLUE;
            case "bB" -> EscapeSequences.RED + " b " + EscapeSequences.BLUE;
            case "bN" -> EscapeSequences.RED + " n " + EscapeSequences.BLUE;
            case "bP" -> EscapeSequences.RED + " p " + EscapeSequences.BLUE;
            default -> "   ";
        };
    }

    private String drawBoard(String perspective) {
        StringBuilder sb = new StringBuilder();
        sb.append(EscapeSequences.ERASE_SCREEN);

        boolean whitePerspective = perspective.equalsIgnoreCase("WHITE") || perspective.equalsIgnoreCase("OBSERVER");
        int startRow = whitePerspective ? 7 : 0;
        int endRow = whitePerspective ? -1 : 8;
        int stepRow = whitePerspective ? -1 : 1;
        int startCol = whitePerspective ? 0 : 7;
        int endCol = whitePerspective ? 8 : -1;
        int stepCol = whitePerspective ? 1 : -1;
        for (int row = startRow; row != endRow; row += stepRow) {
            int rank = whitePerspective ? row + 1 : 8 - row;
            sb.append(rank).append(" ");
            for (int col = startCol; col != endCol; col += stepCol) {
                boolean lightSquare = (row + col) % 2 != 0;
                String bgColor = lightSquare ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY
                        : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                sb.append(bgColor)
                        .append(pieceSymbol(initialBoard[row][col]))
                        .append(EscapeSequences.RESET_BG_COLOR);
            }
            sb.append("\n");
        }
        sb.append("  ");
        for (char c = 'a'; c <= 'h'; c++) {
            sb.append(" ").append(c).append(" ");
        }
        sb.append("\n");

        return sb.toString();
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
