package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn;
    private ChessBoard myboard;

    public ChessGame() {
        this.turn = TeamColor.WHITE;
        this.myboard=new ChessBoard();
        this.myboard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn=team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece p=myboard.getPiece(startPosition);
        Collection<ChessMove> legal=new ArrayList<>();
        if(p==null)return legal;
        Collection<ChessMove> valid=p.pieceMoves(myboard,startPosition);
        ChessGame.TeamColor mp = p.getTeamColor();

        for(ChessMove move:valid){
            ChessBoard copy=copyBoard(myboard);
            finishMove(copy,move);
            if(!isInCheck(mp,copy))legal.add(move);
        }
        return legal;
    }

    public void finishMove(ChessBoard board, ChessMove move){
        ChessPiece p=board.getPiece(move.getStartPosition());
        ChessPiece.PieceType pro=move.getPromotionPiece();
        board.addPiece(move.getStartPosition(),null);
        if(pro!=null){
            ChessPiece np=new ChessPiece(p.getTeamColor(),pro);
            board.addPiece(move.getEndPosition(),np);
        }
        else{
            board.addPiece(move.getEndPosition(),p);
        }
    }

    public ChessBoard copyBoard(ChessBoard board){
        ChessBoard copy=new ChessBoard();
        for(int a=1;a<9;a++){
            for(int b=1;b<9;b++){
                ChessPosition pos=new ChessPosition(a,b);
                ChessPiece p=board.getPiece(pos);
                if(p!=null){
                    ChessPiece np=new ChessPiece(p.getTeamColor(),p.getPieceType());
                    copy.addPiece(pos,np);
                }
            }
        }
        return copy;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece p= myboard.getPiece(move.getStartPosition());
        if(p==null)throw new InvalidMoveException("Null piece");
        if(p.getTeamColor()!=turn)throw new InvalidMoveException("Not turn");

        Collection<ChessMove> valid=validMoves(move.getStartPosition());
        boolean ans=false;
        if(valid!=null){
            for(ChessMove option:valid){
                if(sameMove(option,move)){
                    ans=true;break;
                }
            }
        }
        if(!ans)throw new InvalidMoveException("Wrong");
        finishMove(myboard,move);

        if(turn==TeamColor.WHITE){
            turn=TeamColor.BLACK;
        }
        else{
            turn=TeamColor.WHITE;
        }
    }

    public boolean sameMove(ChessMove one, ChessMove two){
        if(one.getStartPosition().getRow()!=two.getStartPosition().getRow())return false;
        if(one.getStartPosition().getColumn()!=two.getStartPosition().getColumn())return false;
        if(one.getEndPosition().getRow()!=two.getEndPosition().getRow())return false;
        if(one.getEndPosition().getColumn()!=two.getEndPosition().getColumn())return false;
        ChessPiece.PieceType a=one.getPromotionPiece();
        ChessPiece.PieceType b=two.getPromotionPiece();
        if(a==null && b==null)return true;
        if(a==null || b==null)return false;
        return a==b;
    }

    public boolean isInCheck(TeamColor teamColor, ChessBoard copy){
        ChessPosition king=kingPos(teamColor,copy);
        if(king==null)return false;
        for(int a=1;a<9;a++){
            for(int b=1;b<9;b++){
                ChessPosition pos=new ChessPosition(a,b);
                ChessPiece p=copy.getPiece(pos);
                if(p!=null && p.getTeamColor()!=teamColor){
                    Collection<ChessMove> moves=p.pieceMoves(copy,pos);
                    for(ChessMove move:moves){
                        ChessPosition epos=move.getEndPosition();
                        if(epos.getRow()==king.getRow() && epos.getColumn()==king.getColumn())return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, myboard);
    }

    public ChessPosition kingPos(TeamColor color,ChessBoard board){
        for(int a=1;a<9;a++){
            for(int b=1;b<9;b++){
                ChessPosition pos=new ChessPosition(a,b);
                ChessPiece p=board.getPiece(pos);
                if(p!=null && p.getTeamColor()==color && p.getPieceType()== ChessPiece.PieceType.KING)return pos;
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if(!isInCheck(teamColor))return false;
        TeamColor lastturn=turn;
        turn=teamColor;
        for(int a=1;a<9;a++){
            for(int b=1;b<9;b++){
                ChessPosition pos=new ChessPosition(a,b);
                ChessPiece p=myboard.getPiece(pos);
                if(p!=null && p.getTeamColor()==teamColor){
                    Collection<ChessMove> moves=validMoves(pos);
                    if(moves!=null && !moves.isEmpty()){
                        turn=lastturn; return false;
                    }
                }
            }
        }
        turn=lastturn; return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if(isInCheck(teamColor))return false;
        TeamColor lastturn=turn;
        turn=teamColor;
        for(int a=1;a<9;a++){
            for(int b=1;b<9;b++){
                ChessPosition pos=new ChessPosition(a,b);
                ChessPiece p=myboard.getPiece(pos);
                if(p!=null && p.getTeamColor()==teamColor){
                    Collection<ChessMove> moves=validMoves(pos);
                    if(moves!=null && !moves.isEmpty()){
                        turn=lastturn; return false;
                    }
                }
            }
        }
        turn=lastturn; return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.myboard=board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return myboard;
    }

    @Override
    public boolean equals(Object o){
        if(this==o)return true;
        if(o==null || getClass()!=o.getClass())return false;
        ChessGame no= (ChessGame) o;
        return turn==no.turn && myboard.equals(no.myboard);
    }

    @Override
    public int hashCode(){
        return 31*turn.hashCode()+ myboard.hashCode();
    }
}
