package ui;

import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece.PieceType;
import exception.ResponseException;
import model.GameData;
import server.ServerFacade;
import ui.websocket.NotificationHandler;
import ui.websocket.WebSocketFacade;
import websocket.messages.ServerMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GamePlayClient extends StandardClient implements NotificationHandler {
    private final String username;
    private final ServerFacade server;
    private final String serverUrl;
    private final String perspective;
    private final WebSocketFacade ws;
    private final int gameID;
    private final String authToken;
    private GameData lastState;

    public GamePlayClient(String username, String serverUrl, ServerFacade server,
                          String perspective, int gameID, String authToken) throws ResponseException {
        this.username = username;
        this.server = server;
        this.serverUrl = serverUrl;
        this.perspective = perspective;
        this.gameID = gameID;
        this.authToken = authToken;

        this.ws = new WebSocketFacade(serverUrl, this);
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                this.lastState = message.getGame();
                if (lastState != null && lastState.game() != null) {
                    System.out.println(DrawBoard.drawBoard(perspective, lastState));
                } else {
                    System.out.println("No game state available to draw.");
                }
            }
            case NOTIFICATION -> {
                System.out.println("Notification: " + message.getMessage());
            }
            case ERROR -> {
                System.err.println("Error: " + message.getErrorMessage());
            }
        }
    }


    public void run() {
        try {
            ws.connect(authToken, gameID);
        } catch (ResponseException e) {
            System.out.println("Error connecting to game: " + e.getMessage());
        }
        runLoop(username + " joined game " + gameID + "!");
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

    public String redraw() {
        if (lastState != null) {
            System.out.println(DrawBoard.drawBoard(perspective, lastState));
            return "Board redrawn.";
        }
        return "No game state available yet.";
    }

    public String leave() throws ResponseException {
        ws.sendLeave(authToken, gameID);
        new PostLoginClient(username, serverUrl, server, authToken).run();
        return username + " left the game.";
    }

    public String move(String... params) throws ResponseException {
        if (params.length >= 2) {
            String source = params[0];
            String destination = params[1];
            String promotionStr = (params.length == 3) ? params[2] : null;

            ChessPosition start = parsePosition(source);
            ChessPosition end = parsePosition(destination);
            PieceType promotion = (promotionStr != null) ? parsePieceType(promotionStr) : null;

            ChessMove move = new ChessMove(start, end, promotion);
            ws.sendMove(authToken, gameID, move);
            return "Move sent: " + move;
        }
        throw new ResponseException(ResponseException.Code.ClientError,
                "Expected: <source> <destination> <optional promotion> (e.g. f5 e4 q)");
    }

    public String resign() throws ResponseException {
        ws.sendResign(authToken, gameID);
        return username + " resigned. Game over.";
    }

    public String legal(String... params) throws ResponseException {
        if (params.length >= 1) {
            String position = params[0];
            Set<String> legalMoves = new HashSet<>();
            legalMoves.add(position);
            System.out.println(DrawBoard.drawBoard(perspective, legalMoves));
            return "Highlighted: " + position;
        }
        throw new ResponseException(ResponseException.Code.ClientError,
                "Expected: <position> (e.g. f5)");
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

    private ChessPosition parsePosition(String algebraic) {
        if (algebraic == null || algebraic.length() < 2) {
            throw new IllegalArgumentException("Invalid square: " + algebraic);
        }
        char file = Character.toLowerCase(algebraic.charAt(0));
        int col = file - 'a' + 1;
        int row = Character.getNumericValue(algebraic.charAt(1));
        return new ChessPosition(row, col);
    }

    private PieceType parsePieceType(String s) {
        if (s == null) {
            return null;
        }
        return switch (s.toLowerCase()) {
            case "q", "queen" -> PieceType.QUEEN;
            case "r", "rook" -> PieceType.ROOK;
            case "b", "bishop" -> PieceType.BISHOP;
            case "n", "knight" -> PieceType.KNIGHT;
            case "k", "king" -> PieceType.KING;
            case "p", "pawn" -> PieceType.PAWN;
            default -> null;
        };
    }
}