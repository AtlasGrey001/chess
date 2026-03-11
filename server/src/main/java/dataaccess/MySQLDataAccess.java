package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MySQLDataAccess implements DataAccess {
    private final Gson gson=new Gson();

    @Override
    public void clear() throws DataAccessException {
        try (var conn=DatabaseManager.getConnection();
             var stmt=conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM auth_tokens");
            stmt.executeUpdate("DELETE FROM games");
            stmt.executeUpdate("DELETE FROM users");
        } catch (SQLException ex) {
            throw new DataAccessException("Error clearing database",ex);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        var sql="INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
        try (var conn=DatabaseManager.getConnection();
             var ps=conn.prepareStatement(sql)) {
            ps.setString(1,user.username());
            ps.setString(2,user.password());
            ps.setString(3,user.email());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error creating user",ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var sql="SELECT username, password_hash, email FROM users WHERE username = ?";
        try (var conn=DatabaseManager.getConnection();
             var ps=conn.prepareStatement(sql)) {
            ps.setString(1,username);
            try (var rs=ps.executeQuery()) {
                if (!rs.next()) {return null;}
                return new UserData(
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("email")
                );
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error getting user",ex);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        var sql="INSERT INTO auth_tokens (auth_token, username) VALUES (?, ?)";
        try (var conn=DatabaseManager.getConnection();
             var ps=conn.prepareStatement(sql)) {
            ps.setString(1,auth.authToken());
            ps.setString(2,auth.username());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error creating auth token",ex);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        var sql="SELECT auth_token, username FROM auth_tokens WHERE auth_token = ?";
        try (var conn=DatabaseManager.getConnection();
             var ps=conn.prepareStatement(sql)) {
            ps.setString(1,authToken);
            try (var rs=ps.executeQuery()) {
                if (!rs.next()) {return null;}
                return new AuthData(
                        rs.getString("auth_token"),
                        rs.getString("username")
                );
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error getting auth token",ex);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var sql="DELETE FROM auth_tokens WHERE auth_token = ?";
        try (var conn=DatabaseManager.getConnection();
             var ps=conn.prepareStatement(sql)) {
            ps.setString(1,authToken);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error deleting auth token",ex);
        }
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        var sql="INSERT INTO games (game_name, game_state) VALUES (?, ?)";
        ChessGame engine=new ChessGame();
        String json=gson.toJson(engine);

        try (var conn=DatabaseManager.getConnection();
             var ps=conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,gameName);
            ps.setString(2,json);
            ps.executeUpdate();

            try (var rs=ps.getGeneratedKeys()) {
                if (!rs.next()) {throw new DataAccessException("Failed to get game ID");}
                int id=rs.getInt(1);
                return new GameData(id,gameName,null,null,false);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error creating game",ex);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var sql="""
            SELECT game_id, game_name, white_username, black_username, game_over
            FROM games
            WHERE game_id = ?
        """;
        try (var conn=DatabaseManager.getConnection();
             var ps=conn.prepareStatement(sql)) {
            ps.setInt(1,gameID);
            try (var rs=ps.executeQuery()) {
                if (!rs.next()) {return null;}
                return new GameData(
                        rs.getInt("game_id"),
                        rs.getString("game_name"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        rs.getBoolean("game_over")
                );
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error getting game",ex);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var sql="""
            SELECT game_id, game_name, white_username, black_username, game_over
            FROM games
        """;
        List<GameData> games=new ArrayList<>();
        try (var conn=DatabaseManager.getConnection();
             var ps=conn.prepareStatement(sql);
             var rs=ps.executeQuery()) {
            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("game_id"),
                        rs.getString("game_name"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        rs.getBoolean("game_over")
                ));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error listing games",ex);
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var sql="""
            UPDATE games
            SET game_name = ?, white_username = ?, black_username = ?, game_over = ?
            WHERE game_id = ?
        """;
        try (var conn=DatabaseManager.getConnection();
             var ps=conn.prepareStatement(sql)) {
            ps.setString(1,game.gameName());
            ps.setString(2,game.whiteUsername());
            ps.setString(3,game.blackUsername());
            ps.setBoolean(4,game.gameOver());
            ps.setInt(5,game.gameID());
            if (ps.executeUpdate()==0) {
                throw new DataAccessException("Game does not exist");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error updating game",ex);
        }
    }

    @Override
    public ChessGame getEngine(int gameID) throws DataAccessException {
        var sql="SELECT game_state FROM games WHERE game_id = ?";
        try (var conn=DatabaseManager.getConnection();
             var ps=conn.prepareStatement(sql)) {
            ps.setInt(1,gameID);
            try (var rs=ps.executeQuery()) {
                if (!rs.next()) {throw new DataAccessException("Engine does not exist");}
                return gson.fromJson(rs.getString("game_state"),ChessGame.class);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error getting engine",ex);
        }
    }

    @Override
    public void updateEngine(int gameID,ChessGame engine) throws DataAccessException {
        var sql="UPDATE games SET game_state = ? WHERE game_id = ?";
        String json=gson.toJson(engine);

        try (var conn=DatabaseManager.getConnection();
             var ps=conn.prepareStatement(sql)) {
            ps.setString(1,json);
            ps.setInt(2,gameID);
            if (ps.executeUpdate()==0) {
                throw new DataAccessException("Engine does not exist");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error updating engine", ex);
        }
    }
}
