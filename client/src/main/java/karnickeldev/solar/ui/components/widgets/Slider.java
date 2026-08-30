package karnickeldev.solar.ui.components.widgets;

import karnickeldev.solar.input.Buttons;
import karnickeldev.solar.input.Keys;
import karnickeldev.solar.render.core.RendererContext;
import karnickeldev.solar.ui.components.*;
import karnickeldev.solar.ui.components.container.HorizontalGroup;
import karnickeldev.solar.ui.components.interaction.Clickable;
import karnickeldev.solar.ui.components.interaction.Draggable;
import karnickeldev.solar.ui.components.interaction.Hoverable;
import karnickeldev.solar.ui.components.interaction.KeyInputTarget;
import karnickeldev.solar.ui.components.styles.SliderStyle;
import karnickeldev.solar.ui.components.styles.TextWidgetStyle;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.fontutil.TextRun;

/**
 * @author KarnickelDev
 * @since 23.06.2026
 **/
public class Slider extends HorizontalGroup implements Clickable, KeyInputTarget, Hoverable {

    @FunctionalInterface
    public interface ValueFormatter {
        String format(float value);
    }

    private final ValueWidget value;
    private final TextButton stepLeft;
    private final TextButton stepRight;
    private final SliderTrack sliderTrack;

    private ValueFormatter valueFormatter;

    /**
     * A slider from min to max in the given style
     * @param min minimum value
     * @param max maximum value
     * @param charCount number of visually distinct values. use -1 for best-effort-fit
     * @param cumulativeFill if true: fill slider from left until current. if false: only show current value
     * @param sliderStyle slider styling
     * @param valueFormatter returns the string the slider displays for values
     */
    public Slider(float min, float max, int charCount, boolean cumulativeFill, SliderStyle sliderStyle, ValueFormatter valueFormatter) {
        if(min >= max) throw new IllegalArgumentException("Invalid min/max values: " + min + "/" + max);
        if(sliderStyle == null) throw new IllegalArgumentException("SliderStyle cannot be null");

        this.valueFormatter = valueFormatter;

        sliderTrack = new SliderTrack(min, max, charCount, cumulativeFill, sliderStyle);
        sliderTrack.setPadding(5f);

        value = new ValueWidget(0, max, valueFormatter, sliderStyle.textStyle);
        value.setPadding(2f);

        stepLeft = new TextButton("<", sliderStyle.textStyle, null);
        stepLeft.setPadding(2f);
        stepLeft.setOnClick(() -> sliderTrack.setIndex(sliderTrack.getIndex() - 1));

        stepRight = new TextButton(">", sliderStyle.textStyle, null);
        stepRight.setPadding(2f);
        stepRight.setOnClick(() -> sliderTrack.setIndex(sliderTrack.getIndex() + 1));

        setPadding(2f);
        add(stepLeft, new UILayout().percentHeight(1f));
        add(sliderTrack, new UILayout().percentHeight(1f).fillWidth(1f));
        add(value, new UILayout().percentHeight(1f));
        add(stepRight, new UILayout().percentHeight(1f));
    }

    public Slider(float min, float max, int charCount, boolean cumulativeFill, SliderStyle sliderStyle) {
        this(min, max, charCount, cumulativeFill, sliderStyle, value -> String.format("%.1f", value));
    }

    @Override
    public void onAct(float delta) {
        value.setText(valueFormatter.format(sliderTrack.getValue()));
    }

    @Override
    public boolean onMouseDown(int x, int y, int button) {
        return false;
    }

    @Override
    public void onMouseUp(int x, int y, int button) {}

    @Override
    public void onClicked(int x, int y, int button) {}

    @Override
    public void onClickCancel(int x, int y, int button) {

    }

    @Override
    public boolean keyDown(int key) {
        return false;
    }

    @Override
    public boolean keyUp(int key) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public void onHoverEnter() {

    }

    @Override
    public void onHoverExit() {

    }



    // INTERNAL SLIDER-TRACK CLASS
    private static final class SliderTrack extends TextWidget implements Hoverable, Clickable, Draggable, KeyInputTarget {

        private static final byte MAX_CHAR_COUNT = 32;

        private SliderStyle sliderStyle;
        private final int charCount;
        private final boolean cumulativeFill;

        private final float min, max;
        private int index;
        private int cells;

        public SliderTrack(float min, float max, int charCount, boolean cumulativeFill, SliderStyle sliderStyle) {
            super("", sliderStyle.textStyle);
            setTouchable(true);
            if(charCount > MAX_CHAR_COUNT) throw new IllegalStateException("SliderTrack can't use more than 32 chars");
            if(charCount == 0 || charCount == 1) throw new IllegalStateException("SliderTrack needs at least 2 chars");

            this.charCount = Math.clamp(charCount, -1, MAX_CHAR_COUNT);
            this.sliderStyle = sliderStyle;
            this.cumulativeFill = cumulativeFill;
            this.min = min;
            this.max = max;

            index = -1;
            cells = 1;
        }

        private float getValue() {
            if(cells <= 0 || index < 0) return min;

            float t = (index + 1) / (float) cells;
            return Math.clamp(min + t * (max - min), min, max);
        }

        private void setValue(float value) {
            if (cells <= 1) {
                index = -1;
                return;
            }

            float t = (value - min) / (max - min);
            if(t <= 0f) {
                index = -1;
                return;
            }

            setIndex(Math.round(t * cells) - 1);
        }

        private int getIndex() {
            return index;
        }

        private void setIndex(int index) {
            this.index = Math.clamp(index, -1, cells - 1);
        }

        private void setIndexFromMouseX(float x) {
            float trackWidth = layout.getLogicalWidth();
            if(cells <= 0 || trackWidth <= 0f) return;

            float trackX = getContentX() + contentAlignX(getContentWidth(), trackWidth, textStyle.contentAlign);
            float cellWidth = trackWidth / cells;

            // Before the first character = zero.
            if(x < trackX) {
                setIndex(-1);
                return;
            }

            setIndex((int)((x - trackX) / cellWidth));
        }

        private int getCellCount() {
            return cells;
        }

        private float getMin() {
            return min;
        }

        private float getMax() {
            return max;
        }

        private float getStepSize() {
            return (max-min) / cells;
        }

        @Override
        public void onLayout(UILayoutEngine.UILayoutContext ctx) {
            if(charCount > 1) {
                // fixed size
                cells = charCount;
            } else {
                // Slider fonts are monospaced, so a slider glyph's advance is the exact cell width.
                float cellWidth = textStyle.font.getGlyph(sliderStyle.sliderChar.getFilledCP()).advance
                    * textStyle.fontSize * ctx.uiScaleY();
                cells = cellWidth > 0f ? Math.max(2, (int) (getContentWidth() / cellWidth)) : 2;
            }

            index = Math.clamp(index, -1, cells - 1);
            updateText();

            // Lay out the text after recomputing it so rendering and hit detection agree immediately.
            super.onLayout(ctx);
        }

        @Override
        public void render(RendererContext ctx) {
            super.render(ctx);
        }

        @Override
        public void onAct(float dt) {
            if(cells <= 0) {
                invalidateLayout();
                return;
            }

            updateText();
        }

        private void updateText() {
            char filled = sliderStyle.sliderChar.getFilledCP();
            char empty = sliderStyle.sliderChar.getEmptyCP();

            StringBuilder text = new StringBuilder();
            for(int i = 0; i < cells; i++) {
                if(cumulativeFill) {
                    text.append(i <= index ? filled : empty);
                } else {
                    text.append(i == index ? filled : empty);
                }
            }

            setText(text.toString());
        }

        @Override
        public boolean onMouseDown(int x, int y, int button) {
            if(button != Buttons.LEFT) return false;
            UI.getUIManager().setFocus(this);
            setIndexFromMouseX(x);
            return true;
        }

        @Override
        public void onMouseUp(int x, int y, int button) {}

        @Override
        public void onClicked(int x, int y, int button) {}

        @Override
        public void onClickCancel(int x, int y, int button) {

        }

        @Override
        public void onDragStart(float x, float y) {}

        @Override
        public void onDrag(float x, float y, float dx, float dy) {
            setIndexFromMouseX(x);
        }

        @Override
        public void onDragEnd(float x, float y) {}

        @Override
        public void onDragCancel(float x, float y) {}

        @Override
        public void onHoverEnter() {
        }

        @Override
        public void onHoverExit() {
        }

        @Override
        public boolean keyDown(int key) {
            if(key != Keys.LEFT && key != Keys.RIGHT) return false;

            if(key == Keys.LEFT) {
                setIndex(getIndex() - 1);
            }
            if(key == Keys.RIGHT) {
                setIndex(getIndex() + 1);
            }

            return true;
        }

        @Override
        public boolean keyUp(int key) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }
    }

    private static final class ValueWidget extends TextWidget {

        private final TextRun[] maxValueRun = new TextRun[1];

        public ValueWidget(float initial, float max, ValueFormatter formatter, TextWidgetStyle textStyle) {
            super(formatter.format(initial), textStyle);
            maxValueRun[0] = new TextRun(formatter.format(max), 0xFFFFFFFF);
        }

        @Override
        public void measure(UILayoutEngine.UILayoutContext ctx) {
            prefLayoutCache.layout(textStyle.font, maxValueRun, textStyle.textAlign, 1e4f, textStyle.fontSize);
            float cachedPrefWidth = prefLayoutCache.getLogicalWidth();
            float cachedPrefHeight = prefLayoutCache.getLogicalHeight();

            prefWidth = 1 + ctx.uiScaleY()*(cachedPrefWidth + padLeft + padRight + 2*borderThickness);
            prefHeight = 1 + ctx.uiScaleY()*(cachedPrefHeight + padTop + padBottom + 2*borderThickness);
        }
    }

}
