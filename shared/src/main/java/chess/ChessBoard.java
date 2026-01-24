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
        //rook
        addPiece(new ChessPosition(1, 1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8, 1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1, 8), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8, 8), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        //knight
        addPiece(new ChessPosition(1, 2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1, 7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        //bishop
        addPiece(new ChessPosition(1, 3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8, 3), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1, 6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8, 6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        //royals
        addPiece(new ChessPosition(1, 4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8, 4), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(1, 5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(8, 5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
    }

    //rewrite
    @Override
    public boolean equals(Object my_obj){
        if (this == my_obj){return true;}
        if (my_obj == null || getClass() != my_obj.getClass()){return false;}
        ChessBoard new_obj = (ChessBoard) my_obj;
        for (int y = 1; y < 9; y++){
            for (int x = 1; x < 9; x++){
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
        for (int y = 1; y < 9; y++){
            for (int x = 1; x < 9; x++){
                ChessPiece piece = game_board[y][x];
                int value = 0;
                if (piece != null){value = piece.hashCode();}
                answer = (31 * answer + value);
            }
        }
        return answer;
    }
}
