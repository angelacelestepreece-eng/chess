package chess;

import java.util.ArrayList;
import java.util.Collection;

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
        this.teamTurn = TeamColor.WHITE;
        board.resetBoard();
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

    public TeamColor opponentTeam(TeamColor color) {
        if (color == TeamColor.BLACK) {
            return TeamColor.WHITE;
        } else {
            return TeamColor.BLACK;
        }
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        TeamColor team = piece.getTeamColor();
        Collection<ChessMove> allMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : allMoves) {
            testMove(startPosition, move, piece, validMoves, team);
        }

        return validMoves;
    }

    public void testMove(ChessPosition startPosition, ChessMove move, ChessPiece piece, Collection<ChessMove> validMoves, TeamColor team) {
        ChessPosition end = move.getEndPosition();
        ChessPiece newPiece = board.getPiece(end);
        movePiece(startPosition, end, move, piece);
        boolean notInCheck = !isInCheck(team);
        board.removePiece(end);
        board.addPiece(startPosition, piece);
        if (newPiece != null) {
            board.addPiece(end, newPiece);
        }
        if (notInCheck) {
            validMoves.add(move);
        }
    }

    public void movePiece(ChessPosition startPosition, ChessPosition endPosition, ChessMove move, ChessPiece piece) {
        board.removePiece(startPosition);
        board.removePiece(endPosition);

        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
        if (promotionPiece != null) {
            board.addPiece(endPosition, new ChessPiece(piece.getTeamColor(), promotionPiece));
        } else {
            board.addPiece(endPosition, piece);
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPiece piece = board.getPiece(move.getStartPosition());

        if (piece == null || piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }
        Collection<ChessMove> validMoves = validMoves(startPosition);
        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        movePiece(startPosition, move.getEndPosition(), move, piece);
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
        if (kingPosition.isEmpty()) {
            return false;
        }

        ChessPosition kingPos = kingPosition.getFirst();
        ArrayList<ChessPosition> opponentPieces = ChessPiece.findTeamPositions(board, opponentTeam(teamColor));
        for (ChessPosition pos : opponentPieces) {
            ChessPiece piece = board.getPiece(pos);
            Collection<ChessMove> moves = piece.pieceMoves(board, pos);
            for (ChessMove move : moves) {
                if (move.getEndPosition().equals(kingPos)) {
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
        if (!isInCheck(teamColor)) {
            return false;
        }
        ArrayList<ChessPosition> positions = ChessPiece.findTeamPositions(board, teamColor);
        for (ChessPosition pos : positions) {
            ChessPiece piece = board.getPiece(pos);
            Collection<ChessMove> moves = piece.pieceMoves(board, pos);
            for (ChessMove move : moves) {
                ChessPiece newPiece = board.getPiece(move.getEndPosition());
                board.removePiece(pos);
                board.removePiece(move.getEndPosition());

                if (move.getPromotionPiece() != null) {
                    ChessPiece movedPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
                    board.addPiece(move.getEndPosition(), movedPiece);
                } else {
                    board.addPiece(move.getEndPosition(), piece);
                }

                boolean stillInCheck = isInCheck(teamColor);

                board.removePiece(move.getEndPosition());
                board.addPiece(pos, piece);
                if (newPiece != null) {
                    board.addPiece(move.getEndPosition(), newPiece);
                }

                if (!stillInCheck) {
                    return false;
                }

            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        ArrayList<ChessPosition> positions = ChessPiece.findTeamPositions(board, teamColor);
        for (ChessPosition pos : positions) {
            Collection<ChessMove> moves = (validMoves(pos));
            if (moves != null && !moves.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame that = (ChessGame) o;
        if (teamTurn != that.teamTurn) {
            return false;
        }
        if (board == null && that.board == null) {
            return true;
        }
        if (board == null || that.board == null) {
            return false;
        }
        return board.equals(that.board);
    }

    @Override
    public String toString() {
        return String.format("ChessGame: %s", (board == null ? "" : board.toString()));
    }

    @Override
    public int hashCode() {
        int k = (board == null ? 0 : board.hashCode());
        k = 31 * k + (teamTurn == null ? 0 : teamTurn.hashCode());
        return k;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE, BLACK
    }

}
