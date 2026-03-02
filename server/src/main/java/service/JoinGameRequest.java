package service;

public record JoinGameRequest(String authToken, Integer gameID, String playerColor) {}

