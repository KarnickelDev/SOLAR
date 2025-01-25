package karnickeldev.solar.gamestate;

import java.io.*;

public class GameState {

    public StarSystemTree starSystem;

    public GameState(StarSystemTree starSystem) {
        this.starSystem = starSystem;
    }

    public boolean load(String path) {
        this.starSystem = null;
        return false;
    }

    public boolean store(String path) {
        return false;
    }

    public StarSystemTree getStarSystem() {
        return starSystem;
    }

}
