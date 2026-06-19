package karnickeldev.solar.ui.components.widgets;

import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.UIHelper;
import karnickeldev.solar.ui.components.UILayout;
import karnickeldev.solar.ui.components.container.HorizontalGroup;

/**
 * @author KarnickelDev
 * @since 06.06.2026
 **/
public class OptionCycler<T> extends HorizontalGroup {

    public static class OptionDefinition<T> {
        final String[] labels;
        final T[] values;

        public OptionDefinition(String[] labels, T[] values) {
            if(labels.length != values.length) throw new IllegalArgumentException("Each Value must have a TextWidget!");

            this.labels = labels;
            this.values = values;
        }

        int size() {
            return labels.length;
        }
    }

    private int borderColor = 0xFFFFFFFF;
    private int backgroundColor = 0xFFFFFFFF;

    private final TextButton left;
    private final TextButton right;
    private final TextWidget label;

    private final OptionDefinition<T> options;
    private int index;

    public OptionCycler(OptionDefinition<T> options) {
        this.options = options;
        this.index = 0;

        left = new TextButton("<", this::prev);
        left.setBorderThickness(0);
        //left.setBackgroundColor(TRANSPARENT);
        //left.setBorderColor(0xFFFFFF00);

        right = new TextButton(">", this::next);
        right.setBorderThickness(0);
        //right.setBackgroundColor(TRANSPARENT);
        //right.setBorderColor(0xFFFFFF00);

        label = new TextWidget(getLabel());
        label.setBorderThickness(0);
        //label.setBackgroundColor(TRANSPARENT);
        //label.setBorderColor(0xFFFFFF00);

        add(left);
        add(label, new UILayout().fillWidth(1).percentHeight(1));
        add(right);
        setPadding(15);
        setBorderColor(0xFFFFFFFF);
        setBorderThickness(2f);
    }

    @Override
    public void setPadding(float padLeft, float padRight, float padTop, float padBottom) {
        right.setPadding(padLeft, padRight, padTop, padBottom);
        left.setPadding(padLeft, padRight, padTop, padBottom);
    }

    public void setFontColor(int rgba) {
        label.setFontColor(rgba);
        left.setFontColor(rgba);
        right.setFontColor(rgba);
    }

    public void setBorderColor(int rgba8888) {
        this.borderColor = rgba8888;
        //label.setBorderColor(Color.rgba8888(borderColor));
    }

    public void setBackgroundColor(int rgba8888) {
        this.backgroundColor = rgba8888;
    }

    @Override
    public void setBorderThickness(float borderThickness) {
        this.borderThickness = borderThickness;
        label.setBorderThickness(-borderThickness);
    }

    public String getLabel() {
        return options.labels[index];
    }

    public T getValue() {
        return options.values[index];
    }

    public int getIndex() {
        return index;
    }

    public int getLength() {
        return options.size();
    }

    public void next() {
        index = (index + 1) % options.size();
        onChange();
    }

    public void prev() {
        index = (index - 1 + options.size()) % options.size();
        onChange();
    }

    public TextWidget getUILabel() {
        return label;
    }

    protected void onChange() {
        label.setText(getLabel());
        invalidateLayout();
    }

    @Override
    public void render(RendererContext ctx) {
        UIHelper.drawBackground(ctx.uiRenderer(), this, backgroundColor, Panel.WHITE);
        UIHelper.drawBorder(ctx.uiRenderer(), this, borderColor, getBorderThickness(), Panel.WHITE);
        label.render(ctx);
        left.render(ctx);
        right.render(ctx);
    }

}
