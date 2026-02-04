package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    private ChessPosition spos;
    private ChessPosition epos;
    private ChessPiece.PieceType promo;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.spos=startPosition;
        this.epos=endPosition;
        this.promo=promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return spos;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return epos;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promo;
    }

    @Override
    public boolean equals(Object o){
        if(this==o)return true;
        if(o==null || getClass() != o.getClass())return false;
        ChessMove no = (ChessMove) o;
        if(!spos.equals(no.spos))return false;
        if(!epos.equals(no.epos))return false;
        if(promo==null && no.promo==null)return true;
        if(promo==null || no.promo==null)return false;
        return promo==no.promo;
    }

    @Override
    public int hashCode(){
        int ans = 31*spos.hashCode()+epos.hashCode();
        int val=0;
        if(promo!=null)val=promo.hashCode();
        return 31*ans+val;
    }
}
