package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
//done
public class ChessMove {
    private ChessPosition start_pos;
    private ChessPosition end_pos;
    private ChessPiece.PieceType promo_piece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        start_pos = startPosition;
        end_pos = endPosition;
        promo_piece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return start_pos;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return end_pos;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {return promo_piece;}

    //rewrite
    @Override
    public boolean equals(Object my_obj){
        if (this == my_obj){return true;}
        if (my_obj == null || getClass() != my_obj.getClass()){return false;}
        ChessMove new_obj = (ChessMove) my_obj;
        if (!start_pos.equals(new_obj.start_pos)){return false;}
        if (!end_pos.equals(new_obj.end_pos)){return false;}
        if (promo_piece == null && new_obj.promo_piece == null){return true;}
        if (promo_piece == null || new_obj.promo_piece == null){return false;}
        boolean answer = (promo_piece == new_obj.promo_piece);
        return answer;
    }

    //rewrite
    @Override
    public int hashCode(){
        int answer = start_pos.hashCode();
        answer = (31 * answer + end_pos.hashCode());
        int value = 0;
        if (promo_piece != null){value = promo_piece.hashCode();}
        answer = (31 * answer + value);
        return answer;
    }
}
