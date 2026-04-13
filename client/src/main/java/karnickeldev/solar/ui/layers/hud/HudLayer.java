package karnickeldev.solar.ui.layers.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.network.packets.ChatMessagePacket;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.*;
import karnickeldev.solar.ui.core.*;
import karnickeldev.solar.ui.layers.hud.chat.*;
import karnickeldev.solar.ui.layers.hud.game.DateDisplay;
import karnickeldev.solar.ui.layers.mainmenu.MultiplayerMenu;

/**
 * @author KarnickelDev
 * @since 04.04.2026
 **/
public class HudLayer extends UILayer {

    public static HudLayer INSTANCE = new HudLayer();

    private boolean wasGamePaused = false;

    public MessageRenderer renderer;

    public ChatWindow chatActor;

    public UIContainer chatContainer = new UIContainer();

    TextButton label = new TextButton("Label");

    public HudLayer() {
        super("hud", new Stage(new ScreenViewport(new OrthographicCamera())));

        chatActor = new ChatWindow();

        label.setFontColor(UI.WHITE);
        label.setBackgroundColor(new Color(0x202020FF));

        renderer = new MessageRenderer(new ChatMessageStore());
        renderer.setPad(5,5,5,5,5);
        renderer.setBorderThickness(3);
        renderer.getLayout().fixedWidth = 420;
        renderer.getLayout().fixedHeight = 320;
        renderer.getLayout().offsetX = 20;
        renderer.getLayout().offsetY = 58;

        chatContainer.add(renderer);
        chatContainer.setX(0);
        chatContainer.setY(0);
        chatContainer.getLayout().widthPercent = 1;
        chatContainer.getLayout().heightPercent = 1;

        UIElement topRight = new UIElement() {
            @Override
            public void act(float dt) {

            }

            @Override
            public void render(RendererContext ctx) {

            }

            @Override
            public boolean handleInput(InputEvent e) {
                return false;
            }
        };
        topRight.getLayout().anchor = UILayout.Anchor.TOP_RIGHT;

        UIElement topLeft = new UIElement() {
            @Override
            public void act(float dt) {

            }

            @Override
            public void render(RendererContext ctx) {

            }

            @Override
            public boolean handleInput(InputEvent e) {
                return false;
            }
        };
        topLeft.getLayout().anchor = UILayout.Anchor.TOP_LEFT;

        UIElement bottomRight = new UIElement() {
            @Override
            public void act(float dt) {

            }

            @Override
            public void render(RendererContext ctx) {

            }

            @Override
            public boolean handleInput(InputEvent e) {
                return false;
            }
        };
        bottomRight.getLayout().anchor = UILayout.Anchor.BOTTOM_RIGHT;

        chatContainer.add(topRight);
        chatContainer.add(bottomRight);
        chatContainer.add(topLeft);

        label.getLayout().anchor = UILayout.Anchor.TOP;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void act(float dt) {
        getStage().getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        super.act(dt);
        //renderer.layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 1);
        //renderer.act(dt);

        shapeRenderer.setProjectionMatrix( new Matrix4().setToOrtho2D(0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        chatContainer.layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 1);
        chatContainer.act(dt);

        label.layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 1);
        label.act(dt);
    }

    ShapeRenderer shapeRenderer = new ShapeRenderer();

    @Override
    public void draw() {
        stage.draw();

        stage.getBatch().begin();
        //renderer.draw(stage.getBatch(), 1f);
        chatContainer.render(new RendererContext((SpriteBatch) stage.getBatch(), null));
        label.render(new RendererContext((SpriteBatch) stage.getBatch(), null));
        stage.getBatch().end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        chatContainer.setDebug(true);
        chatContainer.renderDebug(new RendererContext(null, shapeRenderer));
        label.setDebug(true);
        label.renderDebug(new RendererContext(null, shapeRenderer));
        shapeRenderer.end();

    }

    @Override
    public void resize(int width, int height) {
        chatContainer.invalidateLayout();
        label.invalidateLayout();
        //renderer.invalidateLayout();
    }

    @Override
    public void onEnter() {
        addForceComponent("debug", new DebugToolTip(false));
        addComponent("date_display", new DateDisplay());

        addComponent("chat_window", chatActor);
    }

    @Override
    public void onExit(UIExitReason reason) {
        removeComponent("debug");
        removeComponent("date_display");

        removeComponent("chat_window");
    }

    @Override
    public void onFocus() {
        showComponent("debug");
        showComponent("date_display");

        if(GameContext.get().isSingleplayer()) GameContext.get().getClock().getSimSpeedController().requestPause(wasGamePaused);
    }

    @Override
    public void onBlur() {
        wasGamePaused = GameContext.get().getClock().isPaused();
        if(GameContext.get().isSingleplayer()) GameContext.get().getClock().getSimSpeedController().requestPause(true);
    }

    @Override
    public boolean blocksInput() {
        return chatActor.isActive();
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if(!renderer.hit(Gdx.input.getX(),  Gdx.input.getY())) return false;

        InputEvent e = new InputEvent();
        e.reset();
        e.setType(InputEvent.Type.scrolled);
        e.setScrollAmountY(amountY);
        return renderer.handleInput(e);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(button == Input.Buttons.LEFT) {
            if(!label.hit(screenX, screenY)) return false;
            InputEvent e = new InputEvent();
            e.reset();
            e.setType(InputEvent.Type.touchDown);
            e.setButton(Input.Buttons.LEFT);
            label.handleInput(e);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.ESCAPE) {
            if(chatActor.isActive()) {
                chatActor.deactivate();
                return true;
            }
        }

        if(keycode == Input.Keys.ENTER) {
            if(blocksInput()) {
                renderer.addMessage(new ChatMessageBuilder(MultiplayerMenu.playerDisplayName, chatActor.getTextField().getText()).build());
                GameContext.get().getClientNetwork().send(new ChatMessagePacket(MultiplayerMenu.playerDisplayName, chatActor.getTextField().getText()));
                chatActor.handleInput();
            } else {
                chatActor.activate();
            }
            return true;
        }

        return false;
    }

}
