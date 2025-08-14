package karnickeldev.solar.assetmanager;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Disposable;

public class AssetWrapper implements Disposable {

    public static final String TEXTURES = "textures/";
    public static final String ICONS = "icons/";

    private static final AssetWrapper INSTANCE = new AssetWrapper();
    private final AssetManager assetManager;

    private AssetWrapper() {
        assetManager = new AssetManager();
    }

    public static AssetWrapper getInstance() {
        return INSTANCE;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public void loadGlobal(Asset asset) {
        if (asset.isGlobal()) assetManager.load(asset.getPath(), asset.getType());
    }

    public void unload(Asset asset) {
        assetManager.unload(asset.getPath());
    }

    public <T> T getAsset(Asset asset) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) asset.getType();
        return assetManager.get(asset.getPath(), type);
    }

    public boolean isLoaded(Asset asset) {
        return assetManager.isLoaded(asset.getPath(), asset.getType());
    }

    public boolean update(int millis) {
        return assetManager.update(millis);
    }

    public void finishLoading() {
        assetManager.finishLoading();
    }

    public boolean isFinished() {
        return assetManager.isFinished();
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }

}
