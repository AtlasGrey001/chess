package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
//not
public class ChessGame {
    private TeamColor team_turn;
    private ChessBoard game_board;

    public ChessGame() {
        game_board = new ChessBoard();
        game_board.resetBoard();
        //white will always go first
        team_turn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return team_turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.team_turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    //makes a copy of the game board and returns it
    public ChessBoard copyBoard(ChessBoard board){
        ChessBoard board_copy = new ChessBoard();
        for (int y = 1; y < 9; y++){
            for (int x = 1; x < 9; x++){
                ChessPosition pos = new ChessPosition(y,x);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null){
                    ChessPiece new_piece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                    board_copy.addPiece(pos, new_piece);
                }
            }
        }
        return board_copy;
    }

    public void finishMove(ChessBoard board, ChessMove move){
        ChessPiece piece = board.getPiece(move.getStartPosition());
        ChessPiece.PieceType promo_piece = move.getPromotionPiece();
        board.addPiece(move.getStartPosition(), null);

        if (promo_piece != null){
            ChessPiece new_piece = new ChessPiece(piece.getTeamColor(), promo_piece);
            board.addPiece(move.getEndPosition(), new_piece);
        }
        else {board.addPiece(move.getEndPosition(), piece);}
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = game_board.getPiece(startPosition);
        if (piece == null){return null;}
        if (piece.getTeamColor() != team_turn){return null;}

        Collection<ChessMove> all_moves = piece.pieceMoves(game_board, startPosition);
        Collection<ChessMove> legal_moves = new ArrayList<>();
        for (ChessMove option : all_moves){
            ChessBoard board_copy = copyBoard(game_board);
            finishMove(board_copy, option);
            if (!isInCheck(team_turn, board_copy)){legal_moves.add(option);}
        }
        return legal_moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = game_board.getPiece(move.getStartPosition());
        Collection<ChessMove> valid = validMoves(move.getStartPosition());
        if (move.getPromotionPiece() != null){
            game_board.addPiece(move.getEndPosition(), new ChessPiece(team_turn, move.getPromotionPiece()));
            //...............
        }
    }

    //finds the king square
    public ChessPosition kingPosition(TeamColor color, ChessBoard board){
        for (int y = 1; y < 9; y++){
            for (int x = 1; x < 8; x++){
                ChessPosition pos = new ChessPosition(y,x);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == color && piece.getPieceType() == ChessPiece.PieceType.KING){
                    return pos;}
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition king_pos = kingPosition(teamColor, game_board);
        if (king_pos == null){return false;}

        for (int y = 1; y < 9; y++){
            for (int x = 1; x < 9; x++){
                ChessPosition pos = new ChessPosition(y,x);
                ChessPiece piece = game_board.getPiece(pos);
                if (piece != null && game_board.getTeamColor() != teamColor){
                    Collection<ChessMove> new_moves = piece.pieceMoves(game_board, pos);
                    for (ChessMove option : new_moves){
                        ChessPosition end_pos = option.getEndPosition();
                        if (end_pos.getRow() == king_pos.getRow() && end_pos.getColumn() == king_pos.getColumn()){
                            return true;}
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        //throw new RuntimeException("Not implemented");
        //if (!isInCheck(teamColor)){return false;}
        //else{for(){...}}
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        //throw new RuntimeException("Not implemented");
        //if (isInCheck(smt)){return false;}
        //else{for(){...}}
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.game_board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return game_board;
    }
}
