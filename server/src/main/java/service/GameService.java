package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {this.dataAccess=dataAccess;}

    public CreateGameResult createGame(CreateGameRequest request)
            throws DataAccessException, BadRequestException, UnauthorizedException {
        var auth=validateAuth(request.authToken());
        if (request.gameName()==null||request.gameName().isBlank()) throw new BadRequestException("Bad Request");
        var game=dataAccess.createGame(request.gameName());
        return new CreateGameResult(game.gameID());
    }

    public ListGamesResult listGames(String authToken)
            throws DataAccessException, UnauthorizedException {
        validateAuth(authToken);
        Collection<GameData> games=dataAccess.listGames();
        return new ListGamesResult(games);
    }

    public void joinGame(JoinGameRequest request)
            throws DataAccessException, BadRequestException, UnauthorizedException {
        var auth=validateAuth(request.authToken());
        if (request.gameID()==null||request.playerColor()==null||request.playerColor().isBlank()) throw new BadRequestException("Bad Request");
        var game = dataAccess.getGame(request.gameID());
        if (game==null) throw new BadRequestException("Bad Request");
        var color=request.playerColor().toUpperCase();
        if (!color.equals("WHITE")&&!color.equals("BLACK")) throw new BadRequestException("Bad Request");
        if (color.equals("WHITE")) {
            if (game.whiteUsername()!=null) throw new AlreadyTakenException("Already Taken");
            dataAccess.updateGame(game.withWhite(auth.username()));
        } else {
            if (game.blackUsername()!=null) throw new AlreadyTakenException("Already Taken");
            dataAccess.updateGame(game.withBlack(auth.username()));
        }
    }

    public AuthData validateAuth(String authToken)
            throws DataAccessException, UnauthorizedException {
        if (authToken==null||authToken.isBlank()) throw new UnauthorizedException("Unauthorized");
        var auth=dataAccess.getAuth(authToken);
        if (auth==null) throw new UnauthorizedException("Unauthorized");
        return auth;
    }

    public GameData getGameRaw(int gameID) throws DataAccessException {return dataAccess.getGame(gameID);}

    public GameData makeMove(AuthData auth, int gameID, ChessMove move)
            throws DataAccessException, BadRequestException, InvalidMoveException {
        var game=dataAccess.getGame(gameID);
        if (game==null) throw new BadRequestException("Bad Request");
        if (game.gameOver()) throw new BadRequestException("Game Already Over");
        var chessGame=dataAccess.getEngine(gameID);

        var turn=chessGame.getTeamTurn();
        boolean whiteTurn=(turn==ChessGame.TeamColor.WHITE);
        if (whiteTurn&&!auth.username().equals(game.whiteUsername())) throw new BadRequestException("Not Your Turn");
        if (!whiteTurn&&!auth.username().equals(game.blackUsername())) throw new BadRequestException("Not Your Turn");

        var legalMoves=chessGame.validMoves(move.getStartPosition());
        if (legalMoves==null||!legalMoves.contains(move)) throw new BadRequestException("Illegal Move");
        chessGame.makeMove(move);
        dataAccess.updateEngine(gameID,chessGame);
        return game;
    }

    public GameData resign(AuthData auth,int gameID)
            throws DataAccessException, BadRequestException {
        var game=dataAccess.getGame(gameID);
        if (game==null) throw new BadRequestException("Bad Request");
        boolean isWhite=auth.username().equals(game.whiteUsername());
        boolean isBlack=auth.username().equals(game.blackUsername());

        if (!isWhite&&!isBlack) throw new BadRequestException("Not A Player");
        if (game.gameOver()) throw new BadRequestException("Game Already Over");
        var updated=game.withGameOver(true);
        dataAccess.updateGame(updated);
        return updated;
    }

    public GameData getGameMetadata(int gameID) throws DataAccessException {return dataAccess.getGame(gameID);}

    public ChessGame getEngine(int gameID) throws DataAccessException {return dataAccess.getEngine(gameID);}
}
