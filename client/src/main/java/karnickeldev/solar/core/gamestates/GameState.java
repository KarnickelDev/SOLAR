package karnickeldev.solar.core.gamestates;

/**
 * @author : KarnickelDev
 * @since : 04.07.2025
 **/
public interface GameState {

    void enter();

    void exit();

    void render(float delta);

    void resize(int width, int height);

    GameStateID getID();
}
