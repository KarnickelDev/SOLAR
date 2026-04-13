package karnickeldev.solar.ui.fontutil;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import karnickeldev.solar.ui.layers.hud.chat.ChatMessage;
import org.lwjgl.opengl.GL20;

/**
 * @author KarnickelDev
 * @since 19.04.2026
 **/
public final class MSDFBatch implements Disposable {

    private static final int FLOATS_PER_VERTEX = 5;
    private static final int VERTICES_PER_GLYPH = 4;
    private static final int INDICES_PER_GLYPH = 6;

    private final int maxGlyphs;

    private final Mesh mesh;
    private final ShaderProgram shader;
    private final float[] vertices;

    private final Matrix4 projectionMatrix = new Matrix4();

    private int vertexFloatCount;
    private int glyphCount;

    private Texture currentTexture;
    private MSDFFont currentFont;

    public MSDFBatch(int maxGlyphs, ShaderProgram shader) {
        this.maxGlyphs = maxGlyphs;
        this.shader = shader;

        this.vertices = new float[maxGlyphs * VERTICES_PER_GLYPH * FLOATS_PER_VERTEX];

        this.mesh = new Mesh(
            true,
            maxGlyphs * VERTICES_PER_GLYPH,
            maxGlyphs * INDICES_PER_GLYPH,
            new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"),
            new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color")
        );

        mesh.setIndices(buildIndices(maxGlyphs));
    }

    public void begin(Matrix4 projection) {
        projectionMatrix.set(projection);

        vertexFloatCount = 0;
        glyphCount = 0;

        currentTexture = null;
        currentFont = null;
    }

    public void drawMessage(MSDFFont font,
                            ChatMessage message,
                            float x,
                            float y,
                            float scale) {

        Texture texture = font.getAtlas();

        if (currentTexture != null && currentTexture != texture) {
            flush();
        }

        currentTexture = texture;
        currentFont = font;

        float lineHeight = font.getLineHeight() * scale;
        float messageHeight = message.lineCount * lineHeight;
        float drawY = y + messageHeight - lineHeight;

        for (int line = 0; line < message.lineCount; line++) {
            drawMessageLine(font, message, line, x, drawY, scale);
            drawY -= lineHeight;
        }
    }

    public void drawMessageLine(MSDFFont font, ChatMessage message, int line, float x, float y, float scale) {
        float cursorX = x;
        float advance = font.getSpaceAdvance() * scale;

        int startSegment = message.lineSegmentStart[line];
        int endSegment = message.lineSegmentEnd[line];



        for (int seg = startSegment; seg <= endSegment; seg++) {
            String text = message.segmentText[seg];
            int color = message.segmentColor[seg];

            int start = (seg == startSegment)
                ? message.lineCharStart[line]
                : 0;

            int end = (seg == endSegment)
                ? message.lineCharEnd[line]
                : text.length();

            for (int i = start; i < end;) {
                int cp = text.codePointAt(i);
                i += Character.charCount(cp);

                Glyph glyph = font.getGlyph(cp);

                float gx0 = cursorX + glyph.planeLeft * scale;
                float gy0 = y + glyph.planeBottom * scale;
                float gx1 = cursorX + glyph.planeRight * scale;
                float gy1 = y + glyph.planeTop * scale;

                if(!canAppendGlyph()) flush();

                appendGlyph(
                    gx0,
                    gy0,
                    gx1,
                    gy1,
                    glyph.u0,
                    glyph.v0,
                    glyph.u1,
                    glyph.v1,
                    color
                );

                cursorX += advance;
            }
        }
    }


    public void draw(MSDFFont font, TextLayout layout, float drawX, float drawY) {
        if (layout == null || layout.glyphCount == 0) {
            return;
        }

        Texture texture = font.getAtlas();

        if (currentTexture != null && currentTexture != texture) {
            flush();
        }

        currentTexture = texture;
        currentFont = font;

        for (int i = 0; i < layout.glyphCount; i++) {
            if (glyphCount >= maxGlyphs) {
                flush();
            }

            if(!canAppendGlyph()) flush();

            appendGlyph(
                layout.x0[i] + drawX,
                layout.y0[i] + drawY,
                layout.x1[i] + drawX,
                layout.y1[i] + drawY,
                layout.u0[i],
                layout.v0[i],
                layout.u1[i],
                layout.v1[i],
                layout.colors[i]
            );
        }
    }

    private void appendGlyph(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, int rgba8888) {
        float packedColor = packColor(rgba8888);

        int idx = vertexFloatCount;

        // bottom-left
        vertices[idx++] = x0;
        vertices[idx++] = y0;
        vertices[idx++] = u0;
        vertices[idx++] = v0;
        vertices[idx++] = packedColor;

        // bottom-right
        vertices[idx++] = x1;
        vertices[idx++] = y0;
        vertices[idx++] = u1;
        vertices[idx++] = v0;
        vertices[idx++] = packedColor;

        // top-right
        vertices[idx++] = x1;
        vertices[idx++] = y1;
        vertices[idx++] = u1;
        vertices[idx++] = v1;
        vertices[idx++] = packedColor;

        // top-left
        vertices[idx++] = x0;
        vertices[idx++] = y1;
        vertices[idx++] = u0;
        vertices[idx++] = v1;
        vertices[idx++] = packedColor;

        vertexFloatCount = idx;
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
        flush();
    }

    @Override
    public void dispose() {
        mesh.dispose();
    }

    private boolean canAppendGlyph() {
        return glyphCount + 1 <= maxGlyphs
            && vertexFloatCount + 20 <= vertices.length;
    }

    private static float packColor(int rgba8888) {
        return Float.intBitsToFloat(Integer.reverseBytes(rgba8888) & 0xfeffffff);
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
