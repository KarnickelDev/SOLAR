package karnickeldev.solar.ui.components;

import karnickeldev.solar.render.core.RendererContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author KarnickelDev
 * @since 10.04.2026
 **/
public abstract class UIContainer extends UIElement {

    protected record Slot(UIElement child, UILayout layout) {}

    protected final List<Slot> children = new ArrayList<>(4);

    public UIContainer() {}

    public final boolean contains(UIElement element) {
        for(Slot slot : children) {
            if(slot.child.equals(element)) return true;
        }
        return false;
    }

    public final void add(UIElement child) {
        add(child, new UILayout());
    }

    public final void add(UIElement child, UILayout layout) {
        Objects.requireNonNull(child, "child");
        Objects.requireNonNull(layout, "layout");
        if(child.getParent() != null) throw new IllegalArgumentException("element already has parent");
        if(contains(child)) throw new IllegalArgumentException("element is already a child of this container");
        if(child == this) throw new IllegalArgumentException("cannot add element as its own child");

        child.setParent(this);
        children.add(new Slot(child, layout));
        child.invalidateLayout();
        invalidateLayout();
    }

    public final void remove(UIElement child) {
        int index = indexOf(child);
        if(index < 0) return;
        if(child.getParent() != this) {
            throw new IllegalStateException("UI tree ownership is inconsistent");
        }

        children.remove(index);
        child.setParent(null);
        invalidateLayout();
    }

    protected final int indexOf(UIElement child) {
        for(int i = 0; i < children.size(); i++) {
            if(children.get(i).child() == child) return i;
        }
        return -1;
    }

    @Override
    public void measure(UILayoutEngine.UILayoutContext ctx) {
        float w = 0;
        float h = 0;

        for (Slot s : children) {

            s.child.measure(ctx);

            w = Math.max(w, s.child.getMeasuredWidth());
            h = Math.max(h, s.child.getMeasuredHeight());
        }

        prefWidth = w + (padLeft + padRight + 2*borderThickness) * ctx.uiScaleY();
        prefHeight = h + (padTop + padBottom + 2*borderThickness) * ctx.uiScaleY();
    }

    @Override
    public void onLayout(UILayoutEngine.UILayoutContext ctx) {
        for (Slot s : children) {
            s.child.arrange(ctx, getContentX(), getContentY(), getContentWidth(), getContentHeight());
        }
    }

    public static float computeAnchorX(Canvas.Anchor anchor, float baseW, float w) {
        return switch (anchor) {
            case LEFT, TOP_LEFT, BOTTOM_LEFT -> 0;
            case RIGHT, TOP_RIGHT, BOTTOM_RIGHT -> baseW - w;
            case CENTER, TOP, BOTTOM -> (baseW - w) * 0.5f;
        };
    }

    public static float computeAnchorY(Canvas.Anchor anchor, float baseH, float h) {
        return switch (anchor) {
            case BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT -> 0;
            case TOP, TOP_LEFT, TOP_RIGHT -> baseH - h;
            case CENTER, LEFT, RIGHT -> (baseH - h) * 0.5f;
        };
    }

    @Override
    protected UIElement hitChildren(float mx, float my) {
        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement child = children.get(i).child();

            UIElement hit = child.hit(mx, my);
            if(hit != null) return hit;
        }

        return null;
    }


    @Override
    protected void onAct(float dt) {
        for(Slot child : children) {
            child.child.act(dt);
        }
    }

    @Override
    public void render(RendererContext ctx) {
    }

    @Override
    protected void renderChildren(RendererContext ctx) {
        for(Slot child : children) {
            child.child.renderTree(ctx);
        }
    }

    @Override
    public void invalidateLayout() {
        super.invalidateLayout();

        for(Slot child : children) {
            child.child.invalidateLayout();
        }
    }

    @Override
    public void setDebug(boolean debug) {
        super.setDebug(debug);
        for(Slot child : children) {
            child.child.setDebug(debug);
        }
    }

    @Override
    protected void renderDebugChildren(RendererContext ctx) {
        for(Slot child : children) {
            child.child.renderDebugTree(ctx);
        }
    }

}
