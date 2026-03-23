package client;

public class ClientState {
    public enum Mode {
        PRELOADING,
        POSTLOGIN
    }

    private Mode mode=Mode.PRELOADING;
    private String authToken=null;
    private String username=null;

    public Mode mode() {
        return mode;
    }

    public boolean isLoggedIn() {
        return mode==Mode.POSTLOGIN && authToken!=null;
    }

    public String authToken() {
        return authToken;
    }

    public String username() {
        return username;
    }

    public void login(String username,String authToken) {
        this.username=username;
        this.authToken=authToken;
        this.mode=Mode.POSTLOGIN;
    }

    public void logout() {
        this.username=null;
        this.authToken=null;
        this.mode=Mode.PRELOADING;
    }
}
