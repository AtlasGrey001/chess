package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.*;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import io.javalin.websocket.WsConfig;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Server {
    private final Javalin javalin;
    private final DataAccess dataAccess;

    public Server() {

        this.dataAccess = new MemoryDataAccess();

        var clearService = new ClearService(dataAccess);
        var userService = new UserService(dataAccess);
        var gameService = new GameService(dataAccess);

        var clearHandler = new ClearHandler(clearService);
        var userHandler = new UserHandler(userService);
        var gameHandler = new GameHandler(gameService);

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinJackson());

            config.router.apiBuilder(() -> {

                // user session
                get("/session", ctx -> userHandler.login(ctx));   // if login is POST, change to post()
                delete("/session", ctx -> userHandler.logout(ctx));

                // game
                post("/game", ctx -> gameHandler.createGame(ctx));
                get("/game", ctx -> gameHandler.listGames(ctx));
                put("/game", ctx -> gameHandler.joinGame(ctx));
                get("/game/{gameID}", ctx -> gameHandler.getGame(ctx));

                // user registration
                post("/user", ctx -> userHandler.register(ctx));

                // clear database
                delete("/db", ctx -> clearHandler.handle(ctx));

                // websocket
                ws("/ws/game/{gameID}", ws -> {
                    ws.onConnect(ctx -> gameHandler.observeGame(ctx));
                    ws.onMessage(ctx -> gameHandler.receiveMessage(ctx));
                    ws.onClose(ctx -> gameHandler.disconnect(ctx));
                });
            });


        });

        // exception handlers
        javalin.exception(service.BadRequestException.class, (e, ctx) -> {
            ctx.status(400).json(new ErrorResponse("Error: " + e.getMessage()));
        });

        javalin.exception(service.UnauthorizedException.class, (e, ctx) -> {
            ctx.status(401).json(new ErrorResponse("Error: " + e.getMessage()));
        });

        javalin.exception(service.AlreadyTakenException.class, (e, ctx) -> {
            ctx.status(403).json(new ErrorResponse("Error: " + e.getMessage()));
        });

        javalin.exception(dataaccess.DataAccessException.class, (e, ctx) -> {
            ctx.status(500).json(new ErrorResponse("Error: " + e.getMessage()));
        });

        javalin.exception(GameNotFoundException.class, (e, ctx) -> {
            ctx.status(404).json(new ErrorResponse("Error: " + e.getMessage()));
        });
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
