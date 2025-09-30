package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        this.board = new ChessBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    public TeamColor opponentTeam(TeamColor color){
        if(color == TeamColor.BLACK){
            return TeamColor.WHITE;
        }
        else{
            return TeamColor.BLACK;
        }
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece myPiece = board.getPiece(startPosition);
        return myPiece.pieceMoves(board, startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null || piece.getTeamColor() != teamTurn){
            throw new InvalidMoveException();
        }
        Collection<ChessMove> validMoves = piece.pieceMoves(board, startPosition);
        if (!validMoves.contains(move)){
            throw new InvalidMoveException();
        }

        ChessPiece captured = board.getPiece(endPosition);
        board.removePiece(startPosition);
        board.removePiece(endPosition);

        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
        if (promotionPiece != null) {
            board.addPiece(endPosition, new ChessPiece(piece.getTeamColor(), promotionPiece));
        }
        else{
            board.addPiece(endPosition, piece);
        }

        if(isInCheck(piece.getTeamColor())){
            board.removePiece(endPosition);
            board.addPiece(startPosition,piece);
            if(captured != null){
                board.addPiece(endPosition, captured);
            }
            throw new InvalidMoveException();
        }

        setTeamTurn(opponentTeam(teamTurn));
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ArrayList<ChessPosition> kingPosition = ChessPiece.findPiecePositionsForTeam(ChessPiece.PieceType.KING, board, teamColor);
        if (kingPosition.isEmpty()) return false;

        ChessPosition kingPos = kingPosition.getFirst();
        ArrayList<ChessPosition> opponentPieces = ChessPiece.findTeamPositions(board, opponentTeam(teamColor));
        for (ChessPosition pos : opponentPieces){
            ChessPiece piece = board.getPiece(pos);
            Collection<ChessMove> moves = piece.pieceMoves(board, pos);
            for (ChessMove move : moves){
                if(move.getEndPosition().equals(kingPos)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if(o==this)return true;
        if(o==null || getClass() != o.getClass()) return false;
        ChessGame that = (ChessGame) o;
        if(board == null && that.board == null)return true;
        if(board == null || that.board == null)return false;
        return board.equals(that.board);
    }

    @Override
    public String toString() {
        return String.format("ChessGame: %s",(board==null? "" : board.toString()));
    }

    @Override
    public int hashCode() {
        return 31 * (board==null? 0 : board.hashCode());
    }

}
