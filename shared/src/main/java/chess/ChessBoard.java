package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] myboard;

    public ChessBoard() {
        this.myboard = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int r=calcRow(position); int c=calcCol(position);
        myboard[r][c]=piece;
    }

    public int calcRow(ChessPosition pos){
        return pos.getRow()-1;
    }
    public int calcCol(ChessPosition pos){
        return pos.getColumn()-1;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int r=calcRow(position); int c=calcCol(position);
        return myboard[r][c];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        ChessPiece.PieceType[] backrow = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };

        for (int a = 0; a < 8; a++) {
            for (int b = 0; b < 8; b++) {
                myboard[a][b] = null;
            }
        }

        for (int b = 1; b < 9; b++) {
            addPiece(new ChessPosition(2, b), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, b), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(1, b), new ChessPiece(ChessGame.TeamColor.WHITE, backrow[b-1]));
            addPiece(new ChessPosition(8, b), new ChessPiece(ChessGame.TeamColor.BLACK, backrow[b-1]));
        }
    }

    @Override
    public boolean equals(Object o){
        if(this==o)return true;
        if(o==null || getClass()!=o.getClass())return false;
        ChessBoard no = (ChessBoard) o;
        for(int a=0;a<8;a++){
            for(int b=0;b<8;b++){
                ChessPiece one = this.myboard[a][b]; ChessPiece two = no.myboard[a][b];
                if(one==null && two==null)continue;
                if(one==null || two==null)return false;
                if(!one.equals(two))return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode(){
        int ans=1;
        for(int a=0;a<8;a++){
            for(int b=0;b<8;b++){
                ChessPiece p = myboard[a][b];
                int val=0;
                if(p!=null)val=p.hashCode();
                ans=31*ans+val;
            }
        }
        return ans;
    }
    }
