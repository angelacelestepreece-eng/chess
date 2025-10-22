package chess;

import java.util.Collection;
import java.util.HashSet;

public class PawnRule implements Rule {
    public Collection<ChessMove> getMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new HashSet<>();
        ChessPiece piece = board.getPiece(myPosition);
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int dir = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;

        ChessPosition forward1 = new ChessPosition(row + dir, col);
        ChessPosition forward2 = new ChessPosition(row + dir * 2, col);
        ChessPosition diag1 = new ChessPosition(row + dir, col + 1);
        ChessPosition diag2 = new ChessPosition(row + dir, col - 1);

        int startRow = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;


        //one move forward
        if (!ChessPosition.invalidPosition(row + dir, col) && board.getPiece(forward1) == null) {
            getPawnMoves(moves, myPosition, forward1);

            //two moves forward
            if (!ChessPosition.invalidPosition(row + dir * 2, col) && board.getPiece(forward2) == null && row == startRow) {
                getPawnMoves(moves, myPosition, forward2);
            }
        }

        //capture enemy
        if (!ChessPosition.invalidPosition(row + dir, col + 1) && board.getPiece(diag1) != null && (board.getPiece(diag1)).getTeamColor() != piece.getTeamColor()) {
            getPawnMoves(moves, myPosition, diag1);
        }

        if (!ChessPosition.invalidPosition(row + dir, col - 1) && board.getPiece(diag2) != null && (board.getPiece(diag2)).getTeamColor() != piece.getTeamColor()) {
            getPawnMoves(moves, myPosition, diag2);
        }

        return moves;
    }

    private void getPawnMoves(Collection<ChessMove> moves, ChessPosition myPosition, ChessPosition newPosition) {
        if (newPosition.getRow() == 8 || newPosition.getRow() == 1) {
            moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.KNIGHT));
            moves.add(new ChessMove(myPosition, newPosition, ChessPiece.PieceType.BISHOP));
        } else {
            moves.add(new ChessMove(myPosition, newPosition, null));
        }
    }
}
