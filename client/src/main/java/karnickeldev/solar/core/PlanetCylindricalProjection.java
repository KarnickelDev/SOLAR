package karnickeldev.solar.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class PlanetCylindricalProjection extends ApplicationAdapter {
    ShaderProgram shader;
    float time = 0f;
    float x = 0f;
    private SpriteBatch batch;
    private Texture texture, newTexture, shadowTexture;
    private Texture cylindricalTexture;
    private int textureWidth, textureHeight;

    public static Texture move(Texture originalTexture, int x0, int y0, int width, int height) {

        // Create a Pixmap to manipulate pixel data
        Pixmap originalPixmap = textureToPixmap(originalTexture);
        Pixmap cylindricalPixmap = new Pixmap(width, height, originalPixmap.getFormat());

        for (int y = 0; y < height; y++) {
            for (int x = x0; x < x0 + width; x++) {
                cylindricalPixmap.drawPixel(x - x0, y, originalPixmap.getPixel(x % width, y));
            }
        }

        // Convert the Pixmap back to a Texture
        return new Texture(cylindricalPixmap);
    }

    /**
     * Applies cylindrical projection to the given texture and returns a new distorted texture.
     */
    public static Texture applyCylindricalProjection(Texture originalTexture, int x0, int y0, int width, int height) {

        // Create a Pixmap to manipulate pixel data
        Pixmap originalPixmap = textureToPixmap(originalTexture);
        Pixmap cylindricalPixmap = new Pixmap(width, height, originalPixmap.getFormat());

        // Loop through each pixel and remap the vertical coordinates
        for (int y = 0; y <= height / 2; y++) {
            // Compute the normalized vertical coordinate (v) between 0 and 1
            float v = ((float) y) / ((float) height);
            // Apply the sine distortion to compress the poles
            float distortedV = MathUtils.sin(v * (MathUtils.PI / 2f)); // Sine to compress poles

            // Map distortedV back to the original texture's vertical coordinate
            int originalY = (int) (distortedV * height);

            // Copy each row of pixels from the original pixmap to the distorted pixmap
            for (int x = x0; x < x0 + width; x++) {
                int pixel = originalPixmap.getPixel(x % 550, y);
                cylindricalPixmap.drawPixel(x - x0, y, pixel);
            }
        }

        for (int y = height / 2; y <= height; y++) {
            // Compute the normalized vertical coordinate (v) between 0 and 1
            float v = ((float) y - height) / ((float) height);
            v = 1f - v;
            // Apply the sine distortion to compress the poles
            float distortedV = MathUtils.sin(v * (MathUtils.PI / 2f)); // Sine to compress poles

            // Map distortedV back to the original texture's vertical coordinate
            int originalY = (int) (distortedV * height);

            // Copy each row of pixels from the original pixmap to the distorted pixmap
            for (int x = x0; x < x0 + width; x++) {
                int pixel = originalPixmap.getPixel(x % 550, y);
                cylindricalPixmap.drawPixel(x - x0, y, pixel); // Draw the pixel in flipped Y
            }
        }

        // Convert the Pixmap back to a Texture
        return new Texture(cylindricalPixmap);
    }

    public static Texture applyCircle(Texture originalTexture) {
        int width = originalTexture.getWidth();
        int height = originalTexture.getHeight();

        int mid = height / 2;

        // Create a Pixmap to manipulate pixel data
        Pixmap originalPixmap = textureToPixmap(originalTexture);
        Pixmap spherePixemap = new Pixmap(width, height, originalPixmap.getFormat());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int dx = x - mid;
                int dy = y - mid;
                int d = dx * dx + dy * dy;
                if (d < mid * mid) {
                    spherePixemap.drawPixel(x, y, originalPixmap.getPixel(x, y));
                } else {
                    spherePixemap.drawPixel(x, y, Color.BLACK.toIntBits());
                }
            }
        }

        return new Texture(spherePixemap);
    }

    /**
     * Converts a Texture to a Pixmap.
     */
    private static Pixmap textureToPixmap(Texture texture) {
        // Framebuffer Object (FBO) to read the texture into a Pixmap
        TextureData textureData = texture.getTextureData();
        if (!textureData.isPrepared()) {
            textureData.prepare();
        }
        return textureData.consumePixmap();
    }

    @Override
    public void create() {
        batch = new SpriteBatch();

        shader = new ShaderProgram(Gdx.files.internal("sphere_vertex.glsl"), Gdx.files.internal("test.glsl"));
        if (!shader.isCompiled()) {
            System.out.println("Shader compile error: " + shader.getLog());
        }

        // Load your 2D texture (e.g., a world map)
        texture = new Texture(Gdx.files.internal("map.png"));
        textureWidth = texture.getWidth();
        textureHeight = texture.getHeight();

        // Create a new texture with cylindrical projection applied
        cylindricalTexture = applyCircle(applyCylindricalProjection(texture, 0, 0, 336, 336));

        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        cylindricalTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        newTexture = applyCircle(texture);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        time += Gdx.graphics.getDeltaTime();

        if (time > 0.1f) {
            time = 0f;
            x += 100f * Gdx.graphics.getDeltaTime();
        }

        // Apply your circular transformations
        newTexture = applyCircle(applyCylindricalProjection(texture, (int) x, 0, 336, 336));

        batch.begin();

        // Set the shader
        //batch.setShader(shaderProgram);

        // Bind the texture and set uniforms

        // Draw the texture as a 2D image
        //batch.draw(newTexture, 200, 200, 600, 600);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        // Reset the shader

        batch.end();

        batch.setShader(shader);

        // Variables for light and planet properties
        Vector2 lightPos = new Vector2(100, 500); // Light source position
        Vector2 planetPos = new Vector2(300, 200); // Planet position
        float planetRadius = 300; // Planet radius
        float lightIntensity = 10.0f; // Intensity of the light
        float shadowSoftness = 0.5f; // Softness of the shadow

        // Begin rendering
        batch.begin();

        // Set the shader uniforms
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);  // Set the active texture unit
        newTexture.bind();                      // Bind the texture to this unit

        // Inform the shader which texture unit the texture is bound to (0 in this case)
        shader.setUniformi("u_texture", 0);

        // Draw the planet texture
        batch.draw(newTexture, planetPos.x - planetRadius, planetPos.y - planetRadius, planetRadius * 2, planetRadius * 2);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        texture.dispose();
        cylindricalTexture.dispose();
    }
}
