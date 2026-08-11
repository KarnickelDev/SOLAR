package karnickeldev.solar.ui.components;

import karnickeldev.solar.render.core.RendererContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KarnickelDev
 * @since 15.06.2026
 **/
public class Canvas extends UIElement {

    public enum Anchor {
        TOP_LEFT, TOP_RIGHT,
        BOTTOM_LEFT, BOTTOM_RIGHT,
        CENTER,
        LEFT, RIGHT, TOP, BOTTOM
    }

    public static final class CanvasSlot {

        public Anchor anchor = Anchor.CENTER;

        public UILayout.SizeMode widthMode = UILayout.SizeMode.CONTENT;
        public UILayout.SizeMode heightMode = UILayout.SizeMode.CONTENT;

        public float widthValue;
        public float heightValue;

        public float offsetX;
        public float offsetY;

        public CanvasSlot fixedSize(float width, float height) {
            this.widthMode = UILayout.SizeMode.FIXED;
            this.heightMode = UILayout.SizeMode.FIXED;
            this.widthValue = width;
            this.heightValue = height;
            return this;
        }

        public CanvasSlot percentSize(float w, float h) {
            this.widthMode = UILayout.SizeMode.PERCENT;
            this.heightMode = UILayout.SizeMode.PERCENT;
            this.widthValue = w;
            this.heightValue = h;
            return this;
        }

        public CanvasSlot anchor(Anchor anchor) {
            this.anchor = anchor;
            return this;
        }

        public CanvasSlot offset(float dx, float dy) {
            this.offsetX = dx;
            this.offsetY = dy;
            return this;
        }

    }

    protected record CanvasEntry(UIElement child, CanvasSlot slot) {}

    private final List<CanvasEntry> entries = new ArrayList<>(4);

    public Canvas() {}

    public void add(UIElement child, CanvasSlot slot) {
        entries.add(new CanvasEntry(child, slot));
        child.setParent(this);
        child.invalidateLayout();
    }

    public void remove(UIElement child) {
        child.setParent(null);
        entries.removeIf(entry -> entry.child.equals(child));
    }

    @Override
    protected UIElement hitChildren(float mx, float my) {
        for (int i = entries.size() - 1; i >= 0; i--) {
            UIElement child = entries.get(i).child();

            UIElement hit = child.hit(mx, my);
            if(hit != null) return hit;
        }

        return null;
    }

    @Override
    public void invalidateLayout() {
        super.invalidateLayout();

        for(CanvasEntry entry : entries) {
            entry.child().invalidateLayout();
        }
    }

    @Override
    public void render(RendererContext ctx) {
    }

    @Override
    protected void renderChildren(RendererContext ctx) {
        for(CanvasEntry entry : entries) {
            entry.child.renderTree(ctx);
        }
    }

    @Override
    protected void renderDebugChildren(RendererContext ctx) {
        for(CanvasEntry entry : entries) {
            entry.child.renderDebugTree(ctx);
        }
    }

    @Override
    public void measure(UILayoutEngine.UILayoutContext ctx) {
        prefWidth = ctx.viewportWidth();
        prefHeight = ctx.viewportHeight();

        for(CanvasEntry entry : entries) {
            entry.child().measure(ctx);
        }
    }

    @Override
    public void act(float dt) {
        for(CanvasEntry entry : entries) {
            entry.child().act(dt);
        }
    }

    @Override
    public void arrange(UILayoutEngine.UILayoutContext ctx, float x, float y, float w, float h) {
        setBounds(x, y, w, h, ctx.uiScaleY());

        for(CanvasEntry entry : entries) {
            arrangeEntry(ctx, entry);
        }

        onLayout(ctx);
    }

    private void arrangeEntry(UILayoutEngine.UILayoutContext ctx, CanvasEntry entry) {
        UIElement child = entry.child();
        CanvasSlot slot = entry.slot();

        float scale = ctx.uiScaleY();

        float w = switch (slot.widthMode) {

            case FIXED -> slot.widthValue * scale;

            case PERCENT -> getContentWidth() * slot.widthValue;

            default -> child.getMeasuredWidth();
        };

        float h = switch (slot.heightMode) {

            case FIXED -> slot.heightValue * scale;

            case PERCENT -> getContentHeight() * slot.heightValue;

            default -> child.getMeasuredHeight();
        };

        float px = UIContainer.computeAnchorX(slot.anchor, getContentWidth(), w);
        float py = UIContainer.computeAnchorY(slot.anchor, getContentHeight(), h);

        child.arrange(ctx, getContentX() + px + slot.offsetX * scale, getContentY() + py + slot.offsetY * scale, w, h);
    }

}
