package karnickeldev.solar.ui.fontutil;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KarnickelDev
 * @since 25.05.2026
 **/
public final class RichTextBuilder {

    private final List<TextRun> runs = new ArrayList<>();

    private int color = 0xFFFFFFFF;
    private float scale = TextStyle.DEFAULT_FONT_SIZE;
    private byte flags = 0;

    public RichTextBuilder(float fontScale) {
        this.scale = fontScale;
    }

    public RichTextBuilder(float fontScale, byte flags) {
        this.scale = fontScale;
        this.flags = flags;
    }

    public RichTextBuilder() {}

    public RichTextBuilder text(String text) {
        if(text == null || text.isEmpty()) return this;

        append(new TextRun(text, color, scale, flags));
        return this;
    }

    public RichTextBuilder color(int rgba) {
        this.color = rgba;
        return this;
    }

    public RichTextBuilder color(Color color) {
        return color(Color.rgba8888(color));
    }

    public RichTextBuilder scale(float scale) {
        this.scale = scale;
        return this;
    }

    public RichTextBuilder flags(byte flags) {
        this.flags = flags;
        return this;
    }

    public RichTextBuilder bold() {
        flags |= TextStyle.BOLD;
        return this;
    }

    public RichTextBuilder italic() {
        flags |= TextStyle.ITALIC;
        return this;
    }

    public RichTextBuilder clearStyle() {
        flags = 0;
        return this;
    }

    public void append(TextRun run) {
        append(run.text, run.rgba, run.scale, run.flags);
    }

    public void append(String text, int color, float scale, byte flags) {
        int size = runs.size();

        if(size > 0) {
            TextRun last = runs.get(size - 1);

            if(last.isSameStyle(color, scale, flags)) {
                last.text += text;
                return;
            }
        }
        runs.add(new TextRun(text, color, scale, flags));
    }

    public RichText build() {
        int len = 0;

        for(TextRun run : runs) {
            len += run.text().length();
        }

        return new RichText(runs.toArray(TextRun[]::new), len);
    }

}
