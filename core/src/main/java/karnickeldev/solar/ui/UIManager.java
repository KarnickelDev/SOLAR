package karnickeldev.solar.ui;

import karnickeldev.solar.ui.pausemenu.OptionsMenu;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 08.11.2024
 */
public class UIManager {

    private OptionsMenu optionsMenu;

    private MainMenu mainMenu;

    public UIManager() {

    }

    public void create() {

        // update Skin
        SkinManager.update();

        // UI Elements
        mainMenu = new MainMenu(SkinManager.getUISkin());

        optionsMenu = new OptionsMenu(SkinManager.getUISkin());
    }

    public MainMenu getMainMenu() {return mainMenu;}
    public OptionsMenu getOptionsMenu() {return optionsMenu;}

    public void resize(int width, int height) {
        Fonts.resizeFonts(height);
        SkinManager.update();

        mainMenu.resizeUI(width, height);
        optionsMenu.resizeUI(width, height);
    }

}
