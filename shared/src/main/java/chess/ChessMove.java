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
}
