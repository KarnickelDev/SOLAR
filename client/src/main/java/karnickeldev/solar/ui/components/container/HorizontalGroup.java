package karnickeldev.solar.ui.components.container;

import karnickeldev.solar.ui.components.UIContainer;
import karnickeldev.solar.ui.components.UIElement;
import karnickeldev.solar.ui.components.UILayout;
import karnickeldev.solar.ui.components.UILayoutEngine;

/**
 * @author KarnickelDev
 * @since 07.06.2026
 **/
public class HorizontalGroup extends UIContainer {

    @Override
    public void measure(UILayoutEngine.UILayoutContext ctx) {
        float w = 0;
        float h = 0;

        for(Slot slot : children) {
            UIElement child = slot.child();
            child.measure(ctx);
            h = Math.max(h, child.getMeasuredHeight());
            w += child.getMeasuredWidth();
        }

        prefWidth = w + (padLeft + padRight + 2*borderThickness) * ctx.uiScaleY();
        prefHeight = h + (padTop + padBottom + 2*borderThickness) * ctx.uiScaleY();
    }

    @Override
    public void onLayout(UILayoutEngine.UILayoutContext ctx) {
        UILayout.AllocationItem[] items = new UILayout.AllocationItem[children.size()];

        for (int i = 0; i < children.size(); i++) {
            UIElement child = children.get(i).child();
            UILayout layout = children.get(i).layout();
            items[i] = new UILayout.AllocationItem(layout.getWidthMode(), layout.getWidthValue(), child.getMeasuredWidth());
        }

        float[] widths = UILayout.allocateSlices(items, getContentWidth(), ctx.uiScaleY());

        float cursor = 0f;

        for (int i = 0; i < children.size(); i++) {
            UIElement child = children.get(i).child();

            float ch = UILayoutEngine.resolveHeight(child, children.get(i).layout(), getContentHeight(), ctx.uiScaleY());
            child.arrange(ctx, getContentX() + cursor, getContentY(), widths[i], ch);

            cursor += widths[i];
        }
    }

}
