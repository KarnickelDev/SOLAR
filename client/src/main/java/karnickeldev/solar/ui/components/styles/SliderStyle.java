package karnickeldev.solar.ui.components.styles;

/**
 * @author KarnickelDev
 * @since 24.06.2026
 **/
public class SliderStyle {

    public enum SliderChar {
        THIN_PIPE('|', '-', false),
        THICK('#', ':', true),
        STEPPED_SQUARE('■', '□', true),
        STEPPED_DIAMOND('◆', '◇', true),
        ;

        private final char cpFilled, cpEmpty;
        private final boolean allowCumulative;
        SliderChar(char cpFilled, char cpEmpty, boolean allowCumulative) {
            this.cpFilled = cpFilled;
            this.cpEmpty = cpEmpty;
            this.allowCumulative = allowCumulative;
        }

        public char getFilledCP() { return cpFilled; }
        public char getEmptyCP() { return cpEmpty; }
        public boolean allowCumulative() { return allowCumulative; }
    }

    public SliderChar sliderChar;
    /** On stepped SliderChar Types, decides if all steps to the left get filled */
    public boolean cumulativeFill;
    public TextWidgetStyle textStyle;

    public SliderStyle(SliderChar sliderChar, boolean cumulativeFill, TextWidgetStyle textStyle) {
        this.sliderChar = sliderChar;
        this.cumulativeFill = cumulativeFill;
        this.textStyle = textStyle;
    }

    public SliderStyle(TextWidgetStyle textStyle) {
        this(SliderChar.STEPPED_SQUARE, true, textStyle);
    }
}
