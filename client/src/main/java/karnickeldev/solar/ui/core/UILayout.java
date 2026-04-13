package karnickeldev.solar.ui.core;

/**
 * @author KarnickelDev
 * @since 10.04.2026
 **/
public class UILayout {

    public enum Anchor {
        TOP_LEFT, TOP_RIGHT,
        BOTTOM_LEFT, BOTTOM_RIGHT,
        CENTER,
        LEFT, RIGHT, TOP, BOTTOM
    }

    public Anchor anchor = Anchor.BOTTOM_LEFT;

    public boolean scaleXWithHeight = true;

    // margins
    public float offsetX = 0;
    public float offsetY = 0;

    // sizing modes
    public float widthPercent = -1;
    public float heightPercent = -1;

    public float fixedWidth = 100;
    public float fixedHeight = 100;

}
