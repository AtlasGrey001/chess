package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
//done?
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
        if (row_new < 1 || row_new > 8 || col_new < 1 || col_new > 8){return false;}

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
            int y = row_old + square[0];
            int x = col_old + square[1];
            while (goodMove(good_moves, board, row_old, col_old, y, x) == true){
                y += square[0];
                x += square[1];
            }
        }
    }

    //the king piece
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

    //the queen
    public Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> good_moves = new ArrayList<>();
        int[][] move_tos = {{0,1},{0,-1},{1,0},{1,1},{1,-1},{-1,0},{-1,1},{-1,-1}};
        slidingMoves(good_moves, board, myPosition, move_tos);
        return good_moves;
    }

    //the bishop
    public Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> good_moves = new ArrayList<>();
        int[][] move_tos = {{1,1},{1,-1},{-1,1},{-1,-1}};
        slidingMoves(good_moves, board, myPosition, move_tos);
        return good_moves;
    }

    //the knight
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

    //the rook
    public Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> good_moves = new ArrayList<>();
        int[][] move_tos = {{0,1},{0,-1},{1,0},{-1,0}};
        slidingMoves(good_moves, board, myPosition, move_tos);
        return good_moves;
    }

    //the pawn
    public Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> good_moves = new ArrayList<>();
        int y = myPosition.getRow();
        int x = myPosition.getColumn();
        int move_to;
        int start_row;
        int promo_row;

        if (this.piece_color == ChessGame.TeamColor.WHITE){
            move_to = 1;
            start_row = 2;
            promo_row = 8;
        }
        else{
            move_to = -1;
            start_row = 7;
            promo_row = 1;
        }

        //one jump forward
        int one_row = y + move_to;
        if (one_row > 0 && one_row < 9){
            ChessPosition one_jump = new ChessPosition(one_row, x);
            if (board.getPiece(one_jump) == null){
                //promotion
                if (one_row == promo_row){
                    good_moves.add(new ChessMove(myPosition, one_jump, PieceType.QUEEN));
                    good_moves.add(new ChessMove(myPosition, one_jump, PieceType.BISHOP));
                    good_moves.add(new ChessMove(myPosition, one_jump, PieceType.KNIGHT));
                    good_moves.add(new ChessMove(myPosition, one_jump, PieceType.ROOK));
                }
                else{good_moves.add(new ChessMove(myPosition, one_jump, null));}
            }
        }

        //two jump forward, from start only
        int two_row = y + (move_to * 2);
        if (y == start_row && board.getPiece(new ChessPosition(one_row, x)) == null){
            if (two_row > 0 && two_row < 9){
                ChessPosition two_jump = new ChessPosition(two_row, x);
                if (board.getPiece(two_jump) == null){
                    good_moves.add(new ChessMove(myPosition, two_jump, null));
                }
            }
        }

        //capture enemy piece, and promo possible...
        int[][] enemy_pos = {{move_to,1},{move_to,-1}};
        for (int[] enemy : enemy_pos){
            int y_new = y + enemy[0];
            int x_new = x + enemy[1];
            if ((y_new < 1 || y_new > 8 || x_new < 1 || x_new > 8) == false){
                ChessPosition new_pos = new ChessPosition(y_new, x_new);
                ChessPiece square = board.getPiece(new_pos);

                if (square != null && square.getTeamColor() != this.piece_color){
                    if (y_new == promo_row){
                        good_moves.add(new ChessMove(myPosition, new_pos, PieceType.QUEEN));
                        good_moves.add(new ChessMove(myPosition, new_pos, PieceType.BISHOP));
                        good_moves.add(new ChessMove(myPosition, new_pos, PieceType.KNIGHT));
                        good_moves.add(new ChessMove(myPosition, new_pos, PieceType.ROOK));
                    }
                    else {good_moves.add(new ChessMove(myPosition, new_pos, null));}
                }
            }
        }
        return good_moves;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
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
