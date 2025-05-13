package karnickeldev.solar.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 11.10.2024
 */
public class Fonts {

    public static final char REGULAR = 0;
    public static final char ITALIC = 1;
    public static final char BOLD = 2;
    public static final String FONT = "PixelOperatorMono";
    private static final String FONTS_PATH = "fonts/";
    private static final String[] FONT_TYPE = new String[3];
    public static BitmapFont BIG, MEDIUM_BOLD, MEDIUM, SMALL, VERY_SMALL;

    static {
        FONT_TYPE[REGULAR] = FONTS_PATH + FONT + ".ttf";
        FONT_TYPE[ITALIC] = FONTS_PATH + FONT + ".ttf";
        FONT_TYPE[BOLD] = FONTS_PATH + FONT + "-Bold.ttf";
    }

    public static BitmapFont generateFontForResolution(int height, int baseFontSize, char type) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_TYPE[type % FONT_TYPE.length]));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        // Set the font size dynamically based on screen height
        parameter.size = Math.max(6, (int) (baseFontSize * (height / 1080f))); // Scale with height

        BitmapFont font = generator.generateFont(parameter); // Generates the font
        generator.dispose();

        return font;
    }


    public static void generateFonts(int appHeight) {
        BIG = generateFontForResolution(appHeight, 46, BOLD);
        MEDIUM_BOLD = generateFontForResolution(appHeight, 32, BOLD);
        MEDIUM = generateFontForResolution(appHeight, 32, REGULAR);
        SMALL = generateFontForResolution(appHeight, 22, REGULAR);
        VERY_SMALL = generateFontForResolution(appHeight, 17, REGULAR);
    }

    public static void resizeFonts(int appHeight) {
        BitmapFont oldBIG = BIG;
        BitmapFont oldMED_BOLD = MEDIUM_BOLD;
        BitmapFont oldMED = MEDIUM;
        BitmapFont oldSMALL = SMALL;
        BitmapFont oldVERY_SMALL = VERY_SMALL;
        generateFonts(appHeight);
        oldBIG.dispose();
        oldMED_BOLD.dispose();
        oldMED.dispose();
        oldSMALL.dispose();
        oldVERY_SMALL.dispose();
    }

    public static void disposeFonts() {
        BIG.dispose();
        MEDIUM_BOLD.dispose();
        MEDIUM.dispose();
        SMALL.dispose();
        VERY_SMALL.dispose();
    }

}
