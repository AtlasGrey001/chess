package client;

import org.junit.jupiter.api.*;
import server.Server;
import model.*;
import java.util.List;

public class ServerFacadeTests {
    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade(port);
    }

    @BeforeEach
    public void clearDB() throws Exception {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    void registerPositive() throws Exception {
        var auth = facade.register("u1","p1","e1");
        Assertions.assertNotNull(auth);
        Assertions.assertTrue(auth.authToken().length()>5);
    }

    @Test
    void registerDuplicateNegative() throws Exception {
        facade.register("u1","p1","e1");
        Assertions.assertThrows(Exception.class,()->facade.register("u1","p1","e1"));
    }

    @Test
    void loginPositive() throws Exception {
        facade.register("u1","p1","e1");
        var auth = facade.login("u1","p1");
        Assertions.assertNotNull(auth);
    }

    @Test
    void loginWrongPasswordNegative() throws Exception {
        facade.register("u1","p1","e1");
        Assertions.assertThrows(Exception.class,()->facade.login("u1","wrong"));
    }

    @Test
    void loginNonexistentUserNegative() {
        Assertions.assertThrows(Exception.class,()->facade.login("ghost","p"));
    }

    @Test
    void logoutPositive() throws Exception {
        var auth = facade.register("u1","p1","e1");
        Assertions.assertDoesNotThrow(()->facade.logout(auth.authToken()));
    }

    @Test
    void logoutInvalidTokenNegative() {
        Assertions.assertThrows(Exception.class,()->facade.logout("bad-token"));
    }

    @Test
    void listGamesEmptyPositive() throws Exception {
        var auth = facade.register("u1","p1","e1");
        List<GameData> games = facade.listGames(auth.authToken());
        Assertions.assertEquals(0,games.size());
    }

    @Test
    void listGamesInvalidTokenNegative() {
        Assertions.assertThrows(Exception.class,()->facade.listGames("bad-token"));
    }

    @Test
    void createGamePositive() throws Exception {
        var auth = facade.register("u1","p1","e1");
        var game = facade.createGame(auth.authToken(),"MyGame");
        Assertions.assertNotNull(game);
        Assertions.assertEquals("MyGame",game.gameName());
    }

    @Test
    void createGameInvalidTokenNegative() {
        Assertions.assertThrows(Exception.class,()->facade.createGame("bad-token","Game"));
    }

    @Test
    void joinGamePositive() throws Exception {
        var auth = facade.register("u1","p1","e1");
        var game = facade.createGame(auth.authToken(),"G1");
        Assertions.assertDoesNotThrow(()->
                facade.joinGame(auth.authToken(),game.gameID(),"WHITE"));
    }

    @Test
    void joinGameInvalidColorNegative() throws Exception {
        var auth = facade.register("u1","p1","e1");
        var game = facade.createGame(auth.authToken(),"G1");
        Assertions.assertThrows(Exception.class,()->
                facade.joinGame(auth.authToken(),game.gameID(),"PURPLE"));
    }

    @Test
    void joinGameNonexistentIdNegative() throws Exception {
        var auth = facade.register("u1","p1","e1");
        Assertions.assertThrows(Exception.class,()->
                facade.joinGame(auth.authToken(),9999,"WHITE"));
    }
}
