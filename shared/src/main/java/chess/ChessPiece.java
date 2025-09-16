package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private PieceType type;
    private ChessGame.TeamColor pieceColor;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.type = type;
        this.pieceColor = pieceColor;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);

        //I'm just testing Bishop Moves & Rook Moves & Queen Moves & King Moves & Knight Moves
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.BISHOP && piece.getPieceType() != ChessPiece.PieceType.ROOK && piece.getPieceType() != ChessPiece.PieceType.QUEEN && piece.getPieceType() != ChessPiece.PieceType.KING && piece.getPieceType() != ChessPiece.PieceType.KNIGHT && piece.getPieceType() != ChessPiece.PieceType.PAWN) {
            return new ArrayList<>();
        }

        Collection<ChessMove> moves = new ArrayList<>();
        int[][] bishop_movement = {{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
        int[][] rook_movement = {{1, 0}, {0, -1}, {-1, 0}, {0, 1}};
        int[][] queen_movement = {{1, 1}, {-1, 1}, {-1, -1}, {1, -1}, {1, 0}, {0, -1}, {-1, 0}, {0, 1}};
        int[][] king_movement = {{1, 1}, {-1, 1}, {-1, -1}, {1, -1}, {1, 0}, {0, -1}, {-1, 0}, {0, 1}};
        int[][] knight_movement = {{1, 2}, {-1, 2}, {1, -2}, {-1, -2}, {2, 1}, {-2, 1}, {2, -1}, {-2, -1}};

        //find Bishop moves
        if (ChessPiece.PieceType.BISHOP == piece.getPieceType()) {
            for (int[] move : bishop_movement) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();

                while (true) {
                    row += move[0];
                    col += move[1];

                    if (ChessPosition.invalidPosition(row, col)) {
                        break;
                    }
                    ChessPosition newPosition = new ChessPosition(row, col);
                    ChessPiece otherPiece = board.getPiece(newPosition);

                    if (otherPiece != null && otherPiece.getTeamColor() == piece.getTeamColor()) {
                        break;
                    }
                    if (otherPiece != null && otherPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                        break;
                    }
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }

        //find Rook moves
        if (ChessPiece.PieceType.ROOK == piece.getPieceType()){
            for (int[] move : rook_movement) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();

                while (true) {
                    row += move[0];
                    col += move[1];

                    if (ChessPosition.invalidPosition(row, col)) {
                        break;
                    }
                    ChessPosition newPosition = new ChessPosition(row, col);
                    ChessPiece otherPiece = board.getPiece(newPosition);

                    if (otherPiece != null && otherPiece.getTeamColor() == piece.getTeamColor()) {
                        break;
                    }
                    if (otherPiece != null && otherPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                        break;
                    }
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }

        //find Queen Moves
        if (ChessPiece.PieceType.QUEEN == piece.getPieceType()) {
            for (int[] move : queen_movement) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();

                while (true) {
                    row += move[0];
                    col += move[1];

                    if (ChessPosition.invalidPosition(row, col)) {
                        break;
                    }
                    ChessPosition newPosition = new ChessPosition(row, col);
                    ChessPiece otherPiece = board.getPiece(newPosition);

                    if (otherPiece != null && otherPiece.getTeamColor() == piece.getTeamColor()) {
                        break;
                    }
                    if (otherPiece != null && otherPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                        break;
                    }
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }

        //find King moves
        if (ChessPiece.PieceType.KING == piece.getPieceType()) {
            for (int[] move : king_movement) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();

                row += move[0];
                col += move[1];

                if (ChessPosition.invalidPosition(row, col)) {
                    continue;
                }
                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece otherPiece = board.getPiece(newPosition);

                if (otherPiece != null && otherPiece.getTeamColor() == piece.getTeamColor()) {
                    continue;
                }
                if (otherPiece != null && otherPiece.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    continue;
                }
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }

        if (ChessPiece.PieceType.KING == piece.getPieceType()) {
            for (int[] move : king_movement) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();

                row += move[0];
                col += move[1];

                if (ChessPosition.invalidPosition(row, col)) {
                    continue;
                }
                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece otherPiece = board.getPiece(newPosition);

                if (otherPiece != null && otherPiece.getTeamColor() == piece.getTeamColor()) {
                    continue;
                }
                if (otherPiece != null && otherPiece.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    continue;
                }
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }

        if (ChessPiece.PieceType.KNIGHT == piece.getPieceType()) {
            for (int[] move : knight_movement) {
                int row = myPosition.getRow();
                int col = myPosition.getColumn();

                row += move[0];
                col += move[1];

                if (ChessPosition.invalidPosition(row, col)) {
                    continue;
                }
                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece otherPiece = board.getPiece(newPosition);

                if (otherPiece != null && otherPiece.getTeamColor() == piece.getTeamColor()) {
                    continue;
                }
                if (otherPiece != null && otherPiece.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    continue;
                }
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }

        if (ChessPiece.PieceType.PAWN == piece.getPieceType()) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();
            ChessPiece piece1 = null;
            ChessPiece piece2 = null;

            int dir = 1;
            if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                dir = -1;
            }

            ChessPosition newPosition1 = new ChessPosition(row + dir, col);
            ChessPosition newPosition2 = new ChessPosition(row + dir * 2, col);
            ChessPosition diag1 = new ChessPosition(row + dir, col + 1);
            ChessPosition diag2 = new ChessPosition(row + dir, col - 1);
            if (!ChessPosition.invalidPosition(diag1.getRow(),diag1.getColumn())) {
                piece1 = board.getPiece(diag1);
            }
            if (!ChessPosition.invalidPosition(diag2.getRow(),diag2.getColumn())) {
                piece2 = board.getPiece(diag2);
            }

            if (! ChessPosition.invalidPosition(row+dir, col) && board.getPiece(newPosition1) == null) {
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && newPosition1.getRow() == 8 || piece.getTeamColor() == ChessGame.TeamColor.BLACK && newPosition1.getRow() == 1){
                    moves.add(new ChessMove(myPosition, newPosition1, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, newPosition1, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, newPosition1, PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition, newPosition1, PieceType.BISHOP));
                }
                else {moves.add(new ChessMove(myPosition, newPosition1, null));}
            }

            if (! ChessPosition.invalidPosition(row+dir*2, col) && piece.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2 || piece.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7){
                if (board.getPiece(newPosition1) == null && board.getPiece(newPosition2) == null){
                    moves.add(new ChessMove(myPosition, newPosition2, null));
                }
            }

            if (! ChessPosition.invalidPosition(row+dir, col+1) &&piece1 != null && piece1.getTeamColor() != piece.getTeamColor()){
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && diag1.getRow() == 8 || piece.getTeamColor() == ChessGame.TeamColor.BLACK && diag1.getRow() == 1){
                    moves.add(new ChessMove(myPosition, diag1, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, diag1, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, diag1, PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition, diag1, PieceType.BISHOP));
                }
                else{moves.add(new ChessMove(myPosition, diag1, null));}
            }

            if (! ChessPosition.invalidPosition(row+dir, col-1) && piece2 != null && piece2.getTeamColor() != piece.getTeamColor()){
                if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && diag2.getRow() == 8 || piece.getTeamColor() == ChessGame.TeamColor.BLACK && diag2.getRow() == 1){
                    moves.add(new ChessMove(myPosition, diag2, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, diag2, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, diag2, PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition, diag2, PieceType.BISHOP));
                }
               else {moves.add(new ChessMove(myPosition, diag2, null));}
            }



        }

        return moves;
    }
}
