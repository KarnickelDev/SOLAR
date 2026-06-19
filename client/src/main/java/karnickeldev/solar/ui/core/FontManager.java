package karnickeldev.solar.ui.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author KarnickelDev
 * @since 07.07.2025
 **/
public class FontManager {

    public enum Fonts {
        JETBRAINS_MONO("JetBrainsMono", "JetBrainsMonoNerdFontPropo-Regular.ttf","JetBrainsMonoNerdFontPropo-Bold.ttf"),
        MARTIAN("Martian", "MartianMonoNerdFontPropo-Regular.ttf","MartianMonoNerdFontPropo-Bold.ttf"),
        ;

        private final String name;
        private final FreeTypeFontGenerator fontGenRegular;
        private final FreeTypeFontGenerator fontGenBold;
        Fonts(String name, String fileName, String boldFileName) {
            this.name = name;
            this.fontGenRegular = new FreeTypeFontGenerator(Gdx.files.internal("fonts/" + fileName));
            this.fontGenBold = new FreeTypeFontGenerator(Gdx.files.internal("fonts/" + boldFileName));
        }
    }


    private final Map<String, BitmapFont> fontCache = new HashMap<>(8);

    protected FontManager() {}

    private BitmapFont getFontRaw(Fonts font, int size, boolean bold) {
        String key = font.name + "-" + size + (bold ? "-bold" : "-reg");

        if(!fontCache.containsKey(key)) {
            fontCache.put(key, generateFont(font, size, bold));
        }
        return fontCache.get(key);
    }

    public BitmapFont getFont(Fonts font, int uiSize, float uiScale, boolean bold) {
        return getFontRaw(font, Math.round(uiSize * uiScale), bold);
    }

    public BitmapFont getFont(Fonts font, int uiSize, boolean bold) {
        return getFontRaw(font, Math.round(uiSize * 1), bold);
    }

    public BitmapFont getFont(int uiSize, boolean bold) {
        return getFont(Fonts.JETBRAINS_MONO, uiSize, 1, bold);
    }

    public BitmapFont getFont(int uiSize) {
        return getFont(Fonts.JETBRAINS_MONO, uiSize, 1,false);
    }

    public void clearCache() {
        for(BitmapFont font: fontCache.values()) {
            font.dispose();
        }
        fontCache.clear();
    }

    private BitmapFont generateFont(Fonts font, int size, boolean bold) {
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = Math.max(3, size);
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        param.incremental = false;
        param.hinting = FreeTypeFontGenerator.Hinting.Medium;
        StringBuilder customCharacters = new StringBuilder(param.characters);
        for(char unicode = 0xF000; unicode < 0xF2FF; unicode++) {
            customCharacters.append(unicode);
        }
        //arrows
        customCharacters.append((char)0x2191).append((char)0x2192).append((char)0x2193).append((char)0x2194);

        param.characters = customCharacters.toString();
        return (bold ? font.fontGenBold : font.fontGenRegular).generateFont(param);
    }

    protected void dispose() {
        clearCache();
        for(Fonts font: Fonts.values()) {
            font.fontGenRegular.dispose();
            font.fontGenBold.dispose();
        }
    }

}
