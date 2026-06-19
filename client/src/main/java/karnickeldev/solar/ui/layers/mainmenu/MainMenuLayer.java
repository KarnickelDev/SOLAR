package karnickeldev.solar.ui.layers.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.input.Keys;
import karnickeldev.solar.ui.components.Canvas;
import karnickeldev.solar.ui.components.UILayoutEngine;
import karnickeldev.solar.ui.components.UIState;
import karnickeldev.solar.ui.components.styles.TextWidgetStyle;
import karnickeldev.solar.ui.components.styles.TextButtonStyle;
import karnickeldev.solar.ui.components.widgets.DebugToolTip;
import karnickeldev.solar.ui.components.widgets.TextWidget;
import karnickeldev.solar.ui.layers.settings.SettingsMenuLayer;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIExitReason;
import karnickeldev.solar.ui.core.UILayer;
import karnickeldev.solar.ui.theme.ThemeColors;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public class MainMenuLayer extends UILayer {

    private final TextWidget fpsLabel;

    public MainMenuLayer() {
        super("main_menu");

        getCanvas().setDebug(true);

        TextWidgetStyle labelStyle = new TextWidgetStyle();
        labelStyle.setFontColor(0x00FF00FF, UIState.values());
        labelStyle.contentAlign = Align.left;
        labelStyle.textAlign = Align.left;
        labelStyle.fontSize = 25;

        fpsLabel = new TextWidget("FPS: 00", labelStyle);

        getCanvas().add(fpsLabel, new Canvas.CanvasSlot().anchor(Canvas.Anchor.TOP_RIGHT));

        ThemeColors colors = new ThemeColors(
            0x0,
            0x0,
            0x34312FFF,
            0x0,
            Color.rgba8888(UI.WHITE),
            0x0,
            0x0,
            0x0,
            0x0
        );
        TextWidgetStyle textButtonStyle = new TextButtonStyle(colors);
        textButtonStyle.fontSize = 32;
        textButtonStyle.setFontColor(Color.rgba8888(new Color(UI.WHITE).mul(1.3f)), UIState.HOVERED);
        textButtonStyle.setBorderColor(0, UIState.all());
        textButtonStyle.textAlign = karnickeldev.solar.ui.core.Align.LEFT;
        textButtonStyle.contentAlign = karnickeldev.solar.ui.core.Align.LEFT;

        MainMenu mainMenu = new MainMenu(textButtonStyle);
        getCanvas().add(mainMenu, new Canvas.CanvasSlot().anchor(Canvas.Anchor.LEFT).percentSize(0.3f, 0.4f));
    }

    @Override
    public boolean isModal() {
        return true;
    }

    @Override
    public void onEnter() {
        addForceComponent("debug", new DebugToolTip(true));
        //addComponent("main_menu", mainMenu);
        addComponent("multiplayer_menu", new MultiplayerMenu());
    }

    @Override
    public void onExit(UIExitReason reason) {
        removeComponent("debug");
        removeComponent("main_menu");
        removeComponent("multiplayer_menu");
    }

    @Override
    public void onFocus() {
        showComponent("debug");
        showComponent("main_menu");
        hideComponent("multiplayer_menu");
    }

    @Override
    public void onBlur() {

    }

    @Override
    public void act(float dt) {
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Keys.ESCAPE) {
            UI.getUIManager().push(new SettingsMenuLayer());
            return true;
        }

        return false;
    }

}
