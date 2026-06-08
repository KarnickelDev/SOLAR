package karnickeldev.solar.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.render.core.RendererContext;

/**
 * @author KarnickelDev
 * @since 06.06.2026
 **/
public class OptionCycler<T> extends HorizontalGroup {

    private static final Color TRANSPARENT = new Color(0xFFFFFF00);

    public static class OptionDefinition<T> {
        final String[] labels;
        final T[] values;

        public OptionDefinition(String[] labels, T[] values) {
            if(labels.length != values.length) throw new IllegalArgumentException("Each Value must have a Label!");

            this.labels = labels;
            this.values = values;
        }

        int size() {
            return labels.length;
        }
    }

    private final Color borderColor = new Color(1,1,1,1);
    private final Color backgroundColor = new Color(1,1,1,1);

    private final TextButton left;
    private final TextButton right;
    private final Label label;

    private final OptionDefinition<T> options;
    private int index;

    public OptionCycler(OptionDefinition<T> options) {
        this.options = options;
        this.index = 0;

        left = new TextButton("<", this::prev);
        left.setBorderThickness(0);
        left.setBackgroundColor(TRANSPARENT);
        left.setBorderColor(0xFFFFFF00);

        right = new TextButton(">", this::next);
        right.setBorderThickness(0);
        right.setBackgroundColor(TRANSPARENT);
        right.setBorderColor(0xFFFFFF00);

        label = new Label(getLabel());
        label.setBorderThickness(0);
        label.setBackgroundColor(TRANSPARENT);
        label.setBorderColor(0xFFFFFF00);

        add(left);
        add(label, new UILayout().fillWidth(1).percentHeight(1));
        add(right);
        setPadding(15);
        setBorderColor(Color.WHITE);
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

    public void setBorderColor(Color borderColor) {
        this.borderColor.set(borderColor);
        label.setBorderColor(Color.rgba8888(borderColor));
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
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

    public Label getUILabel() {
        return label;
    }

    protected void onChange() {
        label.setText(getLabel());
        invalidateLayout();
    }

    @Override
    public void render(RendererContext ctx) {
        UIHelper.drawBackground(ctx.uiRenderer(), this, backgroundColor, Panel.WHITE);
        UIHelper.drawBorder(ctx.uiRenderer(), this, borderColor, Panel.WHITE);
        label.render(ctx);
        left.render(ctx);
        right.render(ctx);
    }

}
