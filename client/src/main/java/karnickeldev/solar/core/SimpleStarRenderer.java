package karnickeldev.solar.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import karnickeldev.solar.util.ShaderUtils;

/**
 * Minimal star renderer: draws a circular star with radial falloff.
 * Usage: call renderStar(worldX, worldY, worldRadiusWorldUnits, color, camera) from your render loop.
* @author : KarnickelDev
* @since : 18.09.2025
 */
public class SimpleStarRenderer {

    private final SpriteBatch batch;
    private final ShaderProgram starShader;
    private final ShaderProgram coronaShader;
    private final Texture whiteTex;

    private double elapsedTime = 0;

    public SimpleStarRenderer(SpriteBatch batch) {
        this.batch = batch;

        // compile shader
        ShaderProgram.pedantic = false;
        String star_vertex = ShaderUtils.preprocessShader("shaders/stars/star_vertex.glsl");
        String star_fragment = ShaderUtils.preprocessShader("shaders/stars/star_fragment.glsl");
        starShader = new ShaderProgram(star_vertex, star_fragment);
        if (!starShader.isCompiled()) {
            String log = starShader.getLog();
            throw new RuntimeException("Star shader compile error:\n" + log);
        }

        String corona_vertex = ShaderUtils.preprocessShader("shaders/stars/corona_vertex.glsl");
        String corona_fragment = ShaderUtils.preprocessShader("shaders/stars/corona_fragment.glsl");
        coronaShader = new ShaderProgram(corona_vertex, corona_fragment);
        if (!coronaShader.isCompiled()) {
            String log = coronaShader.getLog();
            throw new RuntimeException("Corona shader compile error:\n" + log);
        }

        // small white texture (1x1)
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(1f, 1f, 1f, 1f);
        px.fill();
        whiteTex = new Texture(px);
        px.dispose();
    }

    public void renderStar(double worldX, double worldY, double worldRadius) {

        elapsedTime += Gdx.graphics.getDeltaTime();

        float T = (float) (elapsedTime * elapsedTime * 8);
        //T = 2200;

        float cx = (float) worldX;
        float cy = (float) worldY;

        // corona
        batch.setShader(coronaShader);
        batch.begin();

        coronaShader.setUniformf("u_temperature", T);
        coronaShader.setUniformf("u_time", (float) elapsedTime);

        float coronaScreenRadius = (float) worldRadius * 4f;

        float coronaX = cx - coronaScreenRadius;
        float coronaY = cy - coronaScreenRadius;

        batch.draw(whiteTex, coronaX, coronaY, 2*coronaScreenRadius, 2*coronaScreenRadius);

        // star surface
        batch.setShader(starShader);

        starShader.setUniformf("u_temperature", T);
        starShader.setUniformf("u_time", (float) elapsedTime);
        starShader.setUniformf("u_pixelation", 0);

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
        starShader.dispose();
        whiteTex.dispose();
    }
}

