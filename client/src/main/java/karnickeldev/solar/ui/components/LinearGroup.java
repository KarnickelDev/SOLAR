package karnickeldev.solar.ui.components;

/**
 * @author KarnickelDev
 * @since 07.06.2026
 **/
public abstract class LinearGroup extends UIContainer {

    protected final UILayout.Axis axis;

    protected LinearGroup(UILayout.Axis axis) {
        this.axis = axis;
    }

    @Override
    public void updateLayout(UILayoutEngine.UILayoutContext ctx) {
        super.updateLayout(ctx);
        float scale = ctx.uiScaleY();

        float main = axis == UILayout.Axis.HORIZONTAL ? getContentWidth() : getContentHeight();
        float cross = axis == UILayout.Axis.HORIZONTAL ? getContentHeight() : getContentWidth();

        UILayout.Allocation[] allocations = UILayout.allocateSlots(children, axis, main, cross, scale);

        float cursor = 0f;

        for(int i = 0; i < allocations.length; i++) {
            UIElement child = children.get(i);
            float size = allocations[i].size();
            float x, y;
            float width, height;

            if(axis == UILayout.Axis.HORIZONTAL) {
                x = getContentX() + cursor;
                y = getContentY();
                width = size;
                height = cross;
            } else {
                x = getContentX();
                y = getContentTop() - cursor - size;
                width = cross;
                height = size;
            }

            child.setBounds(x, y, width, height);
            child.updateMetrics(scale);

            child.invalidateLayout(); //TODO: make this unnecessary

            cursor += size;
        }
    }

}
