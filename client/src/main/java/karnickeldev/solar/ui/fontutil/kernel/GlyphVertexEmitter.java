package karnickeldev.solar.ui.fontutil.kernel;

/**
 * @author KarnickelDev
 * @since 21.05.2026
 **/
final class GlyphVertexEmitter {

    private GlyphVertexEmitter() {}

    static int emitGlyph(float[] vertices, int offset,
                         float x0, float y0, float x1, float y1,
                         float u0, float v0, float u1, float v1,
                         float packedColor, float topSkew, float style) {

        // bottom-left
        vertices[offset++] = x0;
        vertices[offset++] = y0;
        vertices[offset++] = u0;
        vertices[offset++] = v0;
        vertices[offset++] = packedColor;
        vertices[offset++] = style;

        // bottom-right
        vertices[offset++] = x1;
        vertices[offset++] = y0;
        vertices[offset++] = u1;
        vertices[offset++] = v0;
        vertices[offset++] = packedColor;
        vertices[offset++] = style;

        // top-right
        vertices[offset++] = x1 + topSkew;
        vertices[offset++] = y1;
        vertices[offset++] = u1;
        vertices[offset++] = v1;
        vertices[offset++] = packedColor;
        vertices[offset++] = style;

        // top-left
        vertices[offset++] = x0 + topSkew;
        vertices[offset++] = y1;
        vertices[offset++] = u0;
        vertices[offset++] = v1;
        vertices[offset++] = packedColor;
        vertices[offset++] = style;

        return offset;
    }

}
