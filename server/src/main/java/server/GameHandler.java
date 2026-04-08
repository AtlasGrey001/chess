package server;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.websocket.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import service.GameService;
import service.exceptions.*;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.results.GetGameResult;
import websocket.commands.UserGameCommand;
import websocket.messages.*;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();
    private final ObserverRegistry observers = new ObserverRegistry();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void createGame(Context ctx) {
        try {
            var rawAuth=ctx.header("authorization");
            var auth=rawAuth==null ? null : rawAuth.replace("\"","").trim();
            if (auth==null || auth.isBlank()) {
                ctx.status(401).json(new ErrorResponse("Error Unauthorized"));
                return;
            }
            var body=gson.fromJson(ctx.body(), CreateGameRequest.class);
            if (body==null || body.gameName()==null || body.gameName().isBlank()) {
                ctx.status(400).json(new ErrorResponse("Error Bad Request"));
                return;
            }

            var request=new CreateGameRequest(auth,body.gameName());
            var gameID=gameService.createGame(request);
            GameData gameData=gameService.getGameMetadata(gameID.gameID());
            ctx.status(200).json(gameData);
        } catch (UnauthorizedException e) {
            ctx.status(401).json(new ErrorResponse("Error Unauthorized"));
        } catch (BadRequestException e) {
            ctx.status(400).json(new ErrorResponse("Error Bad Request"));
        } catch (DataAccessException | dataaccess.DataAccessException e) {
            ctx.status(500).json(new ErrorResponse("Error "+e.getMessage()));
        }
    }

    public void listGames(Context ctx) {
        try {
            var rawAuth=ctx.header("authorization");
            var auth = rawAuth == null ? null : rawAuth.replace("\"", "").trim();
            if (auth==null || auth.isBlank()) {
                ctx.status(401).json(new ErrorResponse("Error Unauthorized"));
                return;
            }
            var result=gameService.listGames(auth);
            ctx.status(200).json(result);
        } catch (UnauthorizedException e) {
            ctx.status(401).json(new ErrorResponse("Error Unauthorized"));
        } catch (DataAccessException | dataaccess.DataAccessException e) {
            ctx.status(500).json(new ErrorResponse("Error "+e.getMessage()));
        }
    }

    public void joinGame(Context ctx) {
        try {
            var rawAuth=ctx.header("authorization");
            var auth = rawAuth == null ? null : rawAuth.replace("\"", "").trim();
            if (auth==null || auth.isBlank()) {
                ctx.status(401).json(new ErrorResponse("Error: Unauthorized"));
                return;
            }
            var body=gson.fromJson(ctx.body(), JoinGameRequest.class);
            if (body==null || body.gameID()==null || body.playerColor()==null || body.playerColor().isBlank()) {
                ctx.status(400).json(new ErrorResponse("Error Bad Request"));
                return;
            }

            var request=new JoinGameRequest(auth,body.gameID(),body.playerColor());
            gameService.joinGame(request);
            ctx.status(200).json(new Object());
        } catch (AlreadyTakenException e) {
            ctx.status(403).json(new ErrorResponse("Error Already Taken"));
        } catch (UnauthorizedException e) {
            ctx.status(401).json(new ErrorResponse("Error Unauthorized"));
        } catch (BadRequestException e) {
            ctx.status(400).json(new ErrorResponse("Error Bad Request"));
        } catch (DataAccessException e) {
            ctx.status(500).json(new ErrorResponse("Error "+e.getMessage()));
        } catch (dataaccess.DataAccessException e) {
            ctx.status(500).json(new ErrorResponse("Error "+e.getMessage()));
        }
    }

    public void getGame(Context ctx) {
        try {
            var rawAuth=ctx.header("authorization");
            var auth = rawAuth == null ? null : rawAuth.replace("\"", "").trim();
            if (auth==null || auth.isBlank()) {
                ctx.status(401).json(new ErrorResponse("Error Unauthorized"));
                return;
            }

            int gameID;
            try {
                gameID=Integer.parseInt(ctx.pathParam("gameID"));
            } catch (NumberFormatException e) {
                ctx.status(400).json(new ErrorResponse("Error Bad Request"));
                return;
            }

            if (gameID<=0) {
                ctx.status(400).json(new ErrorResponse("Error Bad Request"));
                return;
            }
            var result=buildGetGameResult(auth,gameID);
            ctx.status(200).json(result);
        } catch (UnauthorizedException e) {
            ctx.status(401).json(new ErrorResponse("Error Unauthorized"));
        } catch (GameNotFoundException e) {
            ctx.status(400).json(new ErrorResponse("Error Game Not Found"));
        } catch (BadRequestException e) {
            ctx.status(400).json(new ErrorResponse("Error Bad Request"));
        } catch (DataAccessException | dataaccess.DataAccessException e) {
            ctx.status(500).json(new ErrorResponse("Error "+e.getMessage()));
        } catch (CloneNotSupportedException e) {
            ctx.status(500).json(new ErrorResponse("Internal Error"));
        }
    }

    public void observeGame(WsConnectContext ctx) {}
    
    public void receiveMessage(WsMessageContext ctx) throws IOException {
        UserGameCommand cmd = gson.fromJson(ctx.message(), UserGameCommand.class);

        if (cmd == null || cmd.getCommandType() == null) {
            sendError(ctx, "Error: invalid command");
            return;
        }

        switch (cmd.getCommandType()) {
            case CONNECT -> handleConnect(ctx, cmd);
            case MAKE_MOVE -> handleMakeMove(ctx, cmd);
            case LEAVE -> handleLeave(ctx, cmd);
            case RESIGN -> handleResign(ctx, cmd);
        }
    }

    public void disconnect(WsCloseContext ctx) {
        observers.remove(ctx.session);
    }
    
    private void handleConnect(WsMessageContext ctx, UserGameCommand cmd) throws IOException {
        try {
            var auth = cmd.getAuthToken();
            var gameID = cmd.getGameID();

            if (auth == null || auth.isBlank()) {
                sendError(ctx, "Error: unauthorized");
                return;
            }

            gameService.validateAuth(auth);
            GameData data = gameService.getGameMetadata(gameID);
            if (data == null) {
                sendError(ctx, "Error: game not found");
                return;
            }

            // Add this session to the game
            observers.add(gameID, ctx.session);
            
            var gameState = buildGetGameResult(data);
            ctx.session.getRemote().sendString(gson.toJson(new LoadGameMessage(gameState)));
            
            String username = gameService.getUsernameFromAuth(auth);
            String role = determineRole(data, username);
            broadcastExcept(ctx.session,gameID,new NotificationMessage(username + " connected as " + role));
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private String determineRole(GameData data, String username) {
        if (username.equals(data.whiteUsername())) return "WHITE";
        if (username.equals(data.blackUsername())) return "BLACK";
        return "OBSERVER";
    }
    
    private void handleMakeMove(WsMessageContext ctx, UserGameCommand cmd) throws IOException {
        try {
            String auth = cmd.getAuthToken();
            int gameID = cmd.getGameID();
            ChessMove move = cmd.getMove();

            if (auth == null || move == null) {
                sendError(ctx, "Error: bad request");
                return;
            }

            gameService.validateAuth(auth);
            String username = gameService.getUsernameFromAuth(auth);
            AuthData nauth = new AuthData(auth, username);

            var updated = gameService.makeMove(nauth, gameID, move);
            
            var gameState = buildGetGameResult(updated);
            broadcast(gameID, new LoadGameMessage(gameState));
            
            broadcastExcept(ctx.session, gameID,
                    new NotificationMessage(username + " made a move"));

            // Check/checkmate notifications
            ChessGame engine = gameService.getEngine(gameID);
            ChessGame.TeamColor turn = engine.getTeamTurn();

            if (engine.isInCheckmate(turn)) {
                broadcast(gameID, new NotificationMessage("Checkmate against " + turn));
            } else if (engine.isInCheck(turn)) {
                broadcast(gameID, new NotificationMessage("Check against " + turn));
            }
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleLeave(WsMessageContext ctx, UserGameCommand cmd) {
        try {
            var auth = cmd.getAuthToken();
            var gameID = cmd.getGameID();
            String username = gameService.getUsernameFromAuth(auth);
            GameData game = gameService.getGameMetadata(gameID);

            GameData updated;
            if (username.equals(game.whiteUsername())) {
                updated = game.withWhite(null);
            } else if (username.equals(game.blackUsername())) {
                updated = game.withBlack(null);
            } else {
                updated = game; // observer, no DB change
            }
            gameService.dataAccess.updateGame(updated);

            observers.remove(ctx.session);
            ctx.session.close();
            broadcast(gameID, new NotificationMessage(username + " left the game"));
        } catch (Exception ignored) {}
    }

    private void handleResign(WsMessageContext ctx, UserGameCommand cmd) throws IOException {
        try {
            String auth = cmd.getAuthToken();
            var gameID = cmd.getGameID();
            String username = gameService.getUsernameFromAuth(auth);
            AuthData nauth = new AuthData(auth, username);
            gameService.resign(nauth, gameID);
            broadcast(gameID, new NotificationMessage(username + " resigned"));
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void sendError(WsMessageContext ctx, String msg) throws IOException {
        //ctx.send(gson.toJson(new ErrorMessage(msg)));
        ctx.session.getRemote().sendString(gson.toJson(new ErrorMessage(msg)));
    }

    private void broadcast(int gameID, ServerMessage msg) {
        String json = gson.toJson(msg);
        for (Session s : observers.get(gameID)) {
            try { s.getRemote().sendString(json); } catch (Exception ignored) {}
        }
    }

    private void broadcastExcept(Session except, int gameID, ServerMessage msg) {
        String json = gson.toJson(msg);
        for (Session s : observers.get(gameID)) {
            if (s != except) {
                try { s.getRemote().sendString(json); } catch (Exception ignored) {}
            }
        }
    }

    public static class ObserverRegistry {
        private final Map<Integer, Set<Session>> map = new ConcurrentHashMap<>();
        public void add(int gameID, Session s) {
            map.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(s);
        }

        public void remove(Session s) {
            map.values().forEach(set -> set.remove(s));
        }

        public Set<Session> get(int gameID) {
            return map.getOrDefault(gameID, Set.of());
        }
    }

    private GetGameResult buildGetGameResult(String auth, int gameID)
            throws UnauthorizedException, GameNotFoundException, DataAccessException, CloneNotSupportedException, dataaccess.DataAccessException {
        // Validate auth
        gameService.validateAuth(auth);

        // Get metadata
        GameData data=gameService.getGameMetadata(gameID);
        if (data==null) {throw new GameNotFoundException("Error Game Not Found");}

        // Get engine
        ChessGame game=gameService.getEngine(gameID);
        if (game==null) {throw new GameNotFoundException("Error Game Not Found");}
        return new GetGameResult(data.gameID(),data.whiteUsername(),
                data.blackUsername(),data.gameName(),game.getBoard().copyWithoutGame(),game.getTeamTurn());
    }

    private GetGameResult buildGetGameResult(GameData data)
            throws GameNotFoundException, DataAccessException, dataaccess.DataAccessException {

        int gameID = data.gameID();
        ChessGame engine = gameService.getEngine(gameID);

        return new GetGameResult(
                data.gameID(),
                data.whiteUsername(),
                data.blackUsername(),
                data.gameName(),
                engine.getBoard().copyWithoutGame(),
                engine.getTeamTurn()
        );
    }
}
