package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.*;
import io.javalin.*;
import io.javalin.json.JavalinJackson;

public class Server {
    private final Javalin javalin;
    private final DataAccess dataAccess;

    public Server() {
        javalin=Javalin.create(config->{ config.staticFiles.add("web");
            config.jsonMapper(new JavalinJackson());});
        this.dataAccess=new MemoryDataAccess();
        var clearService=new ClearService(dataAccess);
        var userService=new UserService(dataAccess);
        var clearHandler=new ClearHandler(clearService);
        var userHandler=new UserHandler(userService);
        var gameService=new GameService(dataAccess);
        var gameHandler=new GameHandler(gameService);

        // Register your endpoints and exception handlers here.
        // user session
        javalin.post("/session",ctx->userHandler.login(ctx));
        javalin.delete("/session",ctx->userHandler.logout(ctx));

        // game
        javalin.post("/game",ctx->gameHandler.createGame(ctx));
        javalin.get("/game",ctx->gameHandler.listGames(ctx));
        javalin.put("/game",ctx->gameHandler.joinGame(ctx));
        javalin.get("/game/{gameID}",ctx->gameHandler.getGame(ctx));

        // other
        javalin.ws("/ws/game/{gameID}",ws->{
            ws.onConnect(ctx->gameHandler.observeGame(ctx));
            ws.onMessage(ctx->gameHandler.receiveMessage(ctx));
            ws.onClose(ctx->gameHandler.disconnect(ctx));});

        javalin.delete("/db",ctx->{clearHandler.handle(ctx);});
        javalin.post("/user",ctx->{userHandler.register(ctx);});

        // exceptions
        javalin.exception(service.BadRequestException.class,(e,ctx)->{ctx.status(400);
            ctx.json(new ErrorResponse("Error: "+e.getMessage()));});
        javalin.exception(service.UnauthorizedException.class,(e,ctx)->{ctx.status(401);
            ctx.json(new ErrorResponse("Error: "+e.getMessage()));});
        javalin.exception(service.AlreadyTakenException.class,(e,ctx)->{ctx.status(403);
            ctx.json(new ErrorResponse("Error: "+e.getMessage()));});
        javalin.exception(dataaccess.DataAccessException.class,(e,ctx)->{ctx.status(500);
            ctx.json(new ErrorResponse("Error: "+e.getMessage()));});
        javalin.exception(GameNotFoundException.class,(e,ctx)->{ctx.status(404);
            ctx.json(new ErrorResponse("Error: "+e.getMessage()));});
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private record ErrorResponse(String message) {}
}
