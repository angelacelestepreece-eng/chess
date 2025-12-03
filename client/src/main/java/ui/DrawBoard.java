package ui;

import java.util.Set;

public class DrawBoard {

    private static final String[][] initialBoard = {
            {"wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR"},
            {"wP", "wP", "wP", "wP", "wP", "wP", "wP", "wP"},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"bP", "bP", "bP", "bP", "bP", "bP", "bP", "bP"},
            {"bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR"}
    };

    private static String pieceSymbol(String piece, boolean highlight) {
        String symbol = switch (piece) {
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
                        .append(pieceSymbol(initialBoard[row][col], highlight))
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
