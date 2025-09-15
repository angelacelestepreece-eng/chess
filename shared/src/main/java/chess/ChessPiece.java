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

        //I'm just testing Bishop Moves & Rook Moves & Queen Moves & King Moves
        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.BISHOP && piece.getPieceType() != ChessPiece.PieceType.ROOK && piece.getPieceType() != ChessPiece.PieceType.QUEEN && piece.getPieceType() != ChessPiece.PieceType.KING) {
            return new ArrayList<>();
        }

        Collection<ChessMove> moves = new ArrayList<>();
        int[][] bishop_movement = {{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
        int[][] rook_movement = {{1, 0}, {0, -1}, {-1, 0}, {0, 1}};
        int[][] queen_movement = {{1, 1}, {-1, 1}, {-1, -1}, {1, -1}, {1, 0}, {0, -1}, {-1, 0}, {0, 1}};
        int[][] king_movement = {{1, 1}, {-1, 1}, {-1, -1}, {1, -1}, {1, 0}, {0, -1}, {-1, 0}, {0, 1}};

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

        return moves;
    }
}
