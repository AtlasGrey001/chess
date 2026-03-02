package server;

import chess.ChessGame;
import com.google.gson.Gson;
import io.javalin.http.Context;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import service.*;
import dataaccess.DataAccessException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson=new Gson();
    private final ObserverRegistry observerRegistry=new ObserverRegistry();

    public GameHandler(GameService gameService) {
        this.gameService=gameService;
    }

    public void createGame(Context ctx)
            throws DataAccessException, BadRequestException, UnauthorizedException {
        var auth=ctx.header("authorization");
        var body=gson.fromJson(ctx.body(),CreateGameRequest.class);
        if (body==null||body.gameName()==null||body.gameName().isBlank()) throw new BadRequestException("Bad Request");
        var request=new CreateGameRequest(auth,body.gameName());
        var result=gameService.createGame(request);
        ctx.status(200);
        ctx.json(result);
    }

    public void listGames(Context ctx)
            throws DataAccessException, UnauthorizedException {
        var auth=ctx.header("authorization");
        var result=gameService.listGames(auth);
        ctx.status(200);
        ctx.json(result);
    }

    public void joinGame(Context ctx)
            throws DataAccessException, BadRequestException, UnauthorizedException, AlreadyTakenException {
        var auth=ctx.header("authorization");
        var body=gson.fromJson(ctx.body(),JoinGameRequest.class);
        var request=new JoinGameRequest(auth,body.gameID(),body.playerColor());
        gameService.joinGame(request);
        ctx.status(200);
        ctx.result("{}");
    }

    public void getGame(Context ctx)
            throws DataAccessException, UnauthorizedException, BadRequestException, GameNotFoundException, CloneNotSupportedException {
        var auth=ctx.header("authorization");
        int gameID;
        try {gameID = Integer.parseInt(ctx.pathParam("gameID"));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Bad Request");}
        if (gameID<=0) throw new BadRequestException("Bad Request");
        var result=buildGetGameResult(auth,gameID);
        ctx.status(200);
        ctx.json(result);
    }

    public void observeGame(WsConnectContext ctx) {
        try {
            String token=ctx.queryParam("token");
            if (token==null||token.isBlank()) {
                ctx.session.close(1008,"Unauthorized");
                return;
            }
            var auth=gameService.validateAuth(token);
            int gameID;
            try {gameID = Integer.parseInt(ctx.pathParam("gameID"));
            } catch (NumberFormatException e) {
                ctx.session.close(1008,"Bad Request");
                return;
            }
            if (gameID<=0) {
                ctx.session.close(1008,"Bad Request");
                return;
            }
            var data=gameService.getGameMetadata(gameID);
            if (data==null) {
                ctx.session.close(1008,"Game Not Found");
                return;
            }
            observerRegistry.add(gameID,ctx.session);
            ctx.send(gson.toJson(buildGetGameResult(data)));
        } catch (UnauthorizedException e) {ctx.session.close(1008, "Unauthorized");
        } catch (Exception e) {ctx.session.close(1011, "Internal Error");}
    }

    public void disconnect(WsCloseContext ctx) {
        observerRegistry.remove(ctx.session);
    }

    public void receiveMessage(WsMessageContext ctx) {
        try {
            var msg=gson.fromJson(ctx.message(),WSMessage.class);
            if (msg==null||msg.type()==null) {
                ctx.session.close(1008,"Bad Request");
                return;
            }
            switch (msg.type()) {
                case "makeMove"->handleMakeMove(ctx, msg);
                case "resign"->handleResign(ctx,msg);
                case "leave"->handleLeave(ctx,msg);
                default->ctx.session.close(1008,"Bad Request");
            }
        } catch (Exception e) {ctx.session.close(1011, "Internal Error");}
    }

    private void handleMakeMove(WsMessageContext ctx,WSMessage msg) {
        try {
            String token=ctx.queryParam("token");
            if (token==null||token.isBlank()) {
                ctx.session.close(1008,"Unauthorized");
                return;
            }
            var auth=gameService.validateAuth(token);
            int gameID=Integer.parseInt(ctx.pathParam("gameID"));
            var game=gameService.getGameRaw(gameID);
            if (game==null) {
                ctx.session.close(1008,"Game Not Found");
                return;
            }
            if (msg.move()==null) {
                ctx.session.close(1008,"Bad Request");
                return;
            }
            var updated=gameService.makeMove(auth,gameID,msg.move());
            broadcast(gameID,buildGetGameResult(updated));
        } catch (UnauthorizedException e) {ctx.session.close(1008, "Unauthorized");
        } catch (BadRequestException e) {ctx.session.close(1008, "Bad Request");
        } catch (Exception e) {ctx.session.close(1011, "Internal Error");}
    }

    private void handleResign(WsMessageContext ctx,WSMessage msg) {
        try {
            String token=ctx.queryParam("token");
            var auth=gameService.validateAuth(token);
            int gameID=Integer.parseInt(ctx.pathParam("gameID"));
            var game=gameService.getGameRaw(gameID);
            if (game==null) {
                ctx.session.close(1008, "Game Not Found");
                return;
            }
            var updated=gameService.resign(auth,gameID);
            broadcast(gameID, buildGetGameResult(updated));
        } catch (UnauthorizedException e) {ctx.session.close(1008, "Unauthorized");
        } catch (Exception e) {ctx.session.close(1011, "Internal Error");}
    }

    private void handleLeave(WsMessageContext ctx,WSMessage msg) {
        observerRegistry.remove(ctx.session);
        ctx.session.close(1000,"left");
    }

    private void broadcast(int gameID,Object message) {
        var json=gson.toJson(message);
        for (Session s:observerRegistry.get(gameID)) {
            try {s.getRemote().sendString(json);
            } catch (Exception ignored) {}
        }
    }

    public static class ObserverRegistry {
        private final Map<Integer, Set<Session>> observers=new ConcurrentHashMap<>();

        public void add(int gameID,Session session) {observers.computeIfAbsent(gameID,k->ConcurrentHashMap.newKeySet()).add(session);}

        public void remove(Session session) {
            observers.values().forEach(set->set.remove(session));
        }

        public Set<Session> get(int gameID) {
            return observers.getOrDefault(gameID,Set.of());
        }
    }

    private GetGameResult buildGetGameResult(String auth,int gameID)
            throws UnauthorizedException, GameNotFoundException, DataAccessException, CloneNotSupportedException {
        // Validate auth
        gameService.validateAuth(auth);

        // Get metadata
        GameData data=gameService.getGameMetadata(gameID);
        if (data==null) throw new GameNotFoundException("Game Not Found");

        // Get engine
        ChessGame game=gameService.getEngine(gameID);
        if (game==null) throw new GameNotFoundException("Game Not Found");
        return new GetGameResult(data.gameID(),data.whiteUsername(),data.blackUsername(),data.gameName(),game.getBoard().copyWithoutGame(),game.getTeamTurn());
    }

    private GetGameResult buildGetGameResult(GameData data)
            throws GameNotFoundException, CloneNotSupportedException, DataAccessException {
        int gameID=data.gameID();

        // Get engine
        ChessGame game=gameService.getEngine(gameID);
        if (game==null) throw new GameNotFoundException("Game Not Found");
        return new GetGameResult(data.gameID(),data.whiteUsername(),data.blackUsername(),data.gameName(),game.getBoard().copyWithoutGame(),game.getTeamTurn());
    }
}
