package karnickeldev.solar.ui.fontutil;

import java.util.Arrays;
import java.util.List;

/**
 * @author KarnickelDev
 * @since 20.04.2026
 **/
public final class TextLayoutEngine {

    private TextLayoutEngine() {}

    public static void layout(MSDFFont font, String text, float fontSize, float maxWidth, TextLayout out) {
        layout(font, List.of(new TextSegment(text, 0xFFFFFFFF)), fontSize, maxWidth, out);
    }

    public static void layout(MSDFFont font, List<TextSegment> segments, float fontSize, float maxWidth, TextLayout out) {
        out.clear();

        if (segments == null || segments.isEmpty()) {
            return;
        }

        int estimatedGlyphs = estimateGlyphCount(segments);
        int estimatedLines = Math.max(4, estimatedGlyphs / 12);
        out.ensureCapacity(estimatedGlyphs, estimatedLines);

        float scaledLineHeight = font.getLineHeight() * fontSize;
        out.lineHeight = scaledLineHeight;

        float penX = 0f;
        float baselineY = 0f;

        int lineIndex = 0;
        int lineStartGlyph = 0;

        int prevCodepoint = -1;

        int lastBreakGlyph = -1;
        float widthAtBreak = 0f;

        float globalMinX = Float.POSITIVE_INFINITY;
        float globalMinY = Float.POSITIVE_INFINITY;
        float globalMaxX = Float.NEGATIVE_INFINITY;
        float globalMaxY = Float.NEGATIVE_INFINITY;

        for (TextSegment segment : segments) {
            String text = segment.text();
            int color = segment.rgba();

            for (int i = 0; i < text.length();) {
                int cp = text.codePointAt(i);
                int charCount = Character.charCount(cp);
                i += charCount;

                if (cp == '\n') {
                    finishLine(out, lineIndex, lineStartGlyph, penX, baselineY);

                    lineIndex++;
                    lineStartGlyph = out.glyphCount;

                    penX = 0f;
                    baselineY -= scaledLineHeight;

                    prevCodepoint = -1;
                    lastBreakGlyph = -1;
                    widthAtBreak = 0f;
                    continue;
                }

                Glyph glyph = font.getGlyph(cp);
                if (glyph == null) {
                    prevCodepoint = -1;
                    continue;
                }

                float kerning = prevCodepoint != -1 ? font.getKerning(prevCodepoint, cp) * fontSize : 0f;

                float advance = glyph.advance * fontSize;
                float nextPenX = penX + kerning;
                float glyphRightEdge = nextPenX + glyph.planeRight * fontSize;

                boolean overflow = maxWidth > 0f && glyphRightEdge > maxWidth && out.glyphCount > lineStartGlyph;

                if (overflow) {
                    if (lastBreakGlyph >= lineStartGlyph) {
                        int moveStart = lastBreakGlyph + 1;

                        finishLine(out, lineIndex, lineStartGlyph, widthAtBreak, baselineY);

                        lineIndex++;
                        baselineY -= scaledLineHeight;

                        shiftGlyphsToNewLine(out, moveStart, out.glyphCount, widthAtBreak, baselineY);

                        lineStartGlyph = moveStart;
                        penX = recomputeLineWidth(out, moveStart, out.glyphCount);
                    } else {
                        finishLine(out, lineIndex, lineStartGlyph, penX, baselineY);

                        lineIndex++;
                        lineStartGlyph = out.glyphCount;

                        penX = 0f;
                        baselineY -= scaledLineHeight;
                    }

                    prevCodepoint = -1;
                    lastBreakGlyph = -1;
                    widthAtBreak = 0f;

                    kerning = 0f;
                    nextPenX = penX;
                }

                float gx0 = nextPenX + glyph.planeLeft * fontSize;
                float gy0 = baselineY + glyph.planeBottom * fontSize;
                float gx1 = nextPenX + glyph.planeRight * fontSize;
                float gy1 = baselineY + glyph.planeTop * fontSize;

                int glyphIndex = out.glyphCount++;

                out.x0[glyphIndex] = gx0;
                out.y0[glyphIndex] = gy0;
                out.x1[glyphIndex] = gx1;
                out.y1[glyphIndex] = gy1;

                out.u0[glyphIndex] = glyph.u0;
                out.v0[glyphIndex] = glyph.v0;
                out.u1[glyphIndex] = glyph.u1;
                out.v1[glyphIndex] = glyph.v1;

                out.colors[glyphIndex] = color;

                globalMinX = Math.min(globalMinX, gx0);
                globalMinY = Math.min(globalMinY, gy0);
                globalMaxX = Math.max(globalMaxX, gx1);
                globalMaxY = Math.max(globalMaxY, gy1);

                penX = nextPenX + advance;
                prevCodepoint = cp;

                if (Character.isWhitespace(cp)) {
                    lastBreakGlyph = glyphIndex;
                    widthAtBreak = penX;
                }
            }
        }

        finishLine(out, lineIndex, lineStartGlyph, penX, baselineY);

        if (out.glyphCount == 0) {
            out.width = 0f;
            out.height = scaledLineHeight;
            return;
        }

        float offsetX = -globalMinX;
        float offsetY = -globalMinY;

        for (int i = 0; i < out.glyphCount; i++) {
            out.x0[i] += offsetX;
            out.x1[i] += offsetX;
            out.y0[i] += offsetY;
            out.y1[i] += offsetY;
        }

        for (int i = 0; i < out.lineCount; i++) {
            out.lineBaselineY[i] += offsetY;
        }

        out.width = globalMaxX - globalMinX;
        out.height = globalMaxY - globalMinY;
    }

    private static void finishLine(TextLayout out, int lineIndex, int glyphStart, float width, float baselineY) {
        ensureLineCapacity(out, lineIndex + 1);

        out.lineStartGlyphIndex[lineIndex] = glyphStart;
        out.lineEndGlyphIndex[lineIndex] = out.glyphCount;
        out.lineWidths[lineIndex] = width;
        out.lineBaselineY[lineIndex] = baselineY;
        out.lineCount = Math.max(out.lineCount, lineIndex + 1);
    }

    private static void shiftGlyphsToNewLine(TextLayout out, int startGlyph, int endGlyph, float oldOffsetX, float newBaselineY) {
        if (startGlyph >= endGlyph) {
            return;
        }

        float oldBaselineY = out.y0[startGlyph];
        float deltaY = newBaselineY - oldBaselineY;

        for (int i = startGlyph; i < endGlyph; i++) {
            out.x0[i] -= oldOffsetX;
            out.x1[i] -= oldOffsetX;

            out.y0[i] += deltaY;
            out.y1[i] += deltaY;
        }
    }

    private static float recomputeLineWidth(TextLayout out, int startGlyph, int endGlyph) {
        float maxX = 0f;

        for (int i = startGlyph; i < endGlyph; i++) {
            maxX = Math.max(maxX, out.x1[i]);
        }

        return maxX;
    }

    private static int estimateGlyphCount(List<TextSegment> segments) {
        int count = 0;

        for (TextSegment seg : segments) {
            count += seg.text().codePointCount(0, seg.text().length());
        }

        return Math.max(count, 1);
    }

    private static void ensureLineCapacity(TextLayout out, int requiredLines) {
        if (out.lineStartGlyphIndex.length >= requiredLines) {
            return;
        }

        int newCapacity = Math.max(4, out.lineStartGlyphIndex.length);

        while (newCapacity < requiredLines) {
            newCapacity *= 2;
        }

        out.lineStartGlyphIndex = Arrays.copyOf(out.lineStartGlyphIndex, newCapacity);
        out.lineEndGlyphIndex = Arrays.copyOf(out.lineEndGlyphIndex, newCapacity);
        out.lineWidths = Arrays.copyOf(out.lineWidths, newCapacity);
        out.lineBaselineY = Arrays.copyOf(out.lineBaselineY, newCapacity);
    }
}
