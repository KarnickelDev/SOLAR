package karnickeldev.solar.ui.layers.escapemenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIExitReason;
import karnickeldev.solar.ui.core.UILayer;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public class EscapeMenuLayer extends UILayer {

    private EscapeMenu escapemenu;

    public EscapeMenuLayer() {
        super("escape_menu", new Stage(new ScreenViewport()));
    }

    @Override
    public boolean isModal() {
        return true;
    }

    @Override
    public void onEnter() {
        //addComponent("escape_menu", new EscapeMenu(() -> UI.getUIManager().requestPop(this, UIExitReason.USER_CLOSE)));
        escapemenu = new EscapeMenu(() -> UI.getUIManager().requestPop(this, UIExitReason.USER_CLOSE));
    }

    @Override
    public void act(float delta) {
        getStage().getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        super.act(delta);
        escapemenu.layout(1,1,1);
        escapemenu.act(delta);
    }

    @Override
    public void draw() {
        super.draw();
        stage.getBatch().begin();
        escapemenu.render(new RendererContext((SpriteBatch) stage.getBatch(), null));
        stage.getBatch().end();
    }

    @Override
    public void resize(int width, int height) {
        escapemenu.invalidateLayout();
    }

    @Override
    public void onExit(UIExitReason reason) {
        removeComponent("escape_menu");
    }

    @Override
    public void onFocus() {
        showComponent("escape_menu");
        escapemenu.setVisible(true);
    }

    @Override
    public void onBlur() {
        hideComponent("escape_menu");
        escapemenu.setVisible(false);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            if (!escapemenu.hit(screenX, screenY)) return false;
            InputEvent e = new InputEvent();
            e.setButton(button);
            e.setType(InputEvent.Type.touchDown);
            e.setStageX(screenX);
            e.setStageY(screenY);
            escapemenu.handleInput(e);
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.ESCAPE) {
            UI.getUIManager().requestPop(this, UIExitReason.USER_CLOSE);
            return true;
        }

        return false;
    }
}
