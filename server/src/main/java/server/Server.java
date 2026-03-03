package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.*;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

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
        });

        // -------------------------
        // ROUTES (Javalin 6 syntax)
        // -------------------------

        // user session
        javalin.post("/session", userHandler::login);
        javalin.delete("/session", userHandler::logout);

        // user registration
        javalin.post("/user", userHandler::register);

        // game endpoints
        javalin.post("/game", gameHandler::createGame);
        javalin.get("/game", gameHandler::listGames);
        javalin.put("/game", gameHandler::joinGame);
        javalin.get("/game/{gameID}", gameHandler::getGame);

        // clear database
        javalin.delete("/db", clearHandler::handle);

        // websocket
        javalin.ws("/ws/game/{gameID}", ws -> {
            ws.onConnect(gameHandler::observeGame);
            ws.onMessage(gameHandler::receiveMessage);
            ws.onClose(gameHandler::disconnect);
        });

        // -------------------------
        // EXCEPTIONS (Javalin 6)
        // -------------------------
        javalin.exception(BadRequestException.class, (e, ctx) -> {
            ctx.status(400).json(new ErrorResponse("Error: " + e.getMessage()));
        });
        javalin.exception(UnauthorizedException.class, (e, ctx) -> {
            ctx.status(401).json(new ErrorResponse("Error: " + e.getMessage()));
        });
        javalin.exception(AlreadyTakenException.class, (e, ctx) -> {
            ctx.status(403).json(new ErrorResponse("Error: " + e.getMessage()));
        });
        javalin.exception(DataAccessException.class, (e, ctx) -> {
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
