package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.settings.Resolution;

import java.util.Arrays;
import java.util.Comparator;

public class BackgroundStarRenderer {

    private static final float ROTATION_SPEED = 15.0f * 32;

    private static final float SMALLEST_RESOLUTION_HEIGHT = Arrays.stream(Resolution.values())
        .min(Comparator.comparingInt(Resolution::getHeight)).orElse(Resolution.FALLBACK_RESOLUTION).getHeight();

    private static TextureRegion[] starTextures;

    private static float x, y, lat, lon;

    private static int STAR_COUNT = 1024;
    private static float[] ra = new float[STAR_COUNT], dec = new float[STAR_COUNT], brightness = new float[STAR_COUNT];
    private static char[] size = new char[STAR_COUNT], color = new char[STAR_COUNT];

    private static float elapsed_time = 0;

    public static void loadAssets() {
        TextureAtlas starAtlas = AssetWrapper.getInstance().getAsset(Asset.STARS_ATLAS);
        assert(starAtlas != null);

        starTextures = new TextureRegion[] {
            starAtlas.findRegion("star_small_white"),
            starAtlas.findRegion("star_small_yellow"),
            starAtlas.findRegion("star_small_blue"),
            starAtlas.findRegion("star_small_red"),
            starAtlas.findRegion("star_mid_white"),
            starAtlas.findRegion("star_mid_yellow"),
            starAtlas.findRegion("star_mid_blue"),
            starAtlas.findRegion("star_mid_red"),
            starAtlas.findRegion("star_big_white"),
            starAtlas.findRegion("star_big_yellow"),
            starAtlas.findRegion("star_big_blue"),
            starAtlas.findRegion("star_big_red"),
        };
        for (TextureRegion starTexture : starTextures) assert (starTexture != null);
        starAtlas.getTextures().first().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    public static void setPosition(float centerX, float centerY, float latitude, float longitude) {
        x = centerX;
        y = centerY;
        lat = latitude;
        lon = longitude;
    }

    public static void setStarParameters(int starCount, char[] starSize, char[] starColor, float[] starBrightness,
                                         float[] rightAscension, float[] declination) {
        STAR_COUNT = starCount;
        size = starSize;
        color = starColor;
        brightness = starBrightness;
        ra = rightAscension;
        dec = declination;
    }

    private static void drawStar(SpriteBatch batch, int size, char color, float initialRightAscension, float declination, float elapsedTime) {

        // Compute updated right ascension based on Earth's rotation
        float currentRA = (initialRightAscension + ROTATION_SPEED * elapsedTime) % 360;

        float latOffset = (lat / 90f) * (Gdx.graphics.getHeight() / 2f);
        // Convert longitude to horizontal offset (x)
        float lonOffset = (lon / 360f) * Gdx.graphics.getWidth();

        // Convert degrees to radians
        float radRA = (float) Math.toRadians(currentRA + lon);
        float radDec = (float) Math.toRadians(declination + lat);

        float radius = Gdx.graphics.getWidth();

        // Convert spherical coordinates to 2D Cartesian coordinates
        float dx = x + radius * (float) Math.cos(radRA) * (float) Math.cos(radDec);
        float dy = y + radius * (float) Math.sin(radRA) * (float) Math.cos(radDec);

        int wh = size == 2 ? 3 : size == 1 ? 2 : 1;
        wh = Math.round((Gdx.graphics.getHeight() / SMALLEST_RESOLUTION_HEIGHT) * wh);
        batch.draw(getTexture(size, color), Math.round(lonOffset + dx), Math.round(latOffset + dy), wh, wh);
    }


    public static TextureRegion getTexture(int size, char color) {
        assert(size >= 0 && size < 3);
        int c = color == 'r' ? 3 : color == 'b' ? 2 : color == 'y' ? 1 : 0;
        int index = 4*size + c;
        return starTextures[index];
    }

    public static boolean drawStarScape(SpriteBatch batch, float delta, boolean seeing) {
        if(size == null || color == null || ra == null || dec == null) return false;
        if(size.length != color.length || color.length != brightness.length || brightness.length != ra.length || ra.length != dec.length) return false;

        float prev_r = batch.getColor().r;
        float prev_g = batch.getColor().g;
        float prev_b = batch.getColor().b;
        float prev_a = batch.getColor().a;

        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        elapsed_time = (elapsed_time + delta) % 1e7f;

        int n_stars = size.length;

        for(int i = 0; i < n_stars; i++) {
            float b = brightness[i] - (!seeing ? 0 : 0.18f*(1 + (float)Math.sin(elapsed_time + 512*brightness[i])));
            batch.setColor(1f,1f,1f, b);
            drawStar(batch, size[i], color[i], ra[i], dec[i], 0);
        }

        batch.setColor(prev_r, prev_g, prev_b, prev_a);
        return true;
    }

}
