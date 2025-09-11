package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    private ChessPosition startPosition;
    private ChessPosition endPosition;
    private ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;


    }

    public boolean validMove(ChessPiece piece, int row, int col, int newRow, int newCol) {
//        if (piece.pieceType == ChessPiece.PieceType.KING){
//            return kingValidMove(row, col, newRow, newCol);
//        }
//        if (piece.pieceType == ChessPiece.PieceType.QUEEN){
//            return queenValidMove(row, col, newRow, newCol);
//        }
        if (piece.getPieceType() == ChessPiece.PieceType.BISHOP){
            return isBishopValidMove(row, col, newRow, newCol);
        }
//        if (piece.pieceType == ChessPiece.PieceType.ROOK){
//            return rookValidMove(row, col, newRow, newCol);
//        }
//        if (piece.pieceType == ChessPiece.PieceType.PAWN){
//            return pawnValidMove(row, col, newRow, newCol);
//        }
        else{
            return false;
        }
    }

    public boolean isBishopValidMove(int row, int col, int newRow, int newCol){
        if (chess.ChessPosition.invalidPosition(newRow, newCol)){
            return false;
        }
        return Math.abs(col - newCol) == Math.abs(row - newRow);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessMove move = (ChessMove) o;
        return (startPosition.equals(move.startPosition) && endPosition.equals(move.endPosition)
                && promotionPiece == move.promotionPiece);
    }

    @Override
    public int hashCode() {
        var promotionCode = (promotionPiece == null ?
                9 : promotionPiece.ordinal());
        return (71 * startPosition.hashCode()) + endPosition.hashCode() + promotionCode;
    }

    @Override
    public String toString() {
        var p = (promotionPiece == null ? "" : ":" + promotionPiece);
        return String.format("%s:%s%s", startPosition.toString(), endPosition.toString(), p);
    }


}
