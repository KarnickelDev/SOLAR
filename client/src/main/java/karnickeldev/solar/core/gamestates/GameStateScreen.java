package karnickeldev.solar.core.gamestates;

import com.badlogic.gdx.Screen;

/**
 * @author KarnickelDev
 * @since 04.07.2025
 **/
public interface GameStateScreen extends GameState, Screen {

    GameStateID getGameStateID();

}
