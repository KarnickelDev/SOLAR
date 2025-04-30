package karnickeldev.solar.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import karnickeldev.solar.ui.pausemenu.OptionsMenu;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 08.11.2024
 */
public class UIManager {

    private OptionsMenu mainMenuOptionsMenu;

    private EscapeMenu escapeMenu;

    private MainMenu mainMenu;

    public UIManager() {

    }

    public void create() {

        // update Skin
        SkinManager.update();

        // UI Elements
        mainMenu = new MainMenu(SkinManager.getUISkin());

        mainMenuOptionsMenu = new OptionsMenu(SkinManager.getUISkin(), mainMenu);

        escapeMenu = new EscapeMenu();
    }

    public MainMenu getMainMenu() {
        return mainMenu;
    }
    public OptionsMenu getMainMenuOptionsMenu() {
        return mainMenuOptionsMenu;
    }
    public EscapeMenu getEscapeMenu() {
        return escapeMenu;
    }

    public void resize(int width, int height) {
        Fonts.resizeFonts(height);
        SkinManager.update();

        mainMenu.resizeUI(width, height);
        mainMenuOptionsMenu.resizeUI(width, height);
        escapeMenu.resizeUI(width, height);
    }

}
