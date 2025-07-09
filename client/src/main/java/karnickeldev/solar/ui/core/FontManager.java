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

    private final FreeTypeFontGenerator fontGenRegular;
    private final FreeTypeFontGenerator fontGenBold;

    private final Map<String, BitmapFont> fontCache = new HashMap<>(8);

    protected FontManager() {
        fontGenRegular = new FreeTypeFontGenerator(Gdx.files.internal("fonts/JetBrainsMono-Regular.ttf"));
        fontGenBold = new FreeTypeFontGenerator(Gdx.files.internal("fonts/JetBrainsMono-Bold.ttf"));
    }

    public BitmapFont getFont(int size, boolean bold) {
        String key = size + (bold ? "-bold" : "-reg");

        if(!fontCache.containsKey(key)) {
            fontCache.put(key, generateFont(size, Gdx.graphics.getHeight(), bold));
        }
        return fontCache.get(key);
    }

    public void clearCache() {
        for(BitmapFont font: fontCache.values()) {
            font.dispose();
        }
        fontCache.clear();
    }

    public BitmapFont generateFont(int size, int appHeight, boolean bold) {
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = Math.max(4, Math.round(size * (appHeight / (float)UI.VIRTUAL_HEIGHT)));
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        param.incremental = false;
        return (bold ? fontGenBold : fontGenRegular).generateFont(param);
    }

    protected void dispose() {
        clearCache();
        fontGenRegular.dispose();
        fontGenBold.dispose();
    }

}
