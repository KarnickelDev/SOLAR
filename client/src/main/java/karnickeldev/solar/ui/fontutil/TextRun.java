package karnickeldev.solar.ui.fontutil;

/**
 * @author KarnickelDev
 * @since 20.04.2026
 **/
public final class TextRun {

    String text;
    int rgba;
    byte flags;

    public TextRun(String text, int rgba, byte flags) {
        this.text = text;
        this.rgba = rgba;
        this.flags = flags;
    }

    public TextRun(String text, int rgba) {
        this(text, rgba, (byte) 0);
    }

    public TextRun(String text, int rgba, int flags) {
        this(text, rgba, (byte) flags);
    }

    boolean isSameStyle(TextRun other) {
        return isSameStyle(other.rgba, other.flags);
    }

    boolean isSameStyle(int color, byte flags) {
        return this.rgba == color && this.flags == flags;
    }

    public String text() {
        return text;
    }

    public int color() {
        return rgba;
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
    public void setFlags(byte flags) {
        this.flags = flags;
    }

}
