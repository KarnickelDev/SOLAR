package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.render.SimplexNoise;
import karnickeldev.solar.util.BlackBodyUtil;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal star renderer: draws a circular star with radial falloff.
 * Usage: call renderStar(worldX, worldY, worldRadiusWorldUnits, color, camera) from your render loop.
* @author KarnickelDev
* @since 18.09.2025
 */
public class SimpleStarRenderer {

    private final SpriteBatch batch;
    private final Texture whiteTex;
    private final Texture blackBodyColorTexture;
    private final Texture noiseTex;

    private static final List<FrameBuffer> fboLODs = new ArrayList<>(1);

    private double elapsedTime = 1;

    public SimpleStarRenderer(SpriteBatch batch) {
        this.batch = batch;

        // compile shader
        ShaderProgram.pedantic = false;

        GameContext.get().getShaderManager().registerFromInternalFile(
            "star_shader",
            "shaders/stars/minimalist/star_vertex.glsl",
            "shaders/stars/minimalist/star_fragment.glsl"
        );

        GameContext.get().getShaderManager().registerFromInternalFile(
            "corona_shader",
            "shaders/stars/minimalist/corona_vertex.glsl",
            "shaders/stars/minimalist/corona_fragment.glsl"
        );

        // small white texture (1x1)
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(1f, 1f, 1f, 1f);
        px.fill();
        whiteTex = new Texture(px);
        px.dispose();


        int size = 256;
        Pixmap bb = new Pixmap(size, 1, Pixmap.Format.RGB888);
        for(int i = 0; i < size; i++) {
            float t = i / (float) (size - 1);
            float temp = 1000f + t * (40000 - 1000f);
            float[] rgb = BlackBodyUtil.rgb(temp);
            bb.setColor(rgb[0], rgb[1], rgb[2], 1);
            bb.drawPixel(i, 0);
        }

        blackBodyColorTexture = new Texture(bb);
        blackBodyColorTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        bb.dispose();

        Pixmap small = new Pixmap(256, 256, Pixmap.Format.RGB888);
        for(int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                float r = (float) noise(x + 42, y - 15, 1);
                float g = (float) noise(x - 37, y + 3.14, 0.5);
                float b = (float) noise(x + 2.7,y - 1.14, 0.1);
                small.setColor(r,g,b,1);
                small.drawPixel(x,y);
            }
        }
        noiseTex = new Texture(small);
        noiseTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        noiseTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        small.dispose();

        fboLODs.add(new FrameBuffer(Pixmap.Format.RGBA8888,64, 64, false));
        fboLODs.add(new FrameBuffer(Pixmap.Format.RGBA8888,512, 512, false));
        fboLODs.add(new FrameBuffer(Pixmap.Format.RGBA8888,1024, 1024, false));
        fboLODs.add(new FrameBuffer(Pixmap.Format.RGBA8888,1024*4, 1024*4, false));
        fboLODs.add(new FrameBuffer(Pixmap.Format.RGBA8888,1024*8, 1024*8, false));
    }

    private static double noise(double x, double y, double scale) {
        double n = SimplexNoise.noise(x * scale, y * scale);
        return 0.5*(n+1);
    }

    private static float starTemperature(double t) {
        double duration = 30;
        double min = 1000;
        double max = 30_000;

        if (t < 0) t = 0;
        if (t > duration) t = duration;

        double progress = t / duration;

        return (float) (min + (max - min) * Math.pow(progress, 3.7));
    }

    private static FrameBuffer getFBO(double worldSize) {
        double pixels = 1.1 * GameContext.get().getWorldManager().getActiveWorld().getCamera().projectLength(worldSize);

        for(FrameBuffer lod: fboLODs) {
            if(lod.getWidth() > pixels) {
                return lod;
            }
        }

        return fboLODs.getLast();
    }

    public void renderStar(double worldX, double worldY, double worldRadius) {
        ShaderProgram starShader = GameContext.get().getShaderManager().get("star_shader");
        ShaderProgram coronaShader = GameContext.get().getShaderManager().get("corona_shader");

        elapsedTime += Gdx.graphics.getDeltaTime();
        double t = elapsedTime % 30;
        float T = starTemperature(t);
        //T = 1200;

        float cx = (float) worldX;
        float cy = (float) worldY;
        float screenRadius = (float) worldRadius;

        FrameBuffer frameBuffer = getFBO(2*worldRadius);

        // prepare FBO and batch
        Matrix4 oldProj = new Matrix4(batch.getProjectionMatrix());
        final int fbSize = frameBuffer.getWidth();
        batch.getProjectionMatrix().setToOrtho2D(0, 0, fbSize, fbSize);
        batch.setColor(1,1,1,1);

        frameBuffer.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw corona directly to screen
        batch.setShader(coronaShader);
        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // bind LUT to unit 1 properly
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
        blackBodyColorTexture.bind();
        coronaShader.bind();
        coronaShader.setUniformi("u_blackBodyTex", 1);

        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE2);
        noiseTex.bind();
        coronaShader.bind();
        coronaShader.setUniformi("u_noiseTex", 2);

        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        coronaShader.setUniformf("u_temperature", T);
        coronaShader.setUniformf("u_time", (float) elapsedTime);
        coronaShader.setUniformf("u_zoom",
            (float) GameContext.get().getWorldManager().getActiveWorld().getCamera().getZoom()
        );

        batch.draw(whiteTex, 0, 0, fbSize, fbSize);
        batch.end();

        // draw star
        batch.setShader(starShader);
        batch.begin();
        batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // bind textures to units with explicit active calls
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
        blackBodyColorTexture.bind();
        starShader.bind();
        starShader.setUniformi("u_blackBodyTex", 1);

        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE2);
        noiseTex.bind();
        starShader.setUniformi("u_noiseTex", 2);

        // restore active to 0
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        // set other uniforms (shader is bound)
        starShader.setUniformf("u_temperature", T);
        starShader.setUniformf("u_sunSpotSeed", 42f);
        starShader.setUniformf("u_time", (float) elapsedTime);
        starShader.setUniformf("u_edgeSmoothing", 0.1f);

        float size = fbSize / 2f;
        batch.draw(whiteTex,
            (fbSize - size) / 2f,
            (fbSize - size) / 2f,
            size, size);

        batch.end();
        frameBuffer.end();
        batch.setShader(null);
        batch.setProjectionMatrix(oldProj);

        Texture starTex = frameBuffer.getColorBufferTexture();
        starTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        starTex.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

        batch.begin();
        batch.draw(starTex, cx - 2*screenRadius, cy - 2*screenRadius, 4*screenRadius, 4*screenRadius, 0f, 0f, 1f, 1f);
        batch.end();


        batch.setShader(null);
        batch.setColor(1,1,1,1);
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // just for safety
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    }

    public void dispose() {
        GameContext.get().getShaderManager().unload("star_shader");
        GameContext.get().getShaderManager().unload("corona_shader");
        whiteTex.dispose();
        blackBodyColorTexture.dispose();
        noiseTex.dispose();
        fboLODs.forEach(FrameBuffer::dispose);
    }
}

