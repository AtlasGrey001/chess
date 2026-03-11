package service.requests;

public record JoinGameRequest(String authToken,Integer gameID,String playerColor) {}
