package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.*;
import io.javalin.*;

public class Server {
    private final Javalin javalin;
    private final DataAccess dataAccess;

    public Server() {
        javalin=Javalin.create(config->config.staticFiles.add("web"));
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

        // other
        javalin.delete("/db",ctx->{clearHandler.handle(ctx);});
        javalin.post("/user",ctx->{userHandler.register(ctx);});

        // exceptions
        javalin.exception(service.BadRequestException.class,(e,ctx)->{ctx.status(400);ctx.json(new ErrorResponse(e.getMessage()));});
        javalin.exception(service.UnauthorizedException.class,(e,ctx)->{ctx.status(401);ctx.json(new ErrorResponse(e.getMessage()));});
        javalin.exception(service.AlreadyTakenException.class,(e,ctx)->{ctx.status(403);ctx.json(new ErrorResponse(e.getMessage()));});
        javalin.exception(dataaccess.DataAccessException.class,(e,ctx)->{ctx.status(500);ctx.json(new ErrorResponse(e.getMessage()));});
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private record ErrorResponse(String txt) {}
}
