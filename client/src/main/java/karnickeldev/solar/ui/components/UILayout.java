package karnickeldev.solar.ui.components;

import java.util.List;

/**
 * @author KarnickelDev
 * @since 10.04.2026
 **/
public class UILayout {

    public enum Axis {
        HORIZONTAL,
        VERTICAL
    }

    public enum SizeMode {
        FIXED,
        PERCENT,
        CONTENT,
        FILL,
    }

    public record Allocation(UILayout slot, float size) {}

    public enum Anchor {
        TOP_LEFT, TOP_RIGHT,
        BOTTOM_LEFT, BOTTOM_RIGHT,
        CENTER,
        LEFT, RIGHT, TOP, BOTTOM
    }

    public Anchor anchor = Anchor.BOTTOM_LEFT;

    // positioning
    public float offsetX = 0;
    public float offsetY = 0;

    private SizeMode widthMode = SizeMode.CONTENT;
    private SizeMode heightMode = SizeMode.CONTENT;

    private float widthValue = 1f;
    private float heightValue = 1f;

    private float aspectRatio = 0f;

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

    public UILayout aspectRatio(float value) {
        aspectRatio = value;
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

    public float getAspectRatio() {
        return aspectRatio;
    }

    public static Allocation[] allocateSlots(List<UIElement> elements, Axis axis, float availableSize, float crossSize, float scale) {
        float used = 0f;
        float totalWeight = 0f;
        Allocation[] allocations = new Allocation[elements.size()];

        for(int i = 0; i < elements.size(); i++) {
            UILayout slot = elements.get(i).getLayout();

            SizeMode mode = axis == Axis.HORIZONTAL ? slot.widthMode : slot.heightMode;
            float value = axis == Axis.HORIZONTAL ? slot.widthValue : slot.heightValue;

            float size = 0f;

            switch (mode) {
                case FIXED -> {
                    size = value * scale;
                    used += size;
                }
                case PERCENT -> {
                    size = availableSize * value;
                    used += size;
                }
                case CONTENT -> {
                    size = axis == Axis.HORIZONTAL ?
                        elements.get(i).getPreferredWidth(scale) : elements.get(i).getPreferredHeight(scale);
                    used += size;
                }

                case FILL -> {
                    totalWeight += value;
                }
            }

            if(mode != SizeMode.FILL) allocations[i] = new Allocation(slot, size);
        }

        float remaining = Math.max(0, availableSize - used);
        float fillUnit = totalWeight > 0 ? remaining / totalWeight : 0f;

        for (int i = 0; i < allocations.length; i++) {
            if(allocations[i] != null) continue;

            UILayout slot = elements.get(i).getLayout();

            float fillWeight = axis == Axis.HORIZONTAL ? slot.widthValue : slot.heightValue;
            float fillSize = fillUnit * fillWeight;
            allocations[i] = new Allocation(slot, fillSize);
        }

        return allocations;
    }

}
