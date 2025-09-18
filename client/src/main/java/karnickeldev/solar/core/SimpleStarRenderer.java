package karnickeldev.solar.core;

/**
 * @author : KarnickelDev
 * @since : 18.09.2025
 **/
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.ecs.components.RadiusComponent;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.render.camera.FloatingOriginCamera;

/**
 * Minimal star renderer: draws a circular star with radial falloff.
 * Usage: call renderStar(worldX, worldY, worldRadiusWorldUnits, color, camera) from your render loop.
 */
public class SimpleStarRenderer {

    private final SpriteBatch batch;
    private final ShaderProgram shader;
    private final Texture whiteTex;

    private double elapsedTime = 0;

    public SimpleStarRenderer(SpriteBatch batch) {
        this.batch = batch;

        // compile shader
        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(Gdx.files.internal("shaders/sphere_vertex.glsl"), Gdx.files.internal("shaders/sphere_fragment.glsl"));
        if (!shader.isCompiled()) {
            String log = shader.getLog();
            throw new RuntimeException("Star shader compile error:\n" + log);
        }

        // small white texture (1x1)
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(1f, 1f, 1f, 1f);
        px.fill();
        whiteTex = new Texture(px);
        px.dispose();
    }

    public void renderStar(double worldX, double worldY, double worldRadius) {

        float cx = (float) worldX;
        float cy = (float) worldY;

        // 2) draw using SpriteBatch and shader
        // Note: setShader must be applied before begin(); SpriteBatch will set u_projTrans automatically.
        batch.setShader(shader);
        batch.begin();

        shader.setUniformf("u_color", 1f, 0.7f, 0.3f);
        shader.setUniformf("u_edgeSoftness", 0.01f);
        shader.setUniformf("u_coronaIntensity", 0.8f); // base value; doubled in shader
        shader.setUniformf("u_coronaRadius", 0.5f);
        shader.setUniformf("u_pixelSize", 0.0006f);
        shader.setUniformf("u_time", (float) elapsedTime);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        elapsedTime += Gdx.graphics.getDeltaTime();

        float screenRadius = (float) worldRadius;
        float diameter = screenRadius * 2f;

        // draw a quad centered at (cx,cy) with size diameter
        float drawX = cx - screenRadius;
        float drawY = cy - screenRadius;


        // default blending (SRC_ALPHA, ONE_MINUS_SRC_ALPHA) is OK for a simple star
        batch.draw(whiteTex, drawX, drawY, diameter, diameter);

        batch.end();
        batch.setShader(null);
    }

    public void dispose() {
        shader.dispose();
        whiteTex.dispose();
    }
}

