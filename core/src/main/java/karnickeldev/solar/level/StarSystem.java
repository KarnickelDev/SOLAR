package karnickeldev.solar.level;

public class StarSystem {

    public StarSystemTree starSystem;

    public StarSystem(StarSystemTree starSystem) {
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
