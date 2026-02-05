package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor color;
    private ChessPiece.PieceType ty;
    private boolean moved;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color=pieceColor;
        this.ty=type;
        this.moved=false;
    }

    public boolean getMoved(){
        return moved;
    }

    public void setMoved(){
        moved=true;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return ty;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves=new ArrayList<>();
        switch(ty){
            case KING:
                return kingMoves(board,myPosition);
            case QUEEN:
                return queenMoves(board,myPosition);
            case BISHOP:
                return bishopMoves(board,myPosition);
            case KNIGHT:
                return knightMoves(board,myPosition);
            case ROOK:
                return rookMoves(board,myPosition);
            case PAWN:
                return pawnMoves(board,myPosition);
            default:
                return moves;
        }
    }

    Collection<ChessMove> kingMoves(ChessBoard board,ChessPosition pos){
        Collection<ChessMove> moves=new ArrayList<>();
        int[][] gotos={{0,1},{0,-1},{1,0},{1,1},{1,-1},{-1,0},{-1,1},{-1,-1}};
        int r=pos.getRow();int c=pos.getColumn();int a;int b;
        for(int[] go:gotos){
            a=r+go[0];b=c+go[1];
            goodMoves(moves,board,r,c,a,b);
        }
        return moves;
    }

    Collection<ChessMove> queenMoves(ChessBoard board,ChessPosition pos){
        Collection<ChessMove> moves=new ArrayList<>();
        int[][] gotos={{0,1},{0,-1},{1,0},{1,1},{1,-1},{-1,0},{-1,1},{-1,-1}};
        slidingMoves(moves,board,pos,gotos);
        return moves;
    }

    Collection<ChessMove> bishopMoves(ChessBoard board,ChessPosition pos){
        Collection<ChessMove> moves=new ArrayList<>();
        int[][] gotos={{1,1},{1,-1},{-1,1},{-1,-1}};
        slidingMoves(moves,board,pos,gotos);
        return moves;
    }

    Collection<ChessMove> knightMoves(ChessBoard board,ChessPosition pos){
        Collection<ChessMove> moves=new ArrayList<>();
        int[][] gotos={{1,2},{1,-2},{2,1},{2,-1},{-1,2},{-1,-2},{-2,1},{-2,-1}};
        int r=pos.getRow(); int c=pos.getColumn();int a; int b;
        for(int[] go:gotos){
            a=r+go[0]; b=c+go[1];
            goodMoves(moves,board,r,c,a,b);
        }
        return moves;
    }

    Collection<ChessMove> rookMoves(ChessBoard board,ChessPosition pos){
        Collection<ChessMove> moves=new ArrayList<>();
        int[][] gotos={{1,0},{-1,0},{0,1},{0,-1}};
        slidingMoves(moves,board,pos,gotos);
        return moves;
    }

    Collection<ChessMove> pawnMoves(ChessBoard board,ChessPosition pos){
        Collection<ChessMove> moves=new ArrayList<>();
        int r=pos.getRow(); int c=pos.getColumn();
        int go; int start; int pro;
        if(this.color== ChessGame.TeamColor.WHITE){
            go=1; start=2; pro=8;
        }
        else{
            go=-1; start=7; pro=1;
        }
        int one=r+go;
        if (one > 0 && one < 9) {
            ChessPosition hop=new ChessPosition(one,c);
            if(board.getPiece(hop)==null){
                if(one==pro){
                    moves.add(new ChessMove(pos,hop,PieceType.QUEEN));
                    moves.add(new ChessMove(pos,hop,PieceType.KNIGHT));
                    moves.add(new ChessMove(pos,hop,PieceType.BISHOP));
                    moves.add(new ChessMove(pos,hop,PieceType.ROOK));
                }
                else{
                    moves.add(new ChessMove(pos,hop,null));
                }
            }
        }
        int two=r+(go*2);
        if(r==start && board.getPiece(new ChessPosition(one,c))==null){
            if(two>0 && two<9){
                ChessPosition jump=new ChessPosition(two,c);
                if(board.getPiece(jump)==null){
                    moves.add(new ChessMove(pos,jump,null));
                }
            }
        }
        int[][] ep={{go,1},{go,-1}};
        for(int[] e:ep){
            int nr=r+e[0]; int nc=c+e[1];
            if(!(nr<1 || nr>8 || nc<1 || nc>8)){
                ChessPosition npos=new ChessPosition(nr,nc);
                ChessPiece p= board.getPiece(npos);
                if(p!=null && p.getTeamColor()!=this.color){
                    if(nr==pro){
                        moves.add(new ChessMove(pos,npos,PieceType.QUEEN));
                        moves.add(new ChessMove(pos,npos,PieceType.KNIGHT));
                        moves.add(new ChessMove(pos,npos,PieceType.BISHOP));
                        moves.add(new ChessMove(pos,npos,PieceType.ROOK));
                    }
                    else{
                        moves.add(new ChessMove(pos,npos,null));
                    }
                }
            }
        }
        return moves;
    }

    public boolean goodMoves(Collection<ChessMove> moves,ChessBoard board,int or,int oc,int nr,int nc){
        if(nr<1 || nr>8 || nc<1 || nc>8)return false;
        ChessPosition npos=new ChessPosition(nr,nc);
        ChessPiece np= board.getPiece(npos);
        if(np==null){
            moves.add(new ChessMove(new ChessPosition(or,oc),npos,null));
            return true;
        }
        if(np.getTeamColor()!=this.color){
            moves.add(new ChessMove(new ChessPosition(or,oc),npos,null));
            return false;
        }
        return false;
    }

    public void slidingMoves(Collection<ChessMove> moves,ChessBoard board, ChessPosition pos, int[][] direction){
        int or= pos.getRow(); int oc= pos.getColumn();
        for(int[] dir:direction){
            int nr=or+dir[0];
            int nc=oc+dir[1];
            while(goodMoves(moves,board,or,oc,nr,nc)){
                nr+=dir[0]; nc+=dir[1];
            }
        }
    }

    @Override
    public boolean equals(Object o){
        if(this==o)return true;
        if(o==null || getClass()!=o.getClass())return false;
        ChessPiece no=(ChessPiece) o;
        return (color==no.color && ty==no.ty);
    }

    @Override
    public int hashCode(){
        return 31*color.hashCode()+ty.hashCode();
    }
}
