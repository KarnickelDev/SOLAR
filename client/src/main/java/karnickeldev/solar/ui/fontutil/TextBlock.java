package karnickeldev.solar.ui.fontutil;

import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.ui.fontutil.kernel.MSDFFont;
import karnickeldev.solar.ui.fontutil.kernel.TextLayout;

/**
 * @author KarnickelDev
 * @since 25.05.2026
 **/
public final class TextBlock {

    private RichText text;

    private final TextLayout layout;

    private float maxWidth = -1;

    private float uiScale = 1f;

    private int alignment = Align.left;

    private boolean dirty = true;

    public TextBlock(RichText text) {
        this.text = text;
        this.layout = new TextLayout();
    }

    public TextBlock() {
        this(RichText.empty());
    }

    public void setText(RichText text) {
        if(this.text == text) return;

        this.text = text;
        dirty = true;
    }

    public void setMaxWidth(float maxWidth) {
        if(this.maxWidth == maxWidth) return;
        this.maxWidth = maxWidth;
        dirty = true;
    }

    public void setUiScale(float uiScale) {
        if(this.uiScale == uiScale) return;
        this.uiScale = uiScale;
        dirty = true;
    }

    public void setAlignment(int alignment) {
        if(this.alignment == alignment) return;
        this.alignment = alignment;
        dirty = true;
    }

    public TextLayout layout(MSDFFont font, int align) {
        if(dirty) {
            rebuild(font, align);
        }
        return layout;
    }

    public TextLayout getLayout() {
        return layout;
    }

    public TextLayout layout(MSDFFont font) {
        return layout(font, alignment);
    }

    private void rebuild(MSDFFont font, int align) {
        layout.layout(font, text, align, maxWidth, uiScale);
        dirty = false;
    }

    public RichText getText() {
        return text;
    }
}
