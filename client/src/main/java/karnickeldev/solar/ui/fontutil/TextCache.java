package karnickeldev.solar.ui.fontutil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KarnickelDev
 * @since 19.04.2026
 **/
public final class TextCache {

    private final ArrayList<TextSegment> segments = new ArrayList<>(1);
    private final TextLayout layout = new TextLayout();

    private float fontSize;
    private float maxWidth = -1f;

    private boolean dirty = true;

    public TextCache(String text, float fontSize, int rgba8888) {
        this.fontSize = fontSize;
        this.segments.add(new TextSegment(text, rgba8888));
    }

    public TextCache(String text, float fontSize) {
        this(text, fontSize, 0xFFFFFFFF);
    }

    public TextCache(String text) {
        this(text, 12f);
    }

    public TextLayout layout() {
        return layout;
    }

    public List<TextSegment> segments() {
        return segments;
    }

    public void clearSegments() {
        segments.clear();
        dirty = true;
    }

    public void addSegment(String text, int rgba8888) {
        segments.add(new TextSegment(text, rgba8888));
        dirty = true;
    }

    public void setText(String text) {
        int color = segments.isEmpty() ? 0xFFFFFFFF : segments.getFirst().rgba();

        segments.clear();
        segments.add(new TextSegment(text, color));
        dirty = true;
    }

    public void setSegments(List<TextSegment> newSegments) {
        segments.clear();
        segments.addAll(newSegments);
        dirty = true;
    }

    public void setFontSize(float fontSize) {
        if (this.fontSize == fontSize) {
            return;
        }

        this.fontSize = fontSize;
        dirty = true;
    }

    public void setMaxWidth(float maxWidth) {
        if (this.maxWidth == maxWidth) {
            return;
        }

        this.maxWidth = maxWidth;
        dirty = true;
    }

    public void invalidate() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void rebuildIfNeeded(MSDFFont font) {
        if (!dirty) {
            return;
        }

        TextLayoutEngine.layout(font, segments, fontSize, maxWidth, layout);
        dirty = false;
    }
}
