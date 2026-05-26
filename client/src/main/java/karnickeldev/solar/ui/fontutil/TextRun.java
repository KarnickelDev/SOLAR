package karnickeldev.solar.ui.fontutil;

/**
 * @author KarnickelDev
 * @since 20.04.2026
 **/
public final class TextRun {

    String text;
    int rgba;
    float scale;
    byte flags;

    public TextRun(String text, int rgba, float scale, byte flags) {
        this.text = text;
        this.rgba = rgba;
        this.scale = scale;
        this.flags = flags;
    }

    public TextRun(String text, int rgba) {
        this(text, rgba, 15f, (byte) 0);
    }

    public TextRun(String text, int rgba, float scale, int flags) {
        this(text, rgba, scale, (byte) flags);
    }

    boolean isSameStyle(TextRun other) {
        return isSameStyle(other.rgba, other.scale, other.flags);
    }

    boolean isSameStyle(int color, float scale, byte flags) {
        return this.rgba == color && this.scale == scale && this.flags == flags;
    }

    public String text() {
        return text;
    }

    public int color() {
        return rgba;
    }

    public float scale() {
        return scale;
    }

    public byte flags() {
        return flags;
    }

    @Deprecated
    public void setText(String text) {
        this.text = text;
    }

    @Deprecated
    public void setColor(int rgba) {
        this.rgba = rgba;
    }

    @Deprecated
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Deprecated
    public void setFlags(byte flags) {
        this.flags = flags;
    }

}
