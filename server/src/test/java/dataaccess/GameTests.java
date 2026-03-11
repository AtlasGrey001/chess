package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameTests {
    private DataAccess dao;

    @BeforeEach
    void setup() throws Exception {
        dao = new MySQLDataAccess();
        dao.clear();
    }

    @Test
    void createGamePositive() throws Exception {
        GameData game = dao.createGame("A");

        assertNotNull(game);
        assertTrue(game.gameID() > 0);
        assertEquals("A", game.gameName());
        assertNotNull(dao.getGame(game.gameID()));
    }

    @Test
    void createGameNegativeNullName() {
        assertThrows(DataAccessException.class,
                () -> dao.createGame(null));
    }

    @Test
    void createGameNegativeEmptyNameAllowed() throws Exception {
        GameData game = dao.createGame("");
        assertNotNull(game);
        assertEquals("", game.gameName());
    }

    @Test
    void getGamePositive() throws Exception {
        GameData created = dao.createGame("A");
        GameData found = dao.getGame(created.gameID());

        assertEquals(created.gameID(), found.gameID());
        assertEquals("A", found.gameName());
    }

    @Test
    void getGameNegativeNotFound() throws Exception {
        assertNull(dao.getGame(9999));
    }

    @Test
    void listGamesPositive() throws Exception {
        dao.createGame("A");
        dao.createGame("B");

        var games = dao.listGames();
        assertEquals(2, games.size());
    }

    @Test
    void listGamesNegativeEmpty() throws Exception {
        assertTrue(dao.listGames().isEmpty());
    }

    @Test
    void listGamesNegativeAfterClear() throws Exception {
        dao.createGame("A");
        dao.createGame("B");
        dao.clear();

        assertTrue(dao.listGames().isEmpty());
    }


    @Test
    void updateGamePositive() throws Exception {
        dao.createUser(new UserData("cameron", "p", "e"));

        GameData created = dao.createGame("A");
        GameData updated = new GameData(
                created.gameID(),
                "A",
                "cameron",
                null,
                false
        );

        dao.updateGame(updated);

        assertEquals("cameron", dao.getGame(created.gameID()).whiteUsername());
    }

    @Test
    void updateGameNegativeNotFound() {
        GameData fake = new GameData(
                9999,
                "A",
                "cameron",
                null,
                false
        );

        assertThrows(DataAccessException.class,
                () -> dao.updateGame(fake));
    }

    @Test
    void updateGameNegativeNullName() throws Exception {
        GameData created = dao.createGame("A");
        GameData bad = new GameData(
                created.gameID(),
                null,
                null,
                null,
                false
        );

        assertThrows(DataAccessException.class,
                () -> dao.updateGame(bad));
    }


    @Test
    void updateEnginePositive() throws Exception {
        GameData created = dao.createGame("A");

        ChessGame engine = new ChessGame();
        dao.updateEngine(created.gameID(), engine);

        assertEquals(engine, dao.getEngine(created.gameID()));
    }

    @Test
    void updateEngineNegativeNotFound() {
        ChessGame engine = new ChessGame();

        assertThrows(DataAccessException.class,
                () -> dao.updateEngine(9999, engine));
    }

    @Test
    void getEnginePositive() throws Exception {
        GameData game = dao.createGame("A");
        ChessGame engine = dao.getEngine(game.gameID());

        assertNotNull(engine);
        assertEquals(new ChessGame(), engine);
    }

    @Test
    void getEngineNegativeNotFound() {
        assertThrows(DataAccessException.class,
                () -> dao.getEngine(9999));
    }

    @Test
    void clearPositive() throws Exception {
        dao.createGame("A");
        dao.clear();

        assertTrue(dao.listGames().isEmpty());
    }

    @Test
    void clearNegativeDoubleClear() throws Exception {
        dao.clear();
        dao.clear();
    }
}
