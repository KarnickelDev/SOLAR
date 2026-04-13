package karnickeldev.solar.ui.layers.hud.chat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import karnickeldev.solar.ui.core.UILayoutEngine;
import karnickeldev.solar.ui.fontutil.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public final class ChatMessage {

    // source content
    public String[] segmentText;
    public int[] segmentColor;
    public int segmentCount;

    // wrapped output
    public int[] lineSegmentStart;
    public int[] lineSegmentEnd;
    public int[] lineCharStart;
    public int[] lineCharEnd;
    public int lineCount;

    // cache state
    public int wrappedForColumns = -1;
    public float wrappedForScale = -1f;
    public boolean dirty = true;

    public ChatMessage(int initialSegmentCapacity, int initialLineCapacity) {
        segmentText = new String[Math.max(1, initialSegmentCapacity)];
        segmentColor = new int[Math.max(1, initialSegmentCapacity)];

        lineSegmentStart = new int[Math.max(1, initialLineCapacity)];
        lineSegmentEnd = new int[Math.max(1, initialLineCapacity)];
        lineCharStart = new int[Math.max(1, initialLineCapacity)];
        lineCharEnd = new int[Math.max(1, initialLineCapacity)];
    }

    public void addSegment(String text, int color) {
        ensureSegmentCapacity(segmentCount + 1);

        segmentText[segmentCount] = text;
        segmentColor[segmentCount] = color;
        segmentCount++;

        dirty = true;
    }

    public void clearSegments() {
        segmentCount = 0;
        lineCount = 0;
        dirty = true;
    }

    public int getLineCount() {
        return lineCount;
    }

    public int getTotalCharacterCount() {
        int total = 0;

        for (int i = 0; i < segmentCount; i++) {
            total += segmentText[i].length();
        }

        return total;
    }

    public float getHeight(MSDFFont font, float scale) {
        return lineCount * font.getLineHeight() * scale;
    }

    public void ensureLineCapacity(int required) {
        if (lineSegmentStart.length >= required) {
            return;
        }

        int newCapacity = lineSegmentStart.length;
        while (newCapacity < required) {
            newCapacity *= 2;
        }

        lineSegmentStart = Arrays.copyOf(lineSegmentStart, newCapacity);
        lineSegmentEnd = Arrays.copyOf(lineSegmentEnd, newCapacity);
        lineCharStart = Arrays.copyOf(lineCharStart, newCapacity);
        lineCharEnd = Arrays.copyOf(lineCharEnd, newCapacity);
    }

    private void ensureSegmentCapacity(int required) {
        if (segmentText.length >= required) {
            return;
        }

        int newCapacity = segmentText.length;
        while (newCapacity < required) {
            newCapacity *= 2;
        }

        segmentText = Arrays.copyOf(segmentText, newCapacity);
        segmentColor = Arrays.copyOf(segmentColor, newCapacity);
    }
}
