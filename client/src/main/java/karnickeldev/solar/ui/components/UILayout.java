package karnickeldev.solar.ui.components;

/**
 * @author KarnickelDev
 * @since 10.04.2026
 **/
public class UILayout {

    public enum SizeMode {
        FIXED,
        PERCENT,
        CONTENT,
        FILL,
    }

    private SizeMode widthMode = SizeMode.CONTENT;
    private SizeMode heightMode = SizeMode.CONTENT;

    private float widthValue = 1f;
    private float heightValue = 1f;

    public UILayout fixedWidth(float w) {
        widthMode = SizeMode.FIXED;
        widthValue = w;
        return this;
    }

    public UILayout fixedHeight(float h) {
        heightMode = SizeMode.FIXED;
        heightValue = h;
        return this;
    }

    public UILayout percentWidth(float p) {
        widthMode = SizeMode.PERCENT;
        widthValue = p;
        return this;
    }

    public UILayout percentHeight(float p) {
        heightMode = SizeMode.PERCENT;
        heightValue = p;
        return this;
    }

    public UILayout fillWidth(float weight) {
        widthMode = SizeMode.FILL;
        widthValue = weight;
        return this;
    }

    public UILayout fillHeight(float weight) {
        heightMode = SizeMode.FILL;
        heightValue = weight;
        return this;
    }

    public UILayout fill() {
        return percentWidth(1f).percentHeight(1f);
    }

    public UILayout contentWidth() {
        widthMode = SizeMode.CONTENT;
        return this;
    }

    public UILayout contentHeight() {
        heightMode = SizeMode.CONTENT;
        return this;
    }

    public SizeMode getWidthMode() {
        return widthMode;
    }

    public SizeMode getHeightMode() {
        return heightMode;
    }

    public float getWidthValue() {
        return widthValue;
    }

    public float getHeightValue() {
        return heightValue;
    }

    public record AllocationItem(SizeMode mode, float value, float prefSize) {}

    public static float[] allocateSlices(AllocationItem[] items, float availableSize, float scale) {
        float[] result = new float[items.length];

        float used = 0f;
        float totalWeight = 0f;

        for(int i = 0; i < items.length; i++) {
            AllocationItem item = items[i];
            switch(item.mode()) {

                case FIXED -> {
                    result[i] = item.value() * scale;
                    used += result[i];
                }

                case PERCENT -> {
                    result[i] = availableSize * item.value();
                    used += result[i];
                }

                case CONTENT -> {
                    result[i] = item.prefSize();
                    used += result[i];
                }

                case FILL -> {
                    totalWeight += item.value();
                }
            }
        }

        float remaining = Math.max(0f, availableSize - used);
        float unit = totalWeight > 0f ? remaining / totalWeight : 0f;

        for(int i = 0; i < items.length; i++) {
            AllocationItem item = items[i];

            if(item.mode() == UILayout.SizeMode.FILL) {
                result[i] = unit * item.value();
            }
        }

        return result;
    }

}
