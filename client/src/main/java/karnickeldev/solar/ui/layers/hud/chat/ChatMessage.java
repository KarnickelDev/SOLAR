package karnickeldev.solar.ui.layers.hud.chat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public final class ChatMessage {

    public enum Type {
        USER, SYSTEM, LOG,
    }

    public static final class ChatSegment {

        public final String text;
        public final int rgba8888;

        public ChatSegment(String text, int rgba8888) {
            this.text = text;
            this.rgba8888 = rgba8888;
        }

        public ChatSegment(String text, Color color) {
            this(text, Color.rgba8888(color));
        }
    }

    public static final class ChatLine {
        public final ArrayList<ChatSegment> segments = new ArrayList<>();
    }

    private final List<ChatSegment> segments;

    private final ArrayList<ChatLine> lines = new ArrayList<>();
    private float totalHeight;

    private boolean dirty = true;

    public ChatMessage(List<ChatSegment> segments) {
        this.segments = segments;
    }

    public List<ChatLine> getLines() {
        return lines;
    }

    public float getTotalHeight() {
        return totalHeight;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void invalidateLayout() {
        dirty = true;
    }

    public void layout(BitmapFont font, float maxWidth) {
        if (!dirty) return;

        lines.clear();
        totalHeight = 0;

        GlyphLayout layout = new GlyphLayout();
        float lineHeight = font.getLineHeight();

        ChatLine currentLine = new ChatLine();
        float lineWidth = 0;

        for (ChatSegment seg : segments) {
            String[] words = seg.text.split(" ");

            for (String word : words) {
                String candidate = word + " ";

                layout.setText(font, candidate);

                if (lineWidth + layout.width > maxWidth) {
                    lines.add(currentLine);
                    totalHeight += lineHeight;

                    currentLine = new ChatLine();
                    lineWidth = 0;
                }

                currentLine.segments.add(new ChatSegment(candidate, seg.rgba8888));
                lineWidth += layout.width;
            }
        }

        if (!currentLine.segments.isEmpty()) {
            lines.add(currentLine);
            totalHeight += lineHeight;
        }

        dirty = false;
    }
}
