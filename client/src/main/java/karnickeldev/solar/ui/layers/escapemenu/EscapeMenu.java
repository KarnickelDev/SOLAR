package karnickeldev.solar.ui.layers.escapemenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.ui.components.*;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.components.UILayout;
import karnickeldev.solar.ui.layers.settings.SettingsMenuLayer;
import karnickeldev.solar.ui.screens.MainMenuScreen;

/**
 * @author KarnickelDev
 * @since 10.07.2025
 **/
public class EscapeMenu extends UIContainer {

    private static final int backgroundColor = 0x0F1215F0;
    private static final int borderColor =     0x34312FFF;
    private static final int buttonColor =     0x0F1215FF;
    private static final int fontColor =       0xF9D2B6FF;

    private final Runnable onClose;

    private final Panel panel;
    private final TextButton resume;
    private final TextButton saveOrPlayerlist;
    private final TextButton settings;
    private final TextButton back;
    private final TextButton exit;

    public EscapeMenu(Runnable onClose) {
        this.onClose = onClose;

        setPadding(0);
        setBorderThickness(0);
        getLayout().anchor = UILayout.Anchor.CENTER;
        getLayout().fixedWidth(300);
        getLayout().fixedHeight(460);

        panel = new Panel(new Color(backgroundColor), new Color(borderColor));
        panel.getLayout().percentWidth(1f);
        panel.getLayout().percentHeight(1f);
        panel.setBorderThickness(2f);

        Color buttonColorObj = new Color(buttonColor);

        resume = new TextButton("Resume");
        resume.setPadding(10f);
        resume.setBorderThickness(2f);
        resume.setOnClick(this::onResume);
        resume.setBackgroundColor(buttonColorObj);
        resume.setBorderColor(borderColor);
        resume.setFontColor(fontColor);
        resume.getLayout().percentWidth(1f).fixedHeight(48);

        saveOrPlayerlist = new TextButton(GameContext.get().isSingleplayer() ? "Save" : "Playerlist");
        saveOrPlayerlist.setPadding(10f);
        saveOrPlayerlist.setBorderThickness(2f);
        saveOrPlayerlist.setBackgroundColor(buttonColorObj);
        saveOrPlayerlist.setBorderColor(borderColor);
        saveOrPlayerlist.setFontColor(fontColor);
        saveOrPlayerlist.getLayout().percentWidth(1f).fixedHeight(48);

        settings = new TextButton("Settings");
        settings.setPadding(10f);
        settings.setBorderThickness(2f);
        settings.setOnClick(this::onSettings);
        settings.setBackgroundColor(buttonColorObj);
        settings.setBorderColor(borderColor);
        settings.setFontColor(fontColor);
        settings.getLayout().percentWidth(1f).fixedHeight(48);

        back = new TextButton(GameContext.get().isSingleplayer() ? "Back" : "Disconnect");
        back.setPadding(10f);
        back.setBorderThickness(2f);
        back.setOnClick(this::onBack);
        back.setBackgroundColor(buttonColorObj);
        back.setBorderColor(borderColor);
        back.setFontColor(fontColor);
        back.getLayout().percentWidth(1f).fixedHeight(48);

        exit = new TextButton("Exit");
        exit.setPadding(10f);
        exit.setBorderThickness(2f);
        exit.setOnClick(this::onExit);
        exit.setBackgroundColor(buttonColorObj);
        exit.setBorderColor(borderColor);
        exit.setFontColor(fontColor);
        exit.getLayout().percentWidth(1f).fixedHeight(48);

        Spacer[] spacers = new Spacer[6];
        for (int i = 0; i < spacers.length; i++) {
            spacers[i] = new Spacer(100,100);
            spacers[i].getLayout().percentWidth(1f);
            spacers[i].getLayout().fillHeight((i == 0 || i == spacers.length-1) ? 0.5f : 1f);
        }

        VerticalGroup verticalGroup = new VerticalGroup();
        verticalGroup.getLayout().fill();
        verticalGroup.setPadding(15f);

        verticalGroup.add(spacers[0]);
        verticalGroup.add(resume);
        verticalGroup.add(spacers[1]);
        verticalGroup.add(saveOrPlayerlist);
        verticalGroup.add(spacers[2]);
        verticalGroup.add(settings);
        verticalGroup.add(spacers[3]);
        verticalGroup.add(back);
        verticalGroup.add(spacers[4]);
        verticalGroup.add(exit);
        verticalGroup.add(spacers[5]);

        OptionCycler.OptionDefinition<Integer> def = new  OptionCycler.OptionDefinition<>(
            new String[]{"25%", "50%", "75%", "100%"},
            new Integer[]{25, 50, 75, 100}
        );

        OptionCycler<Integer> cycler = new OptionCycler<>(def);
        cycler.getLayout().fixedHeight(48).percentWidth(1f);
        verticalGroup.add(cycler);

        add(panel);
        add(verticalGroup);
    }

    @Override
    public boolean handleInput(InputEvent e) {
        for(UIElement child : children) {
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
