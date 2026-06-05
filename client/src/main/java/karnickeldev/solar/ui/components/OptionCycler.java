package karnickeldev.solar.ui.components;

/**
 * @author KarnickelDev
 * @since 06.06.2026
 **/
public class OptionCycler<T> extends HorizontalGroup {

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

    private final TextButton left;
    private final TextButton right;
    private final Label label;

    private final OptionDefinition<T> options;
    private int index;

    public OptionCycler(OptionDefinition<T> options) {
        this.options = options;
        this.index = 0;

        left = new TextButton("<--", this::prev);
        left.setBorderColor(0xFF0000FF);
        left.setBorderThickness(2f);

        right = new TextButton("-->", this::next);
        right.setBorderColor(0xFF0000FF);
        right.setBorderThickness(2f);

        label = new Label(getLabel());
        label.getLayout().percentHeight(1f).fillWidth(1f);

        add(left);
        add(label);
        add(right);
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

    protected void onChange() {
        label.setText(getLabel());
    }

}
