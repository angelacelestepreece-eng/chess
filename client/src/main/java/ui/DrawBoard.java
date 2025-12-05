package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;

import java.util.Set;

public class DrawBoard {

    private static final String[][] INITIAL_BOARD = {
            {"wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR"},
            {"wP", "wP", "wP", "wP", "wP", "wP", "wP", "wP"},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"bP", "bP", "bP", "bP", "bP", "bP", "bP", "bP"},
            {"bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR"}
    };

    private static String pieceSymbol(String abbrev, boolean highlight) {
        String symbol = switch (abbrev) {
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
        if (highlight) {
            return EscapeSequences.SET_BG_COLOR_YELLOW + symbol + EscapeSequences.RESET_BG_COLOR;
        }
        return symbol;
    }

    private static String pieceSymbol(ChessPiece piece, boolean highlight) {
        String abbrev = "   ";
        if (piece != null) {
            boolean white = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
            String color = white ? "w" : "b";
            String type = switch (piece.getPieceType()) {
                case KING -> "K";
                case QUEEN -> "Q";
                case ROOK -> "R";
                case BISHOP -> "B";
                case KNIGHT -> "N";
                case PAWN -> "P";
            };
            abbrev = color + type;
        }
        return pieceSymbol(abbrev, highlight);
    }

    public static String drawBoard(String perspective, Set<String> highlights) {
        StringBuilder sb = new StringBuilder();
        sb.append(EscapeSequences.ERASE_SCREEN);

        boolean whitePerspective = perspective.equalsIgnoreCase("WHITE")
                || perspective.equalsIgnoreCase("OBSERVER");
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
                char file = (char) ('a' + col);
                int rankNum = row + 1;
                String square = "" + file + rankNum;

                boolean highlight = highlights != null && highlights.contains(square);

                sb.append(bgColor)
                        .append(pieceSymbol(INITIAL_BOARD[row][col], highlight))
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

    public static String drawBoard(String perspective, GameData state) {
        if (state == null || state.game() == null) {
            return "No game state available.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(EscapeSequences.ERASE_SCREEN);

        ChessBoard board = state.game().getBoard();

        boolean whitePerspective = perspective.equalsIgnoreCase("WHITE")
                || perspective.equalsIgnoreCase("OBSERVER");
        int startRow = whitePerspective ? 8 : 1;
        int endRow = whitePerspective ? 1 : 8;
        int stepRow = whitePerspective ? -1 : 1;
        int startCol = whitePerspective ? 1 : 8;
        int endCol = whitePerspective ? 8 : 1;
        int stepCol = whitePerspective ? 1 : -1;

        for (int row = startRow; (whitePerspective ? row >= endRow : row <= endRow); row += stepRow) {
            sb.append(row).append(" ");
            for (int col = startCol; (whitePerspective ? col <= endCol : col >= endCol); col += stepCol) {
                int zeroRow = row - 1;
                int zeroCol = col - 1;

                boolean lightSquare = (zeroRow + zeroCol) % 2 != 0;
                String bgColor = lightSquare ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY
                        : EscapeSequences.SET_BG_COLOR_DARK_GREY;

                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                sb.append(bgColor)
                        .append(pieceSymbol(piece, false))
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
}
