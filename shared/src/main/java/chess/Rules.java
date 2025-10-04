package chess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;


public class Rules implements Rule {
    private final boolean toEdge;
    private final int[][] directions;

    public Rules(boolean toEdge, int[][] directions) {
        this.toEdge = toEdge;
        this.directions = directions;
    }

    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        ChessPiece piece = board.getPiece(myPosition);

        for (int[] move : directions) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();

                do {
                    row += move[0];
                    col += move[1];

                    if (ChessPosition.invalidPosition(row, col)) {
                        break;
                    }
                    ChessPosition newPosition = new ChessPosition(row, col);
                    ChessPiece otherPiece = board.getPiece(newPosition);

                    if (otherPiece != null) {
                        if (otherPiece.getTeamColor() != piece.getTeamColor()){
                            moves.add(new ChessMove(myPosition, newPosition, null));
                        }
                        break;
                    }
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } while (toEdge);
        }


        return moves;
    }

}
