package karnickeldev.solar.core.gamestates;

import com.badlogic.gdx.Screen;

/**
 * @author : KarnickelDev
 * @since : 04.07.2025
 **/
public class GameStateScreen implements Screen {

    private final GameState gameState;

    public GameStateScreen(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void render(float delta) {
        gameState.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        gameState.resize(width, height);
    }

    @Override
    public void show() {
        //gameState.enter();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        //gameState.exit();
    }
}
