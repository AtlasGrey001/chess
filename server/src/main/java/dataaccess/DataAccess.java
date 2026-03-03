package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface DataAccess {
    Map<Integer, ChessGame> ENGINES = new HashMap<>();

    // Clear
    void clear() throws DataAccessException;

    // Users
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    // Auth
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;

    // Games
    GameData createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    default ChessGame getEngine(int gameID) throws DataAccessException {return ENGINES.get(gameID);}
    void updateEngine(int gameID, ChessGame engine) throws DataAccessException;
}
