package karnickeldev.solar.ui.layers.escapemenu;

import com.badlogic.gdx.graphics.Color;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.ui.components.*;
import karnickeldev.solar.ui.components.container.VerticalGroup;
import karnickeldev.solar.ui.components.styles.SliderStyle;
import karnickeldev.solar.ui.components.styles.TextWidgetStyle;
import karnickeldev.solar.ui.components.widgets.*;
import karnickeldev.solar.ui.core.Align;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.components.UILayout;
import karnickeldev.solar.ui.layers.settings.SettingsMenuLayer;
import karnickeldev.solar.ui.screens.MainMenuScreen;
import karnickeldev.solar.ui.theme.ThemeColors;
import karnickeldev.solar.ui.theme.UITheme;

import java.util.Locale;

/**
 * @author KarnickelDev
 * @since 10.07.2025
 **/
public class EscapeMenu extends UIContainer {

    private static final class MenuButton extends TextButton {

        private final String rawText;
        public MenuButton(String text, TextWidgetStyle style, Runnable onClick) {
            super("  " + text.toLowerCase(Locale.ENGLISH), style, onClick);
            this.rawText = text.toLowerCase(Locale.ENGLISH);
        }

        @Override
        public void onHoverEnter() {
            super.onHoverEnter();
            setText("> " + rawText);
        }
        @Override
        public void onHoverExit() {
            super.onHoverExit();
            setText("  " + rawText);
        }
    }

    private final Runnable onClose;

    private final Panel panel;
    private final TextWidget title;
    private final TextButton resume;
    private final TextButton saveOrPlayerlist;
    private final TextButton settings;
    private final TextButton back;
    private final TextButton exit;

    private final HorizontalLine seperator;

    private final TextWidget controlsInfo;

    public EscapeMenu(Runnable onClose) {
        this.onClose = onClose;

        float buttonBorder = 0f;

        setPadding(0);
        setBorderThickness(0);

        UITheme theme = UI.getThemeManager().getTheme("default");
        ThemeColors colors = theme.colors();

        TextWidgetStyle textStyle = new TextWidgetStyle(theme.textWidgetStyle());
        textStyle.setBackgroundColor(0x16181AFF, UIState.all());
        textStyle.setBackgroundColor(0x1E2124FF, UIState.HOVERED);

        textStyle.setBorderColor(0x2A2D30FF, UIState.all());
        textStyle.setBorderColor(0x4B4F54FF, UIState.HOVERED);
        textStyle.setBorderColor(0, UIState.all());

        textStyle.setBackgroundColor(0x101214FF, UIState.all());
        textStyle.setBackgroundColor(0x1E2124FF, UIState.HOVERED, UIState.PRESSED);

        textStyle.setFontColor(colors.textPrimary(), UIState.all());
        textStyle.fontSize = 22;

        TextWidgetStyle titleStyle = new TextWidgetStyle(theme.textWidgetStyle());
        titleStyle.fontSize = 28;
        titleStyle.setFontColor(colors.textSecondary(), UIState.all());
        titleStyle.textAlign = Align.CENTER;
        titleStyle.contentAlign = Align.CENTER | Align.MIDDLE;

        title = new TextWidget("SOLAR", titleStyle);
        title.setPadding(5f);

        panel = new Panel(new Color(0x101214FF), new Color(colors.border()));
        panel.setBorderThickness(2f);

        seperator = new HorizontalLine(colors.textPrimary(), 1f);
        seperator.setPadding(0, 0, 5f, 5f);

        resume = new MenuButton("Resume", textStyle, this::onResume);
        resume.setPadding(20f);
        resume.setBorderThickness(buttonBorder);

        saveOrPlayerlist = new MenuButton(GameContext.get().isSingleplayer() ? "Save" : "Playerlist", textStyle, null);
        saveOrPlayerlist.setPadding(20f);
        saveOrPlayerlist.setBorderThickness(buttonBorder);

        settings = new MenuButton("Settings", textStyle, this::onSettings);
        settings.setPadding(20f);
        settings.setBorderThickness(buttonBorder);

        back = new MenuButton(GameContext.get().isSingleplayer() ? "Back" : "Disconnect", textStyle, this::onBack);
        back.setPadding(20f);
        back.setBorderThickness(buttonBorder);

        exit = new MenuButton("Exit", textStyle, this::onExit);
        exit.setPadding(20f);
        exit.setBorderThickness(buttonBorder);

        TextWidgetStyle ciStyle = new TextWidgetStyle(textStyle);
        ciStyle.fontSize = 12;
        controlsInfo = new TextWidget("[enter]: select\n[esc]: close", ciStyle);

        VerticalGroup verticalGroup = new VerticalGroup();
        verticalGroup.setPadding(40, 40, 15, 15);

        verticalGroup.add(title, new UILayout().percentWidth(1f));
        verticalGroup.add(new Spacer(0,0), new UILayout().percentWidth(1f).fillHeight(1f));
        verticalGroup.add(resume, new UILayout().percentWidth(1f).fixedHeight(48));
        verticalGroup.add(saveOrPlayerlist, new UILayout().percentWidth(1f).fixedHeight(48));
        verticalGroup.add(settings, new UILayout().percentWidth(1f).fixedHeight(48));
        verticalGroup.add(back, new UILayout().percentWidth(1f).fixedHeight(48));
        verticalGroup.add(exit, new UILayout().percentWidth(1f).fixedHeight(48));

        SliderStyle sliderStyle = new SliderStyle(SliderStyle.SliderChar.STEPPED_SQUARE, true, textStyle);
        Slider slider = new Slider(0,10, -1, true, sliderStyle, v -> (int)(v*10) + "%");
        verticalGroup.add(slider, new UILayout().percentWidth(1f).fixedHeight(50));

        OptionCycler.OptionDefinition<Integer> options = new OptionCycler.OptionDefinition<>(
            new String[]{"a", "b", "c"},
            new Integer[]{0, 1, 2}
        );

        OptionCycler<Integer> cycler = new OptionCycler<>(options, textStyle);
        //verticalGroup.add(cycler, new UILayout().percentWidth(1f).fixedHeight(48));

        verticalGroup.add(new Spacer(0,0), new UILayout().percentWidth(1f).fillHeight(1f));
        verticalGroup.add(seperator, new UILayout().percentWidth(1f).fixedHeight(60));
        verticalGroup.add(controlsInfo, new UILayout().percentWidth(1f).fixedHeight(48));

        add(panel);
        add(verticalGroup, new UILayout().percentHeight(1).percentWidth(1));
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
