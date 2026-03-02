package model;

public record GameData(int gameID,String gameName,String whiteUsername,String blackUsername,boolean gameOver) {

    public GameData withWhite(String username) {
        return new GameData(gameID,gameName,username,blackUsername,gameOver);
    }

    public GameData withBlack(String username) {
        return new GameData(gameID,gameName,whiteUsername,username,gameOver);
    }

    public GameData withGameOver(boolean over) {
        return new GameData(gameID,gameName,whiteUsername,blackUsername,over);
    }

}
