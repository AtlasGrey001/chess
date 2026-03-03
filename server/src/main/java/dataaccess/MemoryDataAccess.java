package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private final Map<String,UserData> users=new HashMap<>();
    private final Map<String,AuthData> authTokens=new HashMap<>();
    private final Map<Integer,GameData> games=new HashMap<>();
    private final Map<Integer, ChessGame> engines=new HashMap<>();
    private int nextGameID=1;
    private final Gson gson=new Gson();

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
        engines.clear();
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
    public GameData createGame(String gameName) {
        int id=nextGameID++;
        GameData data=new GameData(id,gameName,null,null,false);
        games.put(id, data);
        engines.put(id,new ChessGame());
        return data;
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
        if (!games.containsKey(game.gameID())) {throw new DataAccessException("Game does not exist");}
        games.put(game.gameID(),game);
    }

    @Override
    public void updateEngine(int gameID,ChessGame engine) throws DataAccessException {
        try {
            var path=Path.of("engines",gameID+".json");
            Files.writeString(path,gson.toJson(engine));
        } catch (IOException ex) {throw new DataAccessException("Unable to write engine file",ex);}
    }

    @Override
    public ChessGame getEngine(int gameID) throws DataAccessException {
        ChessGame engine=engines.get(gameID);
        if (engine==null) {throw new DataAccessException("Engine does not exist for game "+gameID);}
        return engine;
    }
}
