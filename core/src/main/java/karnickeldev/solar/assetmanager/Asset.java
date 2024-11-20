package karnickeldev.solar.assetmanager;

import com.badlogic.gdx.graphics.Texture;

public enum Asset {


    STARRY_SKY_BACKGROUND_TILES(AssetWrapper.TEXTURES + "starry_sky_tiles.png", Texture.class, true),
    ;



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
