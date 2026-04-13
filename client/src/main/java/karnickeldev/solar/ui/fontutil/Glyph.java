package karnickeldev.solar.ui.fontutil;

/**
 * @author KarnickelDev
 * @since 20.04.2026
 **/
public final class Glyph {

    public int index;
    public final int codepoint;

    public float advance;
    public float planeLeft;
    public float planeBottom;
    public float planeRight;
    public float planeTop;

    public float u0;
    public float v0;
    public float u1;
    public float v1;

    public short atlasPage;

    Glyph(int codepoint) {
        this.codepoint = codepoint;
    }

    public void setPlaneBounds(float left, float bottom, float right, float top) {
        this.planeLeft = left;
        this.planeBottom = bottom;
        this.planeRight = right;
        this.planeTop = top;
    }

    public void setAtlasBounds(float u0, float v0, float u1, float v1) {
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
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
