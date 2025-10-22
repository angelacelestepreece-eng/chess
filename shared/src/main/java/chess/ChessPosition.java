package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int row;
    private final int column;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.column = col;
    }

    public static boolean invalidPosition(int row, int col) {
        return row < 1 || row > 8 || col < 1 || col > 8;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)", row, column);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition position = (ChessPosition) o;
        return row == position.row && column == position.column;
    }

    @Override
    public int hashCode() {
        return 31 * row + column;
    }

}
