package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
//done
public class ChessBoard {

    private ChessPiece[][] game_board;

    public ChessBoard() {
        game_board = new ChessPiece[8][8];
    }

    public int calcRow(ChessPosition position){return position.getRow() - 1;}

    public int calcColumn(ChessPosition position){return position.getColumn() - 1;}

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        //throw new RuntimeException("Not implemented");
        int y = calcRow(position);
        int x = calcColumn(position);
        game_board[y][x] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        //throw new RuntimeException("Not implemented");
        int y = calcRow(position);
        int x = calcColumn(position);
        return game_board[y][x];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //throw new RuntimeException("Not implemented");
        //clear
        for (int y = 0; y < 8; y++){
            for (int x = 0; x < 8; x++){game_board[y][x] = null;}
        }
        //reset pieces
        //pawn
        for (int x = 1; x <= 8; x++){
            addPiece(new ChessPosition(2, x), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, x), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }

        ChessPiece.PieceType[] back_row = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };

        //rook - royals
        for (int x = 1; x <= 8; x++){
            addPiece(new ChessPosition(1,x), new ChessPiece(ChessGame.TeamColor.WHITE, back_row[x-1]));
            addPiece(new ChessPosition(8,x), new ChessPiece(ChessGame.TeamColor.BLACK, back_row[x-1]));
        }
    }

    //rewrite
    @Override
    public boolean equals(Object my_obj){
        if (this == my_obj){return true;}
        if (my_obj == null || getClass() != my_obj.getClass()){return false;}
        ChessBoard new_obj = (ChessBoard) my_obj;
        for (int y = 0; y < 8; y++){
            for (int x = 0; x < 8; x++){
                ChessPiece piece_one = this.game_board[y][x];
                ChessPiece piece_two = new_obj.game_board[y][x];
                if (piece_one == null && piece_two == null){continue;}
                if (piece_one == null || piece_two == null){return false;}
                if (!piece_one.equals(piece_two)){return false;}
            }
        }
        return true;
    }

    //rewrite
    @Override
    public int hashCode(){
        //we don't want 31 * 0 bc that = 0
        int answer = 1;
        for (int y = 0; y < 8; y++){
            for (int x = 0; x < 8; x++){
                ChessPiece piece = game_board[y][x];
                int value = 0;
                if (piece != null){value = piece.hashCode();}
                answer = (31 * answer + value);
            }
        }
        return answer;
    }
}
