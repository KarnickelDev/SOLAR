package karnickeldev.solar.ui.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * @author : KarnickelDev
 * @since : 07.07.2025
 **/
public final class UI {

    public static final short VIRTUAL_WIDTH = 1920;
    public static final short VIRTUAL_HEIGHT = 1080;

    public static final Color WHITE = new Color(0xEED7A8FF);
    public static final Color DARK_RED = new Color(0x6F3929FF);

    public static Skin skin() {
        return getSkinManager().getSkin();
    }

    public static SkinManager getSkinManager() {
        return SkinManager.get();
    }

    public static FontManager getFontManager() {
        return getSkinManager().getFontManager();
    }

    public static Stage stage() {
        return getUIManager().getStage();
    }

    public static UIManager getUIManager() {
        return UIManager.get();
    }

}
