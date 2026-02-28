package server;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.*;
import dataaccess.DataAccessException;

public class GameHandler {

    private final GameService gameService;
    private final Gson gson=new Gson();

    public GameHandler(GameService gameService) {
        this.gameService=gameService;
    }

    public void createGame(Context ctx) throws DataAccessException, BadRequestException, UnauthorizedException {
        var auth=ctx.header("authorization");
        var request=gson.fromJson(ctx.body(),CreateGameRequest.class);
        var result=gameService.createGame(auth,request);
        ctx.json(result);
    }

    public void listGames(Context ctx) throws DataAccessException, UnauthorizedException {
        var auth=ctx.header("authorization");
        var result=gameService.listGames(auth);
        ctx.json(result);
    }

    public void joinGame(Context ctx) throws DataAccessException, BadRequestException, UnauthorizedException {
        var auth=ctx.header("authorization");
        var request = gson.fromJson(ctx.body(), JoinGameRequest.class);
        gameService.joinGame(auth, request);
        ctx.result("{}");
    }
}

