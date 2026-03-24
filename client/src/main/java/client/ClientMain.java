package client;

import ui.EscapeSequences;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        ClientState state=new ClientState();
        MyGameList cache=new MyGameList();
        int port=8080;
        ServerFacade facade=new ServerFacade(port);
        GameMenu gameMenu=new GameMenu(facade,state,cache,scanner);

        printBanner();

        while (true) {
            try {
                if (state.mode()==ClientState.Mode.PRELOADING) {
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN+"[prelogin] >>> "+EscapeSequences.RESET_TEXT_COLOR);
                    String input=scanner.nextLine().trim().toLowerCase();
                    switch (input) {
                        case "help"-> printPreloadingHelp();
                        case "quit"->{
                            System.out.println("Goodbye.");
                            return;
                        }
                        case "login"->doLogin(scanner,facade,state);
                        case "register"->doRegister(scanner,facade,state);
                        default->System.out.println("Unknown command. Type 'help'.");
                    }
                } else {
                    System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE+"[postlogin] >>> "+EscapeSequences.RESET_TEXT_COLOR);
                    String input=scanner.nextLine().trim().toLowerCase();
                    switch (input) {
                        case "help"->printPostloginHelp();
                        case "logout"->{
                            facade.logout(state.authToken());
                            state.logout();
                            System.out.println("Logged out.");
                        }
                        case "create game"->gameMenu.createGame();
                        case "list games"->gameMenu.listGames();
                        case "play game"->gameMenu.playGame();
                        case "observe game"->gameMenu.observeGame();
                        case "quit"->{
                            System.out.println("Goodbye.");
                            return;
                        }
                        default->System.out.println("Unknown command. Type 'help'.");
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error: "+ex.getMessage());
            }
        }
    }

    private static void doLogin(Scanner scanner,ServerFacade facade,ClientState state) throws Exception {
        System.out.print("Username: ");
        String u=scanner.nextLine();
        System.out.print("Password: ");
        String p=scanner.nextLine();
        var auth=facade.login(u, p);
        state.login(u,auth.authToken());
        System.out.println("Logged in as "+u);
    }

    private static void doRegister(Scanner scanner,ServerFacade facade,ClientState state) throws Exception {
        System.out.print("Username: ");
        String u=scanner.nextLine();
        System.out.print("Password: ");
        String p=scanner.nextLine();
        System.out.print("Email: ");
        String e=scanner.nextLine();
        var auth=facade.register(u,p,e);
        state.login(u,auth.authToken());
        System.out.println("Registered and logged in as "+u);
    }

    private static void printBanner() {
        System.out.println(EscapeSequences.SET_TEXT_BOLD+"♕ 240 Chess Client"+EscapeSequences.RESET_TEXT_BOLD_FAINT);
        System.out.println("Type 'help' to see available commands.");
    }

    private static void printPreloadingHelp() {
        System.out.println("Preloading commands:");
        System.out.println("  help       - show this help");
        System.out.println("  login      - login with username/password");
        System.out.println("  register   - create a new account");
        System.out.println("  quit       - exit the program");
    }

    private static void printPostloginHelp() {
        System.out.println("Post login commands:");
        System.out.println("  help         - show this help");
        System.out.println("  logout       - logout and return to prelogin");
        System.out.println("  create game  - create a new game");
        System.out.println("  list games   - list existing games");
        System.out.println("  play game    - join a game as a player");
        System.out.println("  observe game - observe a game");
        System.out.println("  quit         - exit the program");
    }
}
