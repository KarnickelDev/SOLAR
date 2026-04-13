package karnickeldev.solar.ui.layers.escapemenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.ui.components.Panel;
import karnickeldev.solar.ui.components.TextButton;
import karnickeldev.solar.ui.components.UIContainer;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.core.UIComponent;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UILayout;
import karnickeldev.solar.ui.layers.settings.SettingsMenuLayer;
import karnickeldev.solar.ui.screens.MainMenuScreen;

/**
 * @author KarnickelDev
 * @since 10.07.2025
 **/
public class EscapeMenu extends UIContainer {

    private final Runnable onClose;

    private final Panel panel;
    private final TextButton resume;
    private final TextButton settings;
    private final TextButton back;
    private final TextButton exit;

    public EscapeMenu(Runnable onClose) {
        this.onClose = onClose;

        Color buttonColor = new Color(0x7F0000FF);

        panel = new Panel(new Color(0x202020F0));
        panel.getLayout().anchor = UILayout.Anchor.CENTER;
        panel.getLayout().heightPercent = 1;
        panel.getLayout().widthPercent = 1;

        resume = new TextButton("Resume");
        resume.pad(10f);
        resume.setOnClick(this::onResume);
        resume.setBackgroundColor(buttonColor);
        resume.getLayout().anchor = UILayout.Anchor.CENTER;
        resume.getLayout().fixedWidth = 280;
        resume.getLayout().fixedHeight = 100;
        resume.getLayout().offsetY = 300;
        resume.getLayout().offsetX = 10;

        settings = new TextButton("Settings");
        settings.pad(10f);
        settings.setOnClick(this::onSettings);
        settings.setBackgroundColor(buttonColor);
        settings.getLayout().anchor = UILayout.Anchor.CENTER;
        settings.getLayout().fixedWidth = 280;
        settings.getLayout().fixedHeight = 100;
        settings.getLayout().offsetY = 200;
        settings.getLayout().offsetX = 10;

        back = new TextButton("Back");
        back.pad(10f);
        back.setOnClick(this::onBack);
        back.setBackgroundColor(buttonColor);
        back.getLayout().anchor = UILayout.Anchor.CENTER;
        back.getLayout().fixedWidth = 280;
        back.getLayout().fixedHeight = 100;
        back.getLayout().offsetY = 100;
        back.getLayout().offsetX = 10;

        exit = new TextButton("Exit");
        exit.pad(10f);
        exit.setOnClick(this::onExit);
        exit.setBackgroundColor(buttonColor);
        exit.getLayout().anchor = UILayout.Anchor.CENTER;
        exit.getLayout().fixedWidth = 280;
        exit.getLayout().fixedHeight = 100;
        exit.getLayout().offsetY = 0;
        exit.getLayout().offsetX = 10;

        getLayout().anchor = UILayout.Anchor.CENTER;
        getLayout().fixedWidth = 300;
        getLayout().fixedHeight = 400;

        add(panel);
        add(resume);
        add(settings);
        add(back);
        add(exit);
    }

    @Override
    public boolean handleInput(InputEvent e) {
        for(UIElement child: children) {
            if(child.hit(e.getStageX(), e.getStageY()) && child.handleInput(e)) return true;
        }
        return false;
    }

    private void onResume() {
        if(onClose != null) {
            onClose.run();
        } else {
            Logger.get(LogTag.UI).warn("onClose is null in " + this.getClass().getSimpleName());
        }
    }

    private void onSettings() {
        UI.getUIManager().push(new SettingsMenuLayer());
    }

    private void onBack() {
        GameStateManager.get().requestStateLoading(new MainMenuScreen(SolarMain.getInstance()));
    }

    private void onExit() {
        SolarMain.shutdown();
    }

}
