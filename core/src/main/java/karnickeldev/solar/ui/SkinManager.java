package karnickeldev.solar.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class SkinManager {

    private static Skin uiSkin;

    public static final char BUTTON_SMALL = 0;
    public static final char BUTTON_MEDIUM = 1;
    public static final char BUTTON_MEDIUM_BOLD = 2;
    public static final char BUTTON_BIG = 3;

    private static TextButton.TextButtonStyle button_small, button_medium, button_medium_bold, button_big;

    public static void update() {
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        TextButton test = new TextButton("test", uiSkin);

        button_small = test.getStyle();
        button_small.font = Fonts.SMALL;

        button_medium = test.getStyle();
        button_medium.font = Fonts.MEDIUM;

        button_medium_bold = test.getStyle();
        button_medium_bold.font = Fonts.MEDIUM_BOLD;

        button_big = test.getStyle();
        button_big.font = Fonts.BIG;
    }

    public static Skin getUISkin() {
        return uiSkin;
    }

    public static TextButton.TextButtonStyle getTextButtonStyle(char fontSize) {
        switch (fontSize) {
            case BUTTON_SMALL:
                return button_small;
            case BUTTON_MEDIUM_BOLD:
                return button_medium_bold;
            case BUTTON_BIG:
                return button_big;
            default:
                return button_medium;
        }
    }

}
