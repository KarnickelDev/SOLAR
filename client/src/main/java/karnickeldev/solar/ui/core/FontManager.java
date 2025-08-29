package karnickeldev.solar.ui.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : KarnickelDev
 * @since : 07.07.2025
 **/
public class FontManager {

    public enum Fonts {
        JETBRAINS_MONO("JetBrainsMono", "JetBrainsMono-Regular.ttf","JetBrainsMono-Bold.ttf"),
        FONT_AWESOME("FontAwesome", "FontAwesome7-Free-Solid-900.otf","FontAwesome7-Free-Solid-900.otf"),
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

    protected FontManager() {

    }

    public BitmapFont getFont(Fonts font, int size, boolean bold) {
        String key = font.name + "-" + size + (bold ? "-bold" : "-reg");

        if(!fontCache.containsKey(key)) {
            fontCache.put(key, generateFont(font, size, Gdx.graphics.getHeight(), bold));
        }
        return fontCache.get(key);
    }

    public BitmapFont getFont(int size, boolean bold) {
        return getFont(Fonts.JETBRAINS_MONO, size, bold);
    }

    public BitmapFont getFont(int size) {
        return getFont(Fonts.JETBRAINS_MONO, size, false);
    }

    public void clearCache() {
        for(BitmapFont font: fontCache.values()) {
            font.dispose();
        }
        fontCache.clear();
    }

    public BitmapFont generateFont(Fonts font, int size, int appHeight, boolean bold) {
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = Math.max(2, Math.round(size * (appHeight / (float)UI.VIRTUAL_HEIGHT)));
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        param.incremental = false;
        StringBuilder customCharacters = new StringBuilder(param.characters);
        for(char unicode = 0xF000; unicode < 0xF2FF; unicode++) {
            customCharacters.append(unicode);
        }
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
