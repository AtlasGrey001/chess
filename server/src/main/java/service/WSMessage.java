package service;

import chess.ChessMove;

public record WSMessage(String type,ChessMove move) {}
