package karnickeldev.solar.ui.fontutil.kernel;

import karnickeldev.solar.ui.fontutil.RichText;
import karnickeldev.solar.ui.fontutil.TextRun;

import java.util.Arrays;

/**
 * @author KarnickelDev
 * @since 20.04.2026
 **/
public final class TextLayout {

    int glyphCount;

    // per glyph (logical) position
    float[] x;
    float[] y;

    int[] color;
    byte[] flags;
    float scale;

    short[] glyphId;

    // line metadata
    int lineCount;
    int[] lineStart;
    int[] lineEnd;

    float lineHeight;
    float ascent;
    float descent;
    float[] lineBaseline;
    float[] lineWidth;

    // line offsets (for alignment)
    float[] lineOffsetX;

    // overall metrics
    float width;
    float height;

    float minX;
    float minY;
    float maxX;
    float maxY;

    public TextLayout() {
        this(32, 1);
    }

    public TextLayout(int initialGlyphCapacity, int initialLineCapacity) {
        allocateGlyphArrays(initialGlyphCapacity);
        allocateLineArrays(initialLineCapacity);
        clear();
    }

    public void clear() {
        glyphCount = 0;
        lineCount = 0;

        width = height = 0;
        scale = 1f;
        ascent = descent = lineHeight = 0f;

        minX = minY = Float.POSITIVE_INFINITY;
        maxX = maxY = Float.NEGATIVE_INFINITY;
    }

    public void layout(MSDFFont font, TextRun[] runs, int align, float maxWidth, float scale) {
        TextLayoutEngine.layout(font, runs, maxWidth, align, this, scale);
    }

    public void layout(MSDFFont font, RichText richText, int align, float maxWidth, float scale) {
        layout(font, richText.runs(), align, maxWidth, scale);
    }

    public float getLeft() {
        return minX;
    }

    public float getRight() {
        return maxX;
    }

    public float getBottom() {
        return minY;
    }

    public float getTop() {
        return maxY;
    }

    public float getLogicalWidth() {
        return width;
    }

    public float getLogicalHeight() {
        return height;
    }

    public float getBoundsWidth() {
        return getRight() - getLeft();
    }

    public float getBoundsHeight() {
        return getTop() - getBottom();
    }

    void setGlyph(int i, float x, float y, short glyphIndex, int color, byte flags) {
        ensureGlyphCapacity(i);

        this.x[i] = x;
        this.y[i] = y;

        this.glyphId[i] = glyphIndex;
        this.color[i] = color;
        this.flags[i] = flags;
    }

    void setLine(int i, int startGlyph, int endGlyph, float baseline, float width) {
        ensureLineCapacity(i);

        lineStart[i] = startGlyph;
        lineEnd[i] = endGlyph;
        lineBaseline[i] = baseline;
        lineWidth[i] = width;
    }

    int appendGlyph() {
        ensureGlyphCapacity(glyphCount + 1);
        return glyphCount++;
    }

    int appendLine() {
        ensureLineCapacity(lineCount + 1);
        return lineCount++;
    }

    //################################# HELPERS ########################################################################

    public boolean isEmpty() {
        return glyphCount == 0;
    }

    public int getLineCount() {
        return lineCount;
    }

    public float getLineWidth(int i) {
        return lineWidth[i];
    }

    public int getLineGlyphCount(int line) {
        return lineEnd[line] - lineStart[line];
    }

    public float getLineHeight() {
        return lineHeight;
    }

    //################################# CAPACITY MANAGEMENT ############################################################

    private void ensureGlyphCapacity(int required) {
        if(glyphId.length >= required) return;

        int newCap = Math.max(1, glyphId.length);
        while(newCap < required) newCap *= 2;

        x = Arrays.copyOf(x, newCap);
        y = Arrays.copyOf(y, newCap);

        color = Arrays.copyOf(color, newCap);
        flags = Arrays.copyOf(flags, newCap);
        glyphId = Arrays.copyOf(glyphId, newCap);
    }

    private void ensureLineCapacity(int required) {
        if (lineStart.length >= required) return;

        int newCap = Math.max(1, lineStart.length);
        while(newCap < required) newCap *= 2;

        lineStart = Arrays.copyOf(lineStart, newCap);
        lineEnd = Arrays.copyOf(lineEnd, newCap);
        lineBaseline = Arrays.copyOf(lineBaseline, newCap);
        lineWidth = Arrays.copyOf(lineWidth, newCap);

        lineOffsetX = Arrays.copyOf(lineOffsetX, newCap);
    }

    private void allocateGlyphArrays(int capacity) {
        x = new float[capacity];
        y = new float[capacity];

        color = new int[capacity];
        glyphId = new short[capacity];
        flags = new byte[capacity];
    }

    private void allocateLineArrays(int capacity) {
        lineStart = new int[capacity];
        lineEnd = new int[capacity];

        lineBaseline = new float[capacity];
        lineWidth = new float[capacity];

        lineOffsetX = new float[capacity];
    }

    //################################# DEBUG OUTPUT ###################################################################

    @Override
    public String toString() {
        return "GlyphLayout{glyphs=" + glyphCount + ", lines=" + lineCount + ", bounds=[" +
            minX + "," + minY + " -> " + maxX + "," + maxY + "]}";
    }

}
