package karnickeldev.solar.ui.fontutil.kernel;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import karnickeldev.solar.ui.fontutil.TextStyle;
import karnickeldev.solar.util.MathUtil;
import org.lwjgl.opengl.GL20;

/**
 * @author KarnickelDev
 * @since 19.04.2026
 **/
public final class MSDFBatch implements Disposable {

    public static final int POSITION_COMPONENTS = 2;
    public static final int UV_COMPONENTS = 2;
    public static final int COLOR_COMPONENTS = 1;
    public static final int STYLE_COMPONENTS = 1;

    public static final int VERTEX_FLOATS = POSITION_COMPONENTS + UV_COMPONENTS + COLOR_COMPONENTS + STYLE_COMPONENTS;

    public static final VertexAttributes VERTEX_ATTRIBUTES = new VertexAttributes(
        new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
        new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"),
        new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color"),
        new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 1, "a_style")
    );

    private static final int VERTICES_PER_GLYPH = 4;
    private static final int INDICES_PER_GLYPH = 6;
    public static final int FLOATS_PER_GLYPH = VERTICES_PER_GLYPH * VERTEX_FLOATS;

    private final int maxGlyphs;

    private final Mesh mesh;
    private final ShaderProgram shader;
    private final float[] vertices;

    private final Matrix4 projectionMatrix = new Matrix4();

    private int vertexFloatCount;
    private int glyphCount;

    private Texture currentTexture;
    private MSDFFont currentFont;

    private volatile boolean drawing = false;

    public MSDFBatch(int maxGlyphs, ShaderProgram shader) {
        this.maxGlyphs = maxGlyphs;
        this.shader = shader;

        this.vertices = new float[maxGlyphs * VERTICES_PER_GLYPH * VERTEX_FLOATS];

        this.mesh = new Mesh(
            true,
            maxGlyphs * VERTICES_PER_GLYPH,
            maxGlyphs * INDICES_PER_GLYPH,
            VERTEX_ATTRIBUTES
        );

        mesh.setIndices(buildIndices(maxGlyphs));
    }

    public void setProjectionMatrix(Matrix4 projectionMatrix) {
        this.projectionMatrix.set(projectionMatrix);
    }

    public Matrix4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public void begin() {
        if(drawing) throw new IllegalStateException("Must call end() before begin()");

        drawing = true;

        vertexFloatCount = 0;
        glyphCount = 0;

        currentTexture = null;
        currentFont = null;
    }

    public void draw(MSDFFont font, TextLayout layout, float x, float y) {
        if (layout == null || layout.glyphCount == 0) {
            return;
        }

        Texture texture = font.getAtlas();

        if (currentTexture != null && currentTexture != texture) {
            flush();
        }

        currentTexture = texture;
        currentFont = font;

        float drawX = x - layout.minX;
        float drawY = y - layout.minY;

        for (int i = 0; i < layout.glyphCount; i++) {
            if(isFlushRequired()) flush();

            Glyph g = font.getGlyphUnsafe(layout.glyphId[i]);

            boolean italic = (layout.flags[i] & TextStyle.ITALIC) != 0;
            float italicSkew = italic ? g.getHeight() * 0.3f * layout.scale[i] : 0f;

            float style = (layout.flags[i] & TextStyle.BOLD) != 0 ? 1f : 0f;

            float scale = layout.scale[i];

            float gx = layout.x[i];
            float gy = layout.y[i];

            float gx0 = gx + g.planeLeft * scale;
            float gy0 = gy + g.planeBottom * scale;

            float gx1 = gx + g.planeRight * scale;
            float gy1 = gy + g.planeTop * scale;

            appendGlyph(drawX + gx0, drawY + gy0, drawX + gx1, drawY + gy1,
                g.u0, g.v0, g.u1, g.v1,
                layout.color[i], italicSkew, style
            );
        }
    }

    private void appendGlyph(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, int rgba8888, float topSkew, float style) {
        float packedColor = MathUtil.packColor(rgba8888);

        vertexFloatCount = GlyphVertexEmitter.emitGlyph(vertices, vertexFloatCount, x0, y0, x1, y1, u0, v0, u1, v1, packedColor, topSkew, style);

        glyphCount++;
    }

    public void flush() {
        if (glyphCount == 0 || currentTexture == null || currentFont == null) {
            return;
        }

        currentTexture.bind(0);

        shader.bind();
        shader.setUniformMatrix("u_projTrans", projectionMatrix);
        shader.setUniformi("u_texture", 0);
        shader.setUniformf("u_pxRange", currentFont.getPxRange());

        mesh.setVertices(vertices, 0, vertexFloatCount);
        mesh.render(shader, GL20.GL_TRIANGLES, 0, glyphCount * INDICES_PER_GLYPH);

        vertexFloatCount = 0;
        glyphCount = 0;
    }

    public void end() {
        if(!drawing) throw new IllegalStateException("Must call begin() before end()");

        flush();
        drawing = false;
    }

    @Override
    public void dispose() {
        mesh.dispose();
    }

    private boolean isFlushRequired() {
        return glyphCount >= maxGlyphs || vertexFloatCount + FLOATS_PER_GLYPH > vertices.length;
    }

    private static short[] buildIndices(int glyphCapacity) {
        short[] indices = new short[glyphCapacity * INDICES_PER_GLYPH];

        int vertex = 0;
        int index = 0;

        for (int i = 0; i < glyphCapacity; i++) {
            indices[index++] = (short) vertex;
            indices[index++] = (short) (vertex + 1);
            indices[index++] = (short) (vertex + 2);

            indices[index++] = (short) (vertex + 2);
            indices[index++] = (short) (vertex + 3);
            indices[index++] = (short) vertex;

            vertex += 4;
        }

        return indices;
    }
}
