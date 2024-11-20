package karnickeldev.solar.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import karnickeldev.solar.core.SolarMain;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 08.11.2024
 */
public class UIManager {

    private Stage stage;

    private OptionsMenu optionsMenu;


    public UIManager(Stage stage) {
        this.stage = stage;
        optionsMenu = new OptionsMenu(stage,
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenWidth(),
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenHeight()
        );
    }

    public OptionsMenu getOptionsMenu() {
        return optionsMenu;
    }

    public void resize(int width, int height) {
        Fonts.resizeFonts(height);
        optionsMenu.dispose();
        optionsMenu = new OptionsMenu(stage, width, height);
    }

}
