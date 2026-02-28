package model;

public record GameData(
        Integer game_ID,
        String game_name,
        String white_username,
        String black_username
) {
    public GameData withWhite(String username) {
        return new GameData(game_ID,game_name,username,black_username);
    }

    public GameData withBlack(String username) {
        return new GameData(game_ID,game_name,white_username,username);
    }
}
