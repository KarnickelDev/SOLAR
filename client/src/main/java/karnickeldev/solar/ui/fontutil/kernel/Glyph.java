package karnickeldev.solar.ui.fontutil.kernel;

/**
 * @author KarnickelDev
 * @since 20.04.2026
 **/
public final class Glyph {

    public final int codepoint;

    public final float advance;
    public final float planeLeft;
    public final float planeBottom;
    public final float planeRight;
    public final float planeTop;

    public final float u0;
    public final float v0;
    public final float u1;
    public final float v1;

    public final short atlasPage;

    Glyph(int codepoint, float advance, float planeLeft, float planeBottom, float planeRight, float planeTop,
          float u0, float v0, float u1, float v1, short atlasPage) {
        this.codepoint = codepoint;
        this.advance = advance;
        this.planeLeft = planeLeft;
        this.planeBottom = planeBottom;
        this.planeRight = planeRight;
        this.planeTop = planeTop;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.atlasPage = atlasPage;
    }

    public float getWidth() {
        return planeRight - planeLeft;
    }

    public float getHeight() {
        return planeTop - planeBottom;
    }

    @Override
    public String toString() {
        return "{" + codepoint + ": " + u0 + ", " + v0 + ", " + u1 + ", " + v1 + "}";
    }

}
