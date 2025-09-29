package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import karnickeldev.solar.context.GameContext;

/**
 * Minimal star renderer: draws a circular star with radial falloff.
 * Usage: call renderStar(worldX, worldY, worldRadiusWorldUnits, color, camera) from your render loop.
* @author : KarnickelDev
* @since : 18.09.2025
 */
public class SimpleStarRenderer {

    private final SpriteBatch batch;
    private final Texture whiteTex;

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
    }

    private static float starTemperature(double t, double duration, double min, double max) {
        if (t < 0) t = 0;
        if (t > duration) t = duration;

        double progress = t / duration;

        return (float) (min + (max - min) * Math.pow(progress, 3.7));
    }

    public void renderStar(double worldX, double worldY, double worldRadius) {

        ShaderProgram starShader = GameContext.get().getShaderManager().get("star_shader");
        ShaderProgram coronaShader = GameContext.get().getShaderManager().get("corona_shader");

        elapsedTime += Gdx.graphics.getDeltaTime();

        double t = elapsedTime % 30;

        float T = starTemperature(t, 30, 1000, 30_000);
        //T = 2000;

        float cx = (float) worldX;
        float cy = (float) worldY;

        // corona
        batch.setShader(coronaShader);
        batch.begin();

        coronaShader.setUniformf("u_temperature", T);
        coronaShader.setUniformf("u_time", (float) elapsedTime);
        coronaShader.setUniformf("u_zoom", (float) GameContext.get().getWorldManager().getActiveWorld().getCamera().getRenderZoom());

        float coronaScreenRadius = (float) worldRadius * 4f;

        float coronaX = cx - coronaScreenRadius;
        float coronaY = cy - coronaScreenRadius;

        batch.draw(whiteTex, coronaX, coronaY, 2*coronaScreenRadius, 2*coronaScreenRadius);

        // star surface
        batch.setShader(starShader);

        starShader.setUniformf("u_temperature", T);
        starShader.setUniformf("u_sunSpotSeed", 42);
        starShader.setUniformf("u_time", (float) elapsedTime);
        starShader.setUniformf("u_edgeSmoothing", 0.15f);
        //starShader.setUniformf("u_pixelation", 0.005f);

        float screenRadius = (float) worldRadius;
        float diameter = screenRadius * 2f;

        // draw a quad centered at (cx,cy) with size diameter
        float drawX = cx - screenRadius;
        float drawY = cy - screenRadius;


        batch.draw(whiteTex, drawX, drawY, diameter, diameter);

        batch.end();
        batch.setShader(null);
    }

    public void dispose() {
        GameContext.get().getShaderManager().unload("star_shader");
        GameContext.get().getShaderManager().unload("corona_shader");
        whiteTex.dispose();
    }
}

