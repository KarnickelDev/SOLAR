package karnickeldev.solar.ui.components.container;

import karnickeldev.solar.ui.components.UIContainer;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.components.UILayout;
import karnickeldev.solar.ui.components.UILayoutEngine;

/**
 * @author KarnickelDev
 * @since 31.05.2026
 **/
public class VerticalGroup extends UIContainer {

    public VerticalGroup() {}

    @Override
    public void measure(UILayoutEngine.UILayoutContext ctx) {
        float w = 0;
        float h = 0;

        for(Slot slot : children) {
            UIElement child = slot.child();
            child.measure(ctx);
            w = Math.max(w, child.getMeasuredWidth());
            h += child.getMeasuredHeight();
        }

        prefWidth = w;
        prefHeight = h;
    }

    @Override
    public void onLayout(UILayoutEngine.UILayoutContext ctx) {
        UILayout.AllocationItem[] items = new UILayout.AllocationItem[children.size()];

        for (int i = 0; i < children.size(); i++) {
            UIElement child = children.get(i).child();
            UILayout layout = children.get(i).layout();
            items[i] = new UILayout.AllocationItem(layout.getHeightMode(), layout.getHeightValue(), child.getMeasuredHeight());
        }

        float[] widths = UILayout.allocateSlices(items, getContentHeight(), ctx.uiScaleY());

        float cursor = 0f;

        for (int i = 0; i < children.size(); i++) {
            UIElement child = children.get(i).child();

            float cw = UILayoutEngine.resolveWidth(child, children.get(i).layout(), getContentWidth(), ctx.uiScaleY());
            child.arrange(ctx, getContentX(), getContentTop() - cursor - widths[i], cw, widths[i]);

            cursor += widths[i];
        }
    }

}
