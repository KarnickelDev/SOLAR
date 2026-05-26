package karnickeldev.solar.ui.core;

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

        ctx.shapes().setColor(DEBUG_COLORS[id % DEBUG_COLORS.length]);
        ctx.shapes().rect(getX(), getY(), getWidth(), getHeight());
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

    public void layout(float width, float height, float scale) {
        if(!layoutDirty) return;

        UILayoutEngine.apply(this, width, height, scale);
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

    // internal only
    void setBounds(float x, float y, float width, float height) {
        if(this.x == x && this.y == y && this.width == width && this.height == height) return;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        invalidateLayout();
    }

}
