package karnickeldev.solar.ui.fontutil.kernel;

import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.ui.fontutil.TextRun;

import java.util.Arrays;
import java.util.List;

/**
 * @author KarnickelDev
 * @since 20.04.2026
 **/
final class TextLayoutEngine {

    private static final int TAB_SIZE = 4;

    private static final int WORD_BUFFER_LENGTH = 64;

    private static final class WordBuffer {
        private int size;
        private int[] codepoints = new int[WORD_BUFFER_LENGTH];
        private float[] scales = new float[WORD_BUFFER_LENGTH];
        private int[] colors = new int[WORD_BUFFER_LENGTH];
        private byte[] flags = new byte[WORD_BUFFER_LENGTH];

        private void clear() {
            size = 0;
        }

        private void add(int cp, float scale, int color, byte flags) {
            if(size >= codepoints.length) {
                int newSize = size * 2;
                codepoints = Arrays.copyOf(codepoints, newSize);
                scales = Arrays.copyOf(scales, newSize);
                colors = Arrays.copyOf(colors, newSize);
                this.flags = Arrays.copyOf(this.flags, newSize);
            }

            codepoints[size] = cp;
            scales[size] = scale;
            colors[size] = color;
            this.flags[size] = flags;

            size++;
        }

        private float computeWidth(MSDFFont font) {
            float w = 0;
            for(int i = 0; i < size; i++) {
                Glyph g = font.getGlyph(codepoints[i]);
                w += g.advance * scales[i];
            }
            return w;
        }
    }

    private TextLayoutEngine() {}

    static void layout(MSDFFont font, List<TextRun> runs, float maxWidth, int align, TextLayout out, float uiScale) {
        layout(font, runs.toArray(TextRun[]::new), maxWidth, align, out, uiScale);
    }

    static void layout(MSDFFont font, TextRun[] segments, float maxWidth, int align, TextLayout out, float uiScale) {
        out.clear();

        if (segments == null || segments.length == 0) {
            return;
        }

        LayoutState state = new LayoutState();

        WordBuffer buffer = new WordBuffer();

        for (TextRun segment : segments) {
            if (segment.text() == null || segment.text().isEmpty()) {
                continue;
            }

            String text = segment.text();
            float scale = segment.scale() * uiScale;
            int color = segment.color();
            byte flags = segment.flags();

            state.maxScale = Math.max(state.maxScale, scale);

            for (int i = 0; i < text.length(); ) {
                int cp = text.codePointAt(i);
                i += Character.charCount(cp);

                if(cp == '\n') {
                    flushWord(font, buffer, state, out, maxWidth, true);
                    finishLine(out, state);

                    moveToNextLine(font, state, out.glyphCount);
                    continue;
                }

                if (cp == '\t') {
                    flushWord(font, buffer, state, out, maxWidth, false);

                    float advance = font.getSpaceAdvance() * scale;
                    float tabWidth = advance * TAB_SIZE;
                    state.penX = ((int)(state.penX / tabWidth) + 1) * tabWidth;;

                    state.lineWidth = state.penX;
                    state.prevCodepoint = -1;
                    continue;
                }

                boolean isBreak = (cp == ' ');
                if(isBreak) {
                    flushWord(font, buffer, state, out, maxWidth, false);
                    state.penX += font.getSpaceAdvance() * scale;
                    state.prevCodepoint = -1;
                    continue;
                }

                buffer.add(cp, scale, color, flags);
            }
        }

        flushWord(font, buffer, state, out, maxWidth, true);
        finishLine(out, state);
        finalizeLayout(out);

        // alignment
        for (int line = 0; line < out.lineCount; line++) {

            float lineWidth = out.lineWidth[line];

            float offset = switch (align) {
                case Align.left -> 0f;
                case Align.center -> (maxWidth - lineWidth) * 0.5f;
                case Align.right -> (maxWidth - lineWidth);
                default -> throw new IllegalStateException("Unexpected value: " + align);
            };

            int start = out.lineStart[line];
            int end = out.lineEnd[line];

            for (int i = start; i < end; i++) {
                out.x[i] += offset;
            }
        }
        recomputeBounds(out, font);
    }

    private static boolean exceedsLine(float penX, float width, float maxWidth) {
        return maxWidth > 0f && penX + width > maxWidth;
    }

    private static void recomputeBounds(TextLayout layout, MSDFFont font) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < layout.glyphCount; i++) {
            Glyph g = font.getGlyphUnsafe(layout.glyphId[i]);
            float scale = layout.scale[i];

            float x0 = layout.x[i] + g.planeLeft * scale;
            float y0 = layout.y[i] + g.planeBottom * scale;

            float x1 = layout.x[i] + g.planeRight * scale;
            float y1 = layout.y[i] + g.planeTop * scale;

            minX = Math.min(minX, x0);
            minY = Math.min(minY, y0);

            maxX = Math.max(maxX, x1);
            maxY = Math.max(maxY, y1);
        }

        layout.minX = minX;
        layout.minY = minY;
        layout.maxX = maxX;
        layout.maxY = maxY;
    }

    private static void flushWord(MSDFFont font, WordBuffer word, LayoutState state, TextLayout out, float maxWidth, boolean force) {
        if (word.size == 0) return;

        float wordWidth = word.computeWidth(font);

        // move word to next line if possible
        if (state.penX > 0f && !force && exceedsLine(state.penX, wordWidth, maxWidth)) {
            finishLine(out, state);
            moveToNextLine(font, state, out.glyphCount);
        }

        // still too large, hard-wrap
        if (force || exceedsLine(0f, wordWidth, maxWidth)) {
            flushHardWrappedWord(font, word, state, out, maxWidth);
            word.clear();
            return;
        }

        for (int i = 0; i < word.size; i++) {
            int cp = word.codepoints[i];
            float scale = word.scales[i];
            int color = word.colors[i];
            byte flags = word.flags[i];

            state.maxScale = Math.max(state.maxScale, scale);

            Glyph g = font.getGlyph(cp);

            state.lineAscent = Math.max(state.lineAscent, font.getAscent() * scale);
            state.lineDescent = Math.max(state.lineDescent, font.getDescent() * scale);

            applyKerning(font, state, cp, scale);

            emitGlyph(out, font, state, g, cp, scale, color, flags);

            state.penX += g.advance * scale;
            state.prevCodepoint = cp;
        }

        state.lineWidth = state.penX;

        word.clear();
    }

    private static void flushHardWrappedWord(MSDFFont font, WordBuffer word, LayoutState state, TextLayout out, float maxWidth) {
        for (int i = 0; i < word.size; i++) {

            int cp = word.codepoints[i];
            float scale = word.scales[i];
            int color = word.colors[i];
            byte flags = word.flags[i];

            state.maxScale = Math.max(state.maxScale, scale);

            Glyph g = font.getGlyph(cp);

            float advance = g.advance * scale;

            // wrap BEFORE emitting
            if (state.penX > 0f && exceedsLine(state.penX, advance, maxWidth)) {
                finishLine(out, state);
                moveToNextLine(font, state, out.glyphCount);
            }

            state.lineAscent = Math.max(state.lineAscent, font.getAscent() * scale);
            state.lineDescent = Math.max(state.lineDescent, font.getDescent() * scale);

            applyKerning(font, state, cp, scale);

            emitGlyph(out, font, state, g, cp, scale, color, flags);

            state.penX += advance;
            state.prevCodepoint = cp;
        }

        state.lineWidth = state.penX;
    }

    private static void emitGlyph(TextLayout out, MSDFFont font, LayoutState state, Glyph glyph, int cp, float scale, int color, byte flags) {
        int glyphIndex = out.appendGlyph();

        out.setGlyph(glyphIndex, state.penX, state.baselineY, scale, (short) font.getGlyphIndex(cp), color, flags);

        float gx0 = state.penX + glyph.planeLeft * scale;
        float gy0 = state.baselineY + glyph.planeBottom * scale;
        float gx1 = state.penX + glyph.planeRight * scale;
        float gy1 = state.baselineY + glyph.planeTop * scale;
    }

    private static void applyKerning(MSDFFont font, LayoutState state, int cp, float scale) {
        if (state.prevCodepoint != -1) {
            state.penX += font.getKerning(state.prevCodepoint, cp) * scale;
        }
    }

    //#################################### line handling ###############################################################

    private static void moveToNextLine(MSDFFont font, LayoutState state, int glyphIndex) {
        state.baselineY -= font.getLineHeight() * state.maxScale;

        state.penX = 0f;
        state.lineWidth = 0f;

        state.startGlyph = glyphIndex;
        state.prevCodepoint = -1;

        state.lineAscent = 0f;
        state.lineDescent = 0f;
        state.maxScale = 0f;
    }

    private static void finishLine(TextLayout out, LayoutState state) {
        if (out.glyphCount == state.startGlyph) return;

        int line = out.appendLine();

        out.setLine(
            line,
            state.startGlyph,
            out.glyphCount,
            state.baselineY,
            state.lineAscent,
            state.lineDescent,
            state.lineWidth
        );
    }

    private static void finalizeLayout(TextLayout out) {
        out.width = computeLogicalWidth(out);
        out.height = computeLogicalHeight(out);
    }

    //######################### helpers ################################################################################

    private static float computeLogicalWidth(TextLayout layout) {
        float max = 0f;
        for (int i = 0; i < layout.lineCount; i++) {
            max = Math.max(max, layout.lineWidth[i]);
        }
        return max;
    }

    private static float computeLogicalHeight(TextLayout layout) {
        float height = 0f;
        for (int i = 0; i < layout.lineCount; i++) {
            height += layout.lineAscent[i] + layout.lineDescent[i];
        }
        return height;
    }

    private static final class LayoutState {
        float penX;
        float baselineY;

        float lineAscent;
        float lineDescent;
        float lineWidth;
        float maxScale;

        int startGlyph;

        int prevCodepoint = -1;
    }
}
