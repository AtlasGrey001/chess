package client;

import chess.ChessGame;
import model.GameData;
import org.glassfish.tyrus.client.ClientManager;
import ui.BoardRender;

import java.net.URI;
import java.util.Scanner;

public class GameMenu {
    private final ServerFacade facade;
    private final ClientState state;
    private final MyGameList cache;
    private final Scanner scanner;

    public GameMenu(ServerFacade facade,ClientState state,MyGameList cache,Scanner scanner) {
        this.facade=facade;
        this.state=state;
        this.cache=cache;
        this.scanner=scanner;
    }

    public void listGames() throws Exception {
        var games=facade.listGames(state.authToken());
        cache.setGames(games);
        if (games.isEmpty()) {
            System.out.println("No games found.");
            return;
        }

        for (int i=0;i<games.size();i++) {
            GameData g=games.get(i);
            System.out.printf("%d. %s (white=%s, black=%s)%n",
                    i+1,
                    g.gameName(),
                    g.whiteUsername(),
                    g.blackUsername());
        }
    }

    public void createGame() throws Exception {
        System.out.print("Game name: ");
        String name=scanner.nextLine();
        var game=facade.createGame(state.authToken(),name);
        System.out.println("Created game: "+game.gameName());
    }

    public void playGame() throws Exception {
        if (cache.isEmpty()) {
            System.out.println("You must list games first.");
            return;
        }

        System.out.print("Game #: ");
        int num;
        try{
            num=Integer.parseInt(scanner.nextLine());
        } catch(Exception ex){
            num=0;
        }
        GameData game=cache.getByIndex(num);
        System.out.print("Color (WHITE/BLACK): ");
        String color=scanner.nextLine().trim().toUpperCase();
        facade.joinGame(state.authToken(), game.gameID(), color);

        ChessGame.TeamColor perspective=
                "BLACK".equals(color) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        ChessGame chessGame=new ChessGame();
        chessGame.getBoard().resetBoard();
        System.out.println("Entering game view (no gameplay yet)...");
        BoardRender.drawBoard(chessGame,perspective);
    }

    public void observeGame() throws Exception {
        if (cache.isEmpty()) {
            System.out.println("You must list games first.");
            return;
        }
        System.out.print("Game #: ");

        int num=Integer.parseInt(scanner.nextLine());
        GameData game=cache.getByIndex(num);

        System.out.println("Connecting as observer...");

        String url=String.format(
                "ws://localhost:8080/ws/game/%d?token=%s",
                game.gameID(),
                state.authToken()
        );

        //System.out.println("Observing game...");
        //facade.joinGame(state.authToken(),game.gameID(),null);
        ChessGame chessGame=new ChessGame();
        chessGame.getBoard().resetBoard();
        System.out.println("Observing game...");
        BoardRender.drawBoard(chessGame,ChessGame.TeamColor.WHITE);
    }
}
