package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess=dataAccess;
    }

    // post game
    public CreateGameResult createGame(String authToken, CreateGameRequest request)
            throws DataAccessException, BadRequestException, UnauthorizedException {
        // check auth
        var auth=validateAuth(authToken);
        // check request
        if (request==null||request.game_name()==null||request.game_name().isBlank()) {
            throw new BadRequestException("bad request");
        }
        // create game
        var game=dataAccess.createGame(request.game_name());
        return new CreateGameResult(game.game_ID());
    }

    // get game
    public ListGamesResult listGames(String authToken)
            throws DataAccessException, UnauthorizedException {
        // check auth
        validateAuth(authToken);
        Collection<GameData> games=dataAccess.listGames();
        return new ListGamesResult(games);
    }

    // set game
    public void joinGame(String authToken,JoinGameRequest request)
            throws DataAccessException, BadRequestException, UnauthorizedException {
        // check auth
        var auth=validateAuth(authToken);
        // check request
        if (request==null||request.gameID()==null||request.playerColor()==null||request.playerColor().isBlank()) {
            throw new BadRequestException("bad request");
        }

        var game=dataAccess.getGame(request.gameID());
        if (game==null) {
            throw new BadRequestException("bad request");
        }

        // check color
        String color=request.playerColor().toUpperCase();
        if (!color.equals("WHITE")&&!color.equals("BLACK")) {
            throw new BadRequestException("bad request");
        }

        // check if seat taken
        if (color.equals("WHITE")) {
            if (game.white_username()!=null) {
                throw new AlreadyTakenException("already taken");
            }
            dataAccess.updateGame(game.withWhite(auth.username()));
        } else {
            if (game.black_username()!=null) {
                throw new AlreadyTakenException("already taken");
            }
            dataAccess.updateGame(game.withBlack(auth.username()));
        }
    }

    // helper
    private AuthData validateAuth(String authToken)
            throws DataAccessException, UnauthorizedException {
        if (authToken==null||authToken.isBlank()) {
            throw new UnauthorizedException("unauthorized");
        }
        var auth=dataAccess.getAuth(authToken);
        if (auth==null) {
            throw new UnauthorizedException("unauthorized");
        }
        return auth;
    }
}
