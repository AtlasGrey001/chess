package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private final Map<String,UserData> users=new HashMap<>();
    private final Map<String,AuthData> authTokens=new HashMap<>();
    private final Map<Integer,GameData> games=new HashMap<>();
    private int nextGameID=1;

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
        nextGameID=1;
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {throw new DataAccessException("User already exists");}
        users.put(user.username(),user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createAuth(AuthData auth) {
        authTokens.put(auth.authToken(),auth);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        int id=nextGameID++;
        GameData game=new GameData(id,gameName,null,null);
        games.put(id,game);
        return game;
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.game_ID())) {throw new DataAccessException("Game does not exist");}
        games.put(game.game_ID(),game);
    }
}
