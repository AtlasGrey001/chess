package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {

    private DataAccess dao;
    private UserService userService;
    private GameService gameService;

    @BeforeEach
    void setup() {
        dao=new MemoryDataAccess();
        userService=new UserService(dao);
        gameService=new GameService(dao);
    }

    @Test
    void createGamePositive() throws Exception {
        var reg=userService.register(new RegisterRequest("cameron","pass","e"));
        var res=gameService.createGame(new CreateGameRequest(reg.authToken(),"MyGame"));

        assertTrue(res.game_ID() > 0);
        assertNotNull(dao.getGame(res.game_ID()));
    }

    @Test
    void createGameNegative_badRequest() throws Exception {
        var reg=userService.register(new RegisterRequest("cameron","pass","e"));

        assertThrows(BadRequestException.class, () -> gameService.createGame(new CreateGameRequest(reg.authToken(),null)));
    }

    @Test
    void listGamesPositive() throws Exception {
        var reg=userService.register(new RegisterRequest("cameron","pass","e"));
        gameService.createGame(new CreateGameRequest(reg.authToken(),"A"));
        var res=gameService.listGames(reg.authToken());

        assertEquals(1,res.games().size());
    }

    @Test
    void listGamesNegative_unauthorized() {
        assertThrows(UnauthorizedException.class, () -> gameService.listGames("fake"));
    }

    @Test
    void joinGamePositive() throws Exception {
        var reg=userService.register(new RegisterRequest("cameron","pass","e"));
        var game=gameService.createGame(new CreateGameRequest(reg.authToken(),"A"));
        gameService.joinGame(new JoinGameRequest(reg.authToken(),"WHITE",game.game_ID()));

        assertEquals("cameron",dao.getGame(game.game_ID()).white_username());
    }

    @Test
    void joinGameNegative_colorTaken() throws Exception {
        var reg1=userService.register(new RegisterRequest("c1","p","e"));
        var reg2=userService.register(new RegisterRequest("c2","p","e"));
        var game=gameService.createGame(new CreateGameRequest(reg1.authToken(),"A"));
        gameService.joinGame(new JoinGameRequest(reg1.authToken(),"WHITE",game.game_ID()));

        assertThrows(AlreadyTakenException.class, () -> gameService.joinGame(new JoinGameRequest(reg2.authToken(), "WHITE", game.game_ID())));
    }
}
