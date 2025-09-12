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
        int originalRow = myPosition.getRow();
        int originalCol = myPosition.getColumn();

        //I'm just testing Bishop Moves
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.BISHOP){
            return new HashSet<ChessMove>();
        }

        Collection<ChessMove> bishopMoves = new ArrayList<>();
        int[][] bishop_movement = {{1,1}, {-1,1}, {-1,-1},{1,-1}};
        for (int[] move:bishop_movement) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true){
                row += move[0];
                col += move[1];

                if (ChessPosition.invalidPosition(row, col)){
                    break;
                }
                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece otherPiece = board.getPiece(newPosition);

                if (otherPiece != null && otherPiece.getTeamColor() == piece.getTeamColor()){
                    break;
                }
                if (otherPiece != null && otherPiece.getTeamColor() != piece.getTeamColor()){
                    bishopMoves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                }
                bishopMoves.add(new ChessMove(myPosition, newPosition, null));
            }
        }

        return bishopMoves;
    }
}
