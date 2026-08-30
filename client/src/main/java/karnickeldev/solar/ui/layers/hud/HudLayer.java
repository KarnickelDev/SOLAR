package karnickeldev.solar.ui.layers.hud;

import com.badlogic.gdx.Gdx;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ui.components.*;
import karnickeldev.solar.ui.components.container.VerticalGroup;
import karnickeldev.solar.ui.components.styles.TextWidgetStyle;
import karnickeldev.solar.ui.components.widgets.DebugToolTip;
import karnickeldev.solar.ui.components.widgets.TextButton;
import karnickeldev.solar.ui.components.widgets.TextWidget;
import karnickeldev.solar.ui.core.*;
import karnickeldev.solar.ui.layers.hud.chat.*;
import karnickeldev.solar.ui.layers.hud.game.DateDisplay;

/**
 * @author KarnickelDev
 * @since 04.04.2026
 **/
public class HudLayer extends UILayer {

    public static HudLayer INSTANCE = new HudLayer();

    private boolean wasGamePaused = false;

    public MessageRenderer renderer;

    public VerticalGroup chatContainer = new VerticalGroup();

    private static final class FPSWidget extends TextWidget {
        public FPSWidget(String text, TextWidgetStyle textStyle) {
            super(text, textStyle);
        }
        @Override
        public void onAct(float delta) {
            setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        }
    }

    FPSWidget fps;

    public HudLayer() {
        super("hud");


        TextWidgetStyle fpsStyle = new TextWidgetStyle();
        fpsStyle.setFontColor(0x00FF00FF, UIState.all());

        fps = new FPSWidget("", fpsStyle);
        fps.setPadding(5f);

        renderer = new MessageRenderer(new ChatMessageStore());
        renderer.setPad(5,5,5,5,5);
        renderer.setBorderThickness(2);

        chatContainer.add(renderer, new UILayout().percentWidth(1).percentHeight(1));

        getCanvas().add(fps, new Canvas.CanvasSlot().anchor(Canvas.Anchor.TOP_RIGHT));
        getCanvas().add(chatContainer, new Canvas.CanvasSlot()
            .anchor(Canvas.Anchor.BOTTOM_LEFT).fixedSize(420, 320).offset(20, 58)
        );
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void resize(int width, int height) {
        chatContainer.invalidateLayout();
        fps.invalidateLayout();
    }

    @Override
    public void onEnter() {
        addForceComponent("debug", new DebugToolTip(false));
        addComponent("date_display", new DateDisplay());
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
    public void act(float delta) {

    }

    @Override
    public boolean blocksInput() {
        return false;
        //return chatActor.isActive();
    }

    @Override
    public boolean keyDown(int keycode) {
//        if(keycode == Input.Keys.ESCAPE) {
//            if(chatActor.isActive()) {
//                chatActor.deactivate();
//                return true;
//            }
//        }
//
//        if(keycode == Input.Keys.ENTER) {
//            if(blocksInput()) {
//                renderer.addMessage(new TextBlock(new RichTextBuilder(MessageRenderer.DEFAULT_CHAT_FONT_SIZE)
//                    .color(Color.CYAN).text(MultiplayerMenu.playerDisplayName + ": ")
//                    .color(UI.WHITE).text(chatActor.getTextField().getText())
//                    .build()));
//                GameContext.get().getClientNetwork().send(new ChatMessagePacket(MultiplayerMenu.playerDisplayName, chatActor.getTextField().getText()));
//                chatActor.handleInput();
//            } else {
//                chatActor.activate();
//            }
//            return true;
//        }
//
//        return false;
        return false;
    }

}
