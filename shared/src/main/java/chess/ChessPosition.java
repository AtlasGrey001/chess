package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
//done
public class ChessPosition {
    private int piece_col;
    private int piece_row;

    public ChessPosition(int row, int col) {
        piece_col = col;
        piece_row = row;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return piece_row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return piece_col;
    }

    //rewrite what equals means
    @Override
    public boolean equals(Object my_object){
        if (this == my_object){return true;}
        if (my_object == null || getClass() != my_object.getClass()){return false;}
        ChessPosition new_obj = (ChessPosition) my_object;
        boolean answer = piece_row == new_obj.piece_row && piece_col == new_obj.piece_col;
        return answer;
    }

    //rewrite this one too
    @Override
    public int hashCode(){
        return (31 * piece_row + piece_col);
    }
}
