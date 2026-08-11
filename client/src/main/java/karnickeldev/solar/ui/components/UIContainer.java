package karnickeldev.solar.ui.components;

import karnickeldev.solar.render.core.RendererContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KarnickelDev
 * @since 10.04.2026
 **/
public abstract class UIContainer extends UIElement {

    protected record Slot(UIElement child, UILayout layout) {}

    protected final List<Slot> children = new ArrayList<>(4);

    public UIContainer() {}

    public void add(UIElement child) {
        add(child, new UILayout());
    }

    public void add(UIElement child, UILayout layout) {
        children.add(new Slot(child, layout));
        child.setParent(this);
        child.invalidateLayout();
    }

    public void remove(UIElement child) {
        child.setParent(null);
        children.removeIf(slot -> slot.child.equals(child));
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

        prefWidth = w;
        prefHeight = h;
    }

    @Override
    public void arrange(UILayoutEngine.UILayoutContext ctx, float x, float y, float w, float h) {
        setBounds(x, y, w, h, ctx.uiScaleY());

        for (Slot s : children) {
            s.child.arrange(ctx, getContentX(), getContentY(), getContentWidth(), getContentHeight());
        }

        onLayout(ctx);
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
    public void act(float dt) {
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
