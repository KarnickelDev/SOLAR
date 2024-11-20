package karnickeldev.solar.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

public class PlanetShaderExample implements Screen {
    private SpriteBatch batch;
    private Texture texture, normalMap;
    private ShaderProgram shader;

    private Vector3 lightPos = new Vector3(0.4f, 0.5f, 0.0f);
    private Vector3 lightColor = new Vector3(1.0f, 1.0f, 1.0f);
    private Vector3 ambientColor = new Vector3(0.0f, 0.0f, 0.0f);

    public PlanetShaderExample() {
        batch = new SpriteBatch();
        texture = new Texture("planet.png"); // Base texture
        normalMap = new Texture("normal_map.png"); // Normal map

        // Load shader from string
        String vertexShader = Gdx.files.internal("vertex_shader.glsl").readString();
        String fragmentShader = Gdx.files.internal("fragment_shader.glsl").readString();
        shader = new ShaderProgram(vertexShader, fragmentShader);

        if (!shader.isCompiled()) {
            Gdx.app.error("Shader", shader.getLog());
        }

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Use the shader
        batch.setShader(shader);

        // Begin the batch with the shader

        // Set uniforms for the shader
        shader.setUniformf("u_lightPos", lightPos);
        shader.setUniformf("u_lightColor", lightColor);
        shader.setUniformf("u_ambientColor", ambientColor);

        // Bind the normal map to the second texture unit
        normalMap.bind(1);
        shader.setUniformi("u_normalMap", 1);

        // Bind the texture to the first texture unit and render
        texture.bind(0);
        shader.setUniformi("u_texture", 0);

        batch.draw(texture, 600, 600, 256, 256);
        batch.end();

        // Reset to the default shader after rendering
        batch.setShader(null);

        lightPos.add(delta * -0.1f, 0f, 0f);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
        texture.dispose();
        normalMap.dispose();
        shader.dispose();
    }
}
