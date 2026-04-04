package ui;

import chess.*;

public class BoardRender {
    public static void drawBoard(ChessGame game,ChessGame.TeamColor color) {
        var board=game.getBoard();
        System.out.print(EscapeSequences.ERASE_SCREEN);
        if (color==ChessGame.TeamColor.WHITE) {
            drawWhite(board);
        } else {
            drawBlack(board);
        }
        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
    }

    private static void drawWhite(ChessBoard board) {
        System.out.println("   a   b   c  d   e   f   g   h");
        for (int r=8;r>=1;r--) {
            System.out.print(r+" ");
            for (int c=1;c<=8;c++) {
                drawSquare(board,r,c);
            }
            System.out.println(" "+r);
        }
        System.out.println("   a   b   c  d   e   f   g   h");
    }

    private static void drawBlack(ChessBoard board) {
        System.out.println("   h   g   f  e   d   c   b   a");
        for (int r=1;r<=8;r++) {
            System.out.print(r+" ");
            for (int c=8;c>=1;c--) {
                drawSquare(board,r,c);
            }
            System.out.println(" "+r);
        }
        System.out.println("   h   g   f  e   d   c   b   a");
    }

    private static void drawSquare(ChessBoard board, int row, int col) {
        boolean light=((row+col)%2==0);
        var piece=board.getPiece(new ChessPosition(row,col));

        String bg=light ? EscapeSequences.SET_BG_COLOR_DARK_GREY : EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        String symbol=EscapeSequences.EMPTY;
        if (piece!=null) {
            symbol=switch (piece.getTeamColor()) {
                case WHITE->switch (piece.getPieceType()) {
                    case KING->EscapeSequences.WHITE_KING;
                    case QUEEN->EscapeSequences.WHITE_QUEEN;
                    case ROOK->EscapeSequences.WHITE_ROOK;
                    case BISHOP->EscapeSequences.WHITE_BISHOP;
                    case KNIGHT->EscapeSequences.WHITE_KNIGHT;
                    case PAWN->EscapeSequences.WHITE_PAWN;
                };
                case BLACK->switch (piece.getPieceType()) {
                    case KING->EscapeSequences.BLACK_KING;
                    case QUEEN->EscapeSequences.BLACK_QUEEN;
                    case ROOK->EscapeSequences.BLACK_ROOK;
                    case BISHOP->EscapeSequences.BLACK_BISHOP;
                    case KNIGHT->EscapeSequences.BLACK_KNIGHT;
                    case PAWN->EscapeSequences.BLACK_PAWN;
                };
            };
        }
        System.out.print(bg+symbol+EscapeSequences.RESET_BG_COLOR);
    }
}
