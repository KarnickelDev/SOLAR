package karnickeldev.solar.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.viewport.Viewport;
import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.util.MathUtil;

import java.util.Random;

/**
 * @author : KarnickelDev
 * @since : 06.07.2025
 **/
public class StarField {

    private static final int WIDTH = Math.round(1.1f * 2560);
    private static final int HEIGHT = Math.round(1.1f * 1440);
    private static final float MAX_BRIGHTNESS = Byte.MAX_VALUE;

    private static final Random rnd = new Random(System.nanoTime());

    private static TextureRegion[] starTextures;

    public static FrameBuffer starFieldBuffer;
    private static TextureRegion starFieldRegion;

    public static class Container {

        private final short starCount;
        private final float[] pos;
        private final byte[] brightness;
        private final byte[] sizeAndColor;

        private Container(short starCount, float[] pos, byte[] brightness, byte[] sizeAndColor) {
            this.starCount = starCount;
            this.pos = pos;
            this.brightness = brightness;
            this.sizeAndColor = sizeAndColor;
        }
    }

    public static void regenerateStarTexture(Container container) {
        if (starFieldBuffer != null) starFieldBuffer.dispose();

        starFieldBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, WIDTH, HEIGHT, false);
        starFieldRegion = new TextureRegion(starFieldBuffer.getColorBufferTexture());
        starFieldRegion.flip(false, true); // Important: Flip Y for correct orientation

        SpriteBatch fboBatch = new SpriteBatch();
        fboBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, WIDTH, HEIGHT));

        starFieldBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fboBatch.begin();
        for (int i = 0; i < container.starCount; i++) {
            int size = 1 + (container.sizeAndColor[i] & 0b00000011);

            float x = container.pos[2 * i] * WIDTH;
            float y = container.pos[2 * i + 1] * HEIGHT;

            fboBatch.setColor(1f, 1f, 1f, container.brightness[i] / MAX_BRIGHTNESS);
            fboBatch.draw(getTexture(container.sizeAndColor[i]), x, y, size, size);
        }
        fboBatch.end();
        starFieldBuffer.end();
        fboBatch.dispose();
    }



    public static Container generateRandom(short starCount, float xDev, float yDev) {
        float[] pos = new float[2*starCount];
        byte[] brightness = new byte[starCount];
        byte[] sizeAndColor = new byte[starCount];

        for(int i = 0; i < starCount; i++) {
            pos[2*i]     = 0.5f + (float) rnd.nextGaussian() * xDev;
            pos[2*i + 1] = 0.5f + (float) rnd.nextGaussian() * yDev;


            brightness[i] = (byte) rnd.nextInt(Byte.MAX_VALUE);

            // color
            int c = rnd.nextInt(100);
            if (c < 70) {
                sizeAndColor[i] = 0;
            } else {
                sizeAndColor[i] = (byte) (rnd.nextInt(4) << 6);
            }

            // size and brightness
            int s = rnd.nextInt(100);
            if(s < 86) {
                //sizeAndColor[i] = (byte)(sizeAndColor[i] | 1);
                brightness[i] = (byte)(MathUtil.random(0.25f, 1) * Byte.MAX_VALUE);
            } else if (s < 97) {
                sizeAndColor[i] = (byte)(sizeAndColor[i] | 1);
                brightness[i] = (byte)(MathUtil.random(0.5f, 1) * Byte.MAX_VALUE);
            } else {
                sizeAndColor[i] = (byte)(sizeAndColor[i] | 2);
                brightness[i] = (byte)(MathUtil.random(0.8f, 1) * Byte.MAX_VALUE);
            }


        }

        return new Container(starCount, pos, brightness, sizeAndColor);
    }

    public static void loadAssets() {
        TextureAtlas starAtlas = AssetWrapper.getInstance().getAsset(Asset.STARS_ATLAS);
        assert (starAtlas != null);

        // small textures double because we use them for both 1x1 and 3x3 sized stars it allows easy indexing
        starTextures = new TextureRegion[]{
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

    private static TextureRegion getTexture(byte sizeAndColor) {
        int index = ((sizeAndColor & 0b11000000) >> 6) + 3 * (sizeAndColor & 0b00000011);
        return starTextures[index];
    }

}
