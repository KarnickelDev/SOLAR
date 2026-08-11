package karnickeldev.solar.ui.components.widgets;

import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.UIHelper;
import karnickeldev.solar.ui.components.UILayout;
import karnickeldev.solar.ui.components.UIState;
import karnickeldev.solar.ui.components.container.HorizontalGroup;
import karnickeldev.solar.ui.components.styles.TextWidgetStyle;
import karnickeldev.solar.ui.core.Align;

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

    private TextWidgetStyle textStyle;

    private final TextButton left;
    private final TextButton right;
    private final TextWidget label;

    private final OptionDefinition<T> options;
    private int index;

    public OptionCycler(OptionDefinition<T> options, TextWidgetStyle style) {
        this.options = options;
        this.index = 0;

        this.textStyle = new TextWidgetStyle(style);
        textStyle.contentAlign = Align.CENTER | Align.MIDDLE;

        left = new TextButton("<", textStyle, this::prev);
        left.setBorderThickness(0);
        left.setPadding(10f);

        right = new TextButton(">", textStyle, this::next);
        right.setBorderThickness(0);
        right.setPadding(10f);

        label = new TextWidget(getLabel(), textStyle);
        label.setBorderThickness(0);
        label.setPadding(10f);

        add(left, new UILayout().percentHeight(1));
        add(label, new UILayout().fillWidth(1).percentHeight(1));
        add(right, new UILayout().percentHeight(1));
        setPadding(0);
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
        UIHelper.drawBackground(ctx.uiRenderer(), this, textStyle.backgroundColor(UIState.NORMAL), Panel.WHITE);
        UIHelper.drawBorder(ctx.uiRenderer(), this, textStyle.borderColor(UIState.NORMAL), getBorderThickness(), Panel.WHITE);
        super.render(ctx);
    }

}
