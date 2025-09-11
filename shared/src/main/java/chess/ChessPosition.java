package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private int row;
    private int column;

    public ChessPosition(int row, int col) {

    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow(){
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return column;
    }

    public static boolean invalidPosition(int row, int col) {
        return row < 1 || row > 8 || col < 1 || col > 8;
    }
}
