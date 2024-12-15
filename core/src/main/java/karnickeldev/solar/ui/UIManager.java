package karnickeldev.solar.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import karnickeldev.solar.ui.pausemenu.OptionsMenu;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 08.11.2024
 */
public class UIManager {

    private OptionsMenu optionsMenu;

    private Skin uiSkin;

    private MainMenu mainMenu;

    public UIManager() {

    }

    public void create() {

        // setup UI Skin
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        // UI Elements
        mainMenu = new MainMenu(uiSkin);

        optionsMenu = new OptionsMenu(uiSkin);
    }

    public Skin getUISkin() {return uiSkin;}

    public MainMenu getMainMenu() {return mainMenu;}
    public OptionsMenu getOptionsMenu() {return optionsMenu;}

    public void resize(int width, int height) {
        Fonts.resizeFonts(height);

        mainMenu.resizeUI(width, height);
        optionsMenu.resizeUI(width, height);
    }

}
