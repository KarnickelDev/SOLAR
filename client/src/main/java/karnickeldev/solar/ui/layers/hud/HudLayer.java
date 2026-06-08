package karnickeldev.solar.ui.layers.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.network.packets.ChatMessagePacket;
import karnickeldev.solar.ui.components.*;
import karnickeldev.solar.ui.core.*;
import karnickeldev.solar.ui.fontutil.RichTextBuilder;
import karnickeldev.solar.ui.fontutil.TextBlock;
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

    public UIContainer chatContainer = new VerticalGroup();

    TextButton label = new TextButton("Label");

    public HudLayer() {
        super("hud", new Stage(new ScreenViewport(new OrthographicCamera())));

        chatActor = new ChatWindow();

        label.setFontColor(0xFFFFFFFF);
        label.setBackgroundColor(new Color(0x202020FF));
        label.setPadding(10, 10, 10, 10);

        renderer = new MessageRenderer(new ChatMessageStore());
        renderer.setPad(5,5,5,5,5);
        renderer.setBorderThickness(2);

        chatContainer.add(renderer, new UILayout().percentWidth(1).percentHeight(1));

        label.setDebug(true);
        chatContainer.setDebug(true);

        getCanvas().add(label, new Canvas.CanvasSlot().anchor(Canvas.Anchor.TOP));
        getCanvas().add(chatContainer, new Canvas.CanvasSlot()
            .anchor(Canvas.Anchor.BOTTOM_LEFT).fixedSize(420, 320).offset(20, 58)
        );
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void act(float dt) {
        getStage().getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        super.act(dt);
    }

    @Override
    public void resize(int width, int height) {
        chatContainer.invalidateLayout();
        label.invalidateLayout();
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
                renderer.addMessage(new TextBlock(new RichTextBuilder(MessageRenderer.DEFAULT_CHAT_FONT_SIZE)
                    .color(Color.CYAN).text(MultiplayerMenu.playerDisplayName + ": ")
                    .color(UI.WHITE).text(chatActor.getTextField().getText())
                    .build()));
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
