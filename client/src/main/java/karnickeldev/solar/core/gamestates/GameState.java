package karnickeldev.solar.core.gamestates;

/**
 * @author : KarnickelDev
 * @since : 04.07.2025
 **/
public interface GameState {

    void enter();

    void exit();

    GameStateID getID();
}
