package karnickeldev.solar.ui.fontutil;

import java.util.List;

/**
 * @author KarnickelDev
 * @since 20.04.2026
 **/
public final class TextLayout {

    public int glyphCount;

    // quad geometry per glyph
    public float[] x0;
    public float[] y0;
    public float[] x1;
    public float[] y1;

    // uv per glyph
    public float[] u0;
    public float[] v0;
    public float[] u1;
    public float[] v1;

    // styling
    public int[] colors;

    // line metadata
    public int lineCount;
    public int[] lineStartGlyphIndex;
    public int[] lineEndGlyphIndex;
    public float[] lineWidths;
    public float[] lineBaselineY;

    // overall metrics
    public float width;
    public float height;
    public float lineHeight;

    public void ensureCapacity(int glyphCapacity, int lineCapacity) {
        if (x0 == null || x0.length < glyphCapacity) {
            x0 = new float[glyphCapacity];
            y0 = new float[glyphCapacity];
            x1 = new float[glyphCapacity];
            y1 = new float[glyphCapacity];

            u0 = new float[glyphCapacity];
            v0 = new float[glyphCapacity];
            u1 = new float[glyphCapacity];
            v1 = new float[glyphCapacity];

            colors = new int[glyphCapacity];
        }

        if (lineStartGlyphIndex == null || lineStartGlyphIndex.length < lineCapacity) {
            lineStartGlyphIndex = new int[lineCapacity];
            lineEndGlyphIndex = new int[lineCapacity];
            lineWidths = new float[lineCapacity];
            lineBaselineY = new float[lineCapacity];
        }
    }

    public void clear() {
        glyphCount = 0;
        lineCount = 0;
        width = 0f;
        height = 0f;
        lineHeight = 0f;
    }

    public void layout(MSDFFont font, String text, float fontSize, float maxWidth) {
        TextLayoutEngine.layout(font, text, fontSize, maxWidth, this);
    }

    public void layout(MSDFFont font, List<TextSegment> segments, float fontSize, float maxWidth) {
        TextLayoutEngine.layout(font, segments, fontSize, maxWidth, this);
    }

    /**
     * Appends this TextLayout to a Mesh
     * @return Returns the new vertexOffset after appending, MESH MUST UPDATE INDEX TO THIS VALUE
     */
    public static int appendToVertexBuffer(TextLayout layout, float drawX, float drawY, float[] vertices, int vertexOffset) {
        int idx = vertexOffset;
        for (int i = 0; i < layout.glyphCount; i++) {
            float x0 = layout.x0[i] + drawX;
            float y0 = layout.y0[i] + drawY;
            float x1 = layout.x1[i] + drawX;
            float y1 = layout.y1[i] + drawY;

            float u0 = layout.u0[i];
            float v0 = layout.v0[i];
            float u1 = layout.u1[i];
            float v1 = layout.v1[i];

            float packedColor = Float.intBitsToFloat(Integer.reverseBytes(layout.colors[i]) & 0xfeffffff);

            vertices[idx++] = x0;
            vertices[idx++] = y0;
            vertices[idx++] = u0;
            vertices[idx++] = v0;
            vertices[idx++] = packedColor;

            vertices[idx++] = x1;
            vertices[idx++] = y0;
            vertices[idx++] = u1;
            vertices[idx++] = v0;
            vertices[idx++] = packedColor;

            vertices[idx++] = x1;
            vertices[idx++] = y1;
            vertices[idx++] = u1;
            vertices[idx++] = v1;
            vertices[idx++] = packedColor;

            vertices[idx++] = x0;
            vertices[idx++] = y1;
            vertices[idx++] = u0;
            vertices[idx++] = v1;
            vertices[idx++] = packedColor;
        }
        return idx;
    }

}
