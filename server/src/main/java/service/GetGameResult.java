package service;

import chess.ChessBoard;
import chess.ChessGame;

public record GetGameResult(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        ChessBoard board,
        ChessGame.TeamColor turn
) {}
