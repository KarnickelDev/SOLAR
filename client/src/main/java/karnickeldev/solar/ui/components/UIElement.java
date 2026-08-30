package karnickeldev.solar.ui.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.core.UIManager;
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

    protected UIElement parent;

    private float x;
    private float y;
    private float width = 100;
    private float height = 100;

    protected float prefWidth = 100;
    protected float prefHeight = 100;

    protected float padLeft, padRight, padTop, padBottom;
    protected float cpLeft, cpRight, cpTop, cpBottom;

    protected float borderThickness;
    protected float cBorderThickness;

    private boolean visible = true;
    private boolean enabled = true;
    private boolean active = true;
    private boolean touchable = false;
    private boolean disposed = false;

    private boolean debug = false;

    protected boolean layoutDirty = true;

    public UIElement() {
        this.id = ID++;
    }

    public final void act(float dt) {
        if(isEffectivelyActive()) onAct(dt);
    }

    protected abstract void onAct(float dt);

    protected abstract void render(RendererContext ctx);

    public final void renderTree(RendererContext ctx) {
        if(!visible) return;

        render(ctx);
        renderChildren(ctx);
    }

    protected void renderChildren(RendererContext ctx) {}

    protected void renderDebug(RendererContext ctx) {
        if(!debug) return;

        ctx.debug().setColor(DEBUG_COLORS[id % DEBUG_COLORS.length]);
        ctx.debug().rect(getX(), getY(), getWidth(), getHeight());
    }

    public final void renderDebugTree(RendererContext ctx) {
        if(!visible) return;

        renderDebug(ctx);
        renderDebugChildren(ctx);
    }

    protected void renderDebugChildren(RendererContext ctx) {}

    public void invalidateLayout() {
        layoutDirty = true;
    }

    protected void onLayout(UILayoutEngine.UILayoutContext ctx) {}

    public abstract void measure(UILayoutEngine.UILayoutContext ctx);

    public final float getMeasuredWidth() {
        return prefWidth;
    }

    public final float getMeasuredHeight() {
        return prefHeight;
    }

    public void arrange(UILayoutEngine.UILayoutContext ctx, float x, float y, float width, float height) {
        setBounds(x, y, width, height, ctx.uiScaleY());
        onLayout(ctx);
    }

    public final UIElement hit(float mx, float my) {
        if(!visible || !isEffectivelyEnabled() || !contains(mx, my)) return null;

        UIElement child = hitChildren(mx, my);
        if(child != null) return child;

        return touchable ? hitSelf(mx, my) : null;
    }

    protected UIElement hitChildren(float mx, float my) {
        return null;
    }

    protected UIElement hitSelf(float mx, float my) {
        return this;
    }

    private boolean contains(float mx, float my) {
        float mouseY = Gdx.graphics.getHeight() - my;
        return MathUtil.AABB(mx, mouseY, getX(), getY(), getRight(), getTop());
    }

    /** sets the parent UIElement. Cannot overwrite existing parent and avoids cycles */
    final void setParent(UIElement parent) {
        if(parent != null) {
            if(this.parent != null) {
                throw new IllegalStateException("UIElement already has a parent");
            }

            for(UIElement ancestor = parent; ancestor != null; ancestor = ancestor.parent) {
                if(ancestor == this) {
                    throw new IllegalArgumentException("A UIElement cannot be added below itself");
                }
            }

            this.parent = parent;
        } else {
            if(this.parent == null) return;

            // Detach first so interaction callbacks cannot recursively remove this element again.
            this.parent = null;
            UIManager.get().clearInteraction(this, true);
        }
        invalidateLayout();
    }

    public final UIElement getParent() {
        return parent;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public final void setEnabled(boolean enabled) {
        if(this.enabled == enabled) return;

        this.enabled = enabled;
        if(!enabled) UIManager.get().clearInteraction(this, true);
    }

    public final void setVisible(boolean visible) {
        if(this.visible == visible) return;

        this.visible = visible;
        if(!visible) UIManager.get().clearInteraction(this, true);
    }

    public final void setTouchable(boolean touchable) {
        if(this.touchable == touchable) return;

        this.touchable = touchable;
        if(!touchable) UIManager.get().clearInteraction(this, false);
    }

    public final void setActive(boolean active) {
        if(this.active == active) return;

        this.active = active;
        if(!active) UIManager.get().clearInteraction(this, true);
    }

    public final boolean isEnabled() {
        return enabled;
    }

    final boolean isEffectivelyEnabled() {
        return enabled && (parent == null || parent.isEffectivelyEnabled());
    }

    public final boolean isVisible() {
        return visible;
    }

    public final boolean isTouchable() {
        return touchable;
    }

    public final boolean isActive() {
        return active;
    }

    final boolean isEffectivelyActive() {
        return active && (parent == null || parent.isEffectivelyActive());
    }

    public final boolean isDisposed() {
        return disposed;
    }

    public final boolean isDebug() {
        return debug;
    }

    public final float getX() {
        return x;
    }

    public final float getY() {
        return y;
    }

    public final float getWidth() {
        return width;
    }

    public final float getHeight() {
        return height;
    }

    public final float getTop() {
        return y + height;
    }

    public final float getRight() {
        return x + width;
    }

    public final float getContentX() {
        return x + cBorderThickness + cpLeft;
    }

    public final float getContentY() {
        return y + cBorderThickness + cpBottom;
    }

    public final float getContentRight() {
        return getContentX() + getContentWidth();
    }

    public final float getContentTop() {
        return getContentY() + getContentHeight();
    }

    public final float getContentWidth() {
        return Math.max(0, width - 2*cBorderThickness - cpLeft - cpRight);
    }

    public final float getContentHeight() {
        return Math.max(0, height - 2*cBorderThickness - cpTop - cpBottom);
    }

    public final float getBorderThickness() {
        return cBorderThickness;
    }

    public final void setBorderThickness(float borderThickness) {
        if(borderThickness < 0) throw new IllegalArgumentException("borderThickness cannot be negative");
        if(this.borderThickness == borderThickness) return;

        this.borderThickness = borderThickness;
        invalidateLayout();
    }

    public final void setPadding(float padLeft, float padRight, float padTop, float padBottom) {
        if(padLeft < 0) throw new IllegalArgumentException("padLeft cannot be negative");
        if(padRight < 0) throw new IllegalArgumentException("padRight cannot be negative");
        if(padTop < 0) throw new IllegalArgumentException("padTop cannot be negative");
        if(padBottom < 0) throw new IllegalArgumentException("padBottom cannot be negative");
        if(this.padLeft == padLeft && this.padRight == padRight && this.padTop == padTop && this.padBottom == padBottom) {
            return;
        }

        this.padLeft = padLeft;
        this.padRight = padRight;
        this.padTop = padTop;
        this.padBottom = padBottom;

        invalidateLayout();
    }

    public final void setPadding(float pad) {
        setPadding(pad, pad, pad, pad);
    }

    //############################ internal only #######################################################################

    void setBounds(float x, float y, float width, float height, float uiScale) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        cpLeft = padLeft * uiScale;
        cpRight = padRight * uiScale;
        cpTop = padTop * uiScale;
        cpBottom = padBottom * uiScale;
        cBorderThickness = borderThickness * uiScale;
    }

}
