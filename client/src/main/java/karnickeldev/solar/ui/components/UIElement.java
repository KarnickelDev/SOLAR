package karnickeldev.solar.ui.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.util.MathUtil;

/**
 * @author KarnickelDev
 * @since 09.04.2026
 **/
public abstract class UIElement {

    private static final Color[] DEBUG_COLORS = {
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.YELLOW,
        Color.ORANGE,
        Color.MAGENTA
    };

    private static int ID = 0;

    private final int id;
    private final UILayout layout = new UILayout();

    private UIElement parent;

    private float x;
    private float y;
    private float width = 100;
    private float height = 100;

    protected float padLeft, padRight, padTop, padBottom;
    protected float cpLeft, cpRight, cpTop, cpBottom;

    protected float borderThickness;
    protected float cBorderThickness;

    private boolean visible = true;
    private boolean debug = false;

    private boolean touchable = true;

    private boolean layoutDirty = true;

    public UIElement() {
        this.id = ID++;
    }

    public abstract void act(float dt);

    public abstract void render(RendererContext ctx);

    public void renderDebug(RendererContext ctx) {
        if(!debug) return;

        ctx.debug().setColor(DEBUG_COLORS[id % DEBUG_COLORS.length]);
        ctx.debug().rect(getX(), getY(), getWidth(), getHeight());
    }

    public abstract boolean handleInput(InputEvent e);

    public UILayout getLayout() {
        return layout;
    }

    public void invalidateLayout() {
        layoutDirty = true;
    }

    public boolean isLayoutDirty() {
        return layoutDirty;
    }

    public void updateLayout(UILayoutEngine.UILayoutContext ctx) {
        if(!layoutDirty) return;

        UILayoutEngine.computeLayout(this, ctx);
        layoutDirty = false;
    }

    public boolean hit(float mx, float my) {
        float mouseY = Gdx.graphics.getHeight() - my;
        return MathUtil.AABB(mx, mouseY, getX(), getY(), getRight(), getTop());
    }

    public void setParent(UIElement parent) {
        this.parent = parent;
        invalidateLayout();
    }

    public UIElement getParent() {
        return parent;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setTouchable(boolean touchable) {
        this.touchable = touchable;
    }

    public boolean isTouchable() {
        return touchable;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getTop() {
        return y + height;
    }

    public float getRight() {
        return x + width;
    }

    public float getContentX() {
        return x + borderThickness + cpLeft;
    }

    public float getContentY() {
        return y + borderThickness + cpBottom;
    }

    public float getContentRight() {
        return getContentX() + getContentWidth();
    }

    public float getContentTop() {
        return getContentY() + getContentHeight();
    }

    public float getContentWidth() {
        return width - 2*cBorderThickness - cpLeft - cpRight;
    }

    public float getContentHeight() {
        return height - 2*cBorderThickness - cpTop - cpBottom;
    }

    public float getPreferredWidth(float scale) {
        return 100 * scale;
    }

    public float getPreferredHeight(float scale) {
        return 100 * scale;
    }

    public float getBorderThickness() {
        return cBorderThickness;
    }

    public void setBorderThickness(float borderThickness) {
        if(this.borderThickness == borderThickness) return;

        this.borderThickness = borderThickness;
        invalidateLayout();
    }

    public void setPadding(float padLeft, float padRight, float padTop, float padBottom) {
        if(this.padLeft == padLeft && this.padRight == padRight && this.padTop == padTop && this.padBottom == padBottom) {
            return;
        }

        this.padLeft = padLeft;
        this.padRight = padRight;
        this.padTop = padTop;
        this.padBottom = padBottom;

        invalidateLayout();
    }

    public void setPadding(float pad) {
        setPadding(pad, pad, pad, pad);
    }

    //############################ internal only #######################################################################

    void setBounds(float x, float y, float width, float height) {
        if(this.x == x && this.y == y && this.width == width && this.height == height) return;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    void updateMetrics(float uiScale) {
        cpLeft = padLeft * uiScale;
        cpRight = padRight * uiScale;
        cpTop = padTop * uiScale;
        cpBottom = padBottom * uiScale;

        cBorderThickness = borderThickness * uiScale;
    }

}
