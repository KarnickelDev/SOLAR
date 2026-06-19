package karnickeldev.solar.ui.fontutil.kernel;

import karnickeldev.solar.ui.components.widgets.TextWidget;
import karnickeldev.solar.ui.fontutil.TextRun;

/**
 * @author KarnickelDev
 * @since 20.04.2026
 **/
final class TextLayoutEngine {

    private static final int TAB_SIZE = 4;

    private TextLayoutEngine() {}

    private static final class WordMetrics {
        float width;
        int endIndex;
        int lastCp;
    }

    static void layout(MSDFFont font, TextRun[] segments, float maxWidth, int align, TextLayout out, float scale) {
        out.clear();
        out.scale = scale;
        out.lineHeight = font.getLineHeight() * scale;
        out.ascent = font.getAscent() * scale;
        out.descent = font.getDescent() * scale;

        if (segments == null || segments.length == 0) {
            return;
        }

        LayoutState state = new LayoutState();
        WordMetrics metrics = new WordMetrics();

        for (TextRun run : segments) {
            String text = run.text();
            if (text == null || text.isEmpty()) continue;

            int i = 0;

            while (i < text.length()) {
                int cp = text.codePointAt(i);
                int charCount = Character.charCount(cp);

                if (cp == '\n') {
                    finishLine(out, state);
                    moveToNextLine(font, state, out.glyphCount, scale);
                    i += charCount;
                    continue;
                }

                if (cp == '\t') {
                    float advance = font.getSpaceAdvance() * scale * TAB_SIZE;
                    state.penX = ((int)(state.penX / advance) + 1) * advance;

                    state.prevCodepoint = -1;
                    i += charCount;
                    continue;
                }

                if (cp == ' ') {
                    float spaceAdvance = font.getSpaceAdvance() * scale;

                    // ALWAYS apply to geometry
                    state.penX += spaceAdvance;

                    // mark that last char was a break (affects kerning only)
                    state.prevCodepoint = -1;

                    i += charCount;
                    continue;
                }

                // WORD START
                int start = i;
                i = measureWord(font, text, i, scale, metrics);

                processWord(font, run, text, start, metrics, state, out, maxWidth);
            }
        }

        finishLine(out, state);
        finalizeDimensions(out);

        normalizeBounds(out);

        align(out, align);
    }

    private static void normalizeBounds(TextLayout out) {
        float offsetY = out.minY;
        for (int i = 0; i < out.glyphCount; i++) {
            out.y[i] -= offsetY;
        }
        out.minX = 0;
        out.minY = 0;
        out.maxY -= offsetY;
    }

    private static void align(TextLayout out, int align) {
        for(int i = 0; i < out.lineCount; i++) {
            float offsetX = TextWidget.contentAlignX(out.lineWidth[i], out.width, align);

            for(int g = out.lineStart[i]; g < out.lineEnd[i]; g++) {
                out.x[g] -= offsetX;
            }
        }
    }

    private static int measureWord(MSDFFont font, String text, int start, float scale, WordMetrics out) {
        float width = 0f;
        int prev = -1;

        int i = start;

        while (i < text.length()) {
            int cp = text.codePointAt(i);

            if (cp == ' ' || cp == '\n' || cp == '\t') break;

            Glyph g = font.getGlyph(cp);

            if (prev != -1) {
                width += font.getKerning(prev, cp) * scale;
            }

            width += g.advance * scale;

            prev = cp;
            i += Character.charCount(cp);
        }

        out.width = width;
        out.endIndex = i;
        out.lastCp = prev;

        return i;
    }

    private static void processWord(MSDFFont font, TextRun run, String text, int start, WordMetrics metrics, LayoutState state, TextLayout out, float maxWidth) {
        // wrap check (geometry already includes spaces)
        if (state.penX > 0f && maxWidth > 0f && state.penX + metrics.width > maxWidth) {
            finishLine(out, state);
            moveToNextLine(font, state, out.glyphCount, out.scale);
        }

        emitWord(font, out, state, text, start, metrics.endIndex, run.color(), run.flags());
    }

    private static void emitWord(MSDFFont font, TextLayout out, LayoutState state, String text, int start, int end, int color, byte flags) {
        int prev = state.prevCodepoint;

        int i = start;

        while (i < end) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);

            if (prev != -1) state.penX += font.getKerning(prev, cp) * out.scale;

            Glyph g = font.getGlyph(cp);

            int idx = out.appendGlyph();

            emitGlyph(out, idx, state.penX, state.baselineY, (short) font.getGlyphIndex(cp), g, color, flags);

            state.penX += g.advance * out.scale;
            prev = cp;
        }

        state.prevCodepoint = prev;
        state.lineWidth = state.penX;
    }

    private static void emitGlyph(TextLayout out, int idx, float penX, float baselineY, short glyphIdx, Glyph g, int color, byte flags) {
        out.setGlyph(idx, penX, baselineY, glyphIdx, color, flags);

        float x0 = out.x[idx] + g.planeLeft * out.scale;
        float y0 = out.y[idx] + g.planeBottom * out.scale;

        float x1 = out.x[idx] + g.planeRight * out.scale;
        float y1 = out.y[idx] + g.planeTop * out.scale;

        out.minX = Math.min(out.minX, x0);
        out.minY = Math.min(out.minY, y0);

        out.maxX = Math.max(out.maxX, x1);
        out.maxY = Math.max(out.maxY, y1);
    }

    //#################################### line handling ###############################################################

    private static void moveToNextLine(MSDFFont font, LayoutState state, int glyphIndex, float scale) {
        state.baselineY -= font.getLineHeight() * scale;

        state.penX = 0f;
        state.lineWidth = 0f;

        state.startGlyph = glyphIndex;
        state.prevCodepoint = -1;
    }

    private static void finishLine(TextLayout out, LayoutState state) {
        if (out.glyphCount == state.startGlyph) return;
        out.setLine(out.appendLine(), state.startGlyph, out.glyphCount, state.baselineY, state.lineWidth);
    }

    private static void finalizeDimensions(TextLayout out) {
        out.height = out.lineHeight * out.lineCount;

        out.width = 0f;
        for (int i = 0; i < out.lineCount; i++) {
            out.width = Math.max(out.width, out.lineWidth[i]);
        }
    }

    private static final class LayoutState {
        float penX;
        float baselineY;

        float lineWidth;

        int startGlyph;

        int prevCodepoint = -1;
    }
}
