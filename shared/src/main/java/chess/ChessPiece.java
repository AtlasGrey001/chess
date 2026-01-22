package chess;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private ChessGame.TeamColor piece_color;
    private PieceType piece_type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.piece_color = pieceColor;
        this.piece_type = type;
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
        return piece_color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return piece_type;
    }

    public boolean goodMove(Collection<ChessMove> good_moves, ChessBoard board, int row_old, int col_old, int row_new, int col_new){
        if (row_new < 1 && row_new > 8 && col_new < 1 && col_new > 8){return false;}

        ChessPosition pos_new = new ChessPosition(row_new, col_new);
        ChessPiece move_to = board.getPiece(pos_new);

        if (move_to == null){
            good_moves.add(new ChessMove(new ChessPosition(row_old, col_old), pos_new, null));
            return true;
        }

        if (move_to.getTeamColor() != this.piece_color){
            good_moves.add(new ChessMove(new ChessPosition(row_old, col_old), pos_new, null));
        }
        return false;
    }

    public void slidingMoves(Collection<ChessMove> good_moves, ChessBoard board, ChessPosition myPosition, int[][] move_tos){
        int row_old = myPosition.getRow();
        int col_old = myPosition.getColumn();
        for (int[] square : move_tos){
            int y = row_old + move_tos[0];
            int x = col_old + move_tos[1];
            while (goodMove(good_moves, board, row_old, col_old, y, x) == true){
                y = y + move_tos[0];
                x = x + move_tos[1];
            }
        }
    }

    /**
     *
     * @param board
     * @param myPosition
     * @return
     */
    public Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> good_moves = new ArrayList<>();
        int[][] move_tos = {{0,1},{0,-1},{1,0},{1,1},{1,-1},{-1,0},{-1,1},{-1,-1}};
        int y = myPosition.getRow();
        int x = myPosition.getColumn();
        int a;
        int b;

        for (int[] square : move_tos){
            a = y + square[0];
            b = x + square[1];
            goodMove(good_moves, board, y, x, a, b);
        }
        return good_moves;
    }

    /**
     *
     * @param board
     * @param myPosition
     * @return
     */
    public Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> good_moves = new ArrayList<>();
        int[][] move_tos = {{0,1},{0,-1},{1,0},{1,1},{1,-1},{-1,0},{-1,1},{-1,-1}};
        slidingMoves(good_moves, board, myPosition, move_tos);
        return good_moves;
    }

    public Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> good_moves = new ArrayList<>();
        int[][] move_tos = {{1,1},{1,-1},{-1,1},{-1,-1}};
        slidingMoves(good_moves, board, myPosition, move_tos);
        return good_moves;
    }

    public Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> good_moves = new ArrayList<>();
        int[][] move_tos = {{1,2},{1,-2},{2,1},{2,-1},{-1,2},{-1,-2},{-2,1},{-2,-1}};
        int y = myPosition.getRow();
        int x = myPosition.getColumn();
        int a;
        int b;

        for (int[] square : move_tos){
            a = y + square[0];
            b = x + square[1];
            goodMove(good_moves, board, y, x, a, b);
        }
        return good_moves;
    }

    public Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> good_moves = new ArrayList<>();
        int[][] move_tos = {{0,1},{0,-1},{1,0},{-1,0}};
        slidingMoves(good_moves, board, myPosition, move_tos);
        return good_moves;
    }

    public Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition){

    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        //throw new RuntimeException("Not implemented");
        Collection<ChessMove> my_moves = new ArrayList<>();
        switch (piece_type){
            case KING:
                return kingMoves(board, myPosition);
            case QUEEN:
                return queenMoves(board, myPosition);
            case BISHOP:
                return bishopMoves(board, myPosition);
            case KNIGHT:
                return knightMoves(board, myPosition);
            case ROOK:
                return rookMoves(board, myPosition);
            case PAWN:
                return pawnMoves(board, myPosition);
            default:
                return my_moves;
        }
    }
}
