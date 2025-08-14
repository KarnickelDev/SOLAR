package karnickeldev.solar.assetmanager;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public enum Asset {

    GAME_ICON(AssetWrapper.ICONS + "solar_icon_512.png", Texture.class, true),
    STARRY_SKY_BACKGROUND_TILES(AssetWrapper.TEXTURES + "starry_sky_tiles.png", Texture.class, true),
    MAIN_MENU_BACKGROUND_SCENERY(AssetWrapper.TEXTURES + "landscape.png", Texture.class, true),
    TREE_LINE(AssetWrapper.TEXTURES + "treeline.png", Texture.class, true),
    STARS_ATLAS(AssetWrapper.TEXTURES + "stars.atlas", TextureAtlas.class, true);


    private final String path;
    private final Class<?> type;
    private final boolean isGlobal;

    Asset(String path, Class<?> type, boolean isGlobal) {
        this.path = path;
        this.type = type;
        this.isGlobal = isGlobal;
    }

    public String getPath() {
        return path;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

}
