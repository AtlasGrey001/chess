package client;

import model.GameData;
import java.util.ArrayList;
import java.util.List;

public class MyGameList {
    private final List<GameData> lastGames=new ArrayList<>();

    public void setGames(List<GameData> games) {
        lastGames.clear();
        if (games!=null) {lastGames.addAll(games);}
    }

    public List<GameData> games() {return List.copyOf(lastGames);}

    public GameData getByIndex(int index1Based) {
        int idx=index1Based-1;
        if (idx<0 || idx>=lastGames.size()) {
            throw new IllegalArgumentException("Invalid game number...");
        }
        return lastGames.get(idx);
    }

    public boolean isEmpty() {return lastGames.isEmpty();}
}
