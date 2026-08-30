package karnickeldev.solar.ui.components.widgets;

import karnickeldev.solar.input.Buttons;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.ui.components.UIState;
import karnickeldev.solar.ui.components.styles.TextWidgetStyle;
import karnickeldev.solar.ui.components.interaction.Clickable;
import karnickeldev.solar.ui.components.interaction.Hoverable;

/**
 * @author KarnickelDev
 * @since 13.04.2026
 **/
public class TextButton extends TextWidget implements Hoverable, Clickable {

    private boolean hovered = false;
    private Runnable onClick;

    public TextButton(String text, TextWidgetStyle style, Runnable onClick) {
        super(text, style);
        setTouchable(true);
        this.textStyle = style;
        this.onClick = onClick;
    }

    public TextButton(String text, Runnable onClick) {
        this(text, new TextWidgetStyle(), onClick);
    }

    public TextButton(String text) {
        this(text, null);
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    public void setStyle(TextWidgetStyle style) {
        this.textStyle = style;
    }

    private void fireClick() {
        if(onClick == null) return;

        try {
            onClick.run();
        } catch (Exception ex) {
            Logger.get(this.getClass().getSimpleName()).warn("error on click: " + ex.getMessage());
        }
    }

    @Override
    public boolean onMouseDown(int x, int y, int button) {
        if(button != Buttons.LEFT) return false;
        state = UIState.PRESSED;
        return true;
    }

    @Override
    public void onMouseUp(int x, int y, int button) {
        state = hovered ? UIState.HOVERED : UIState.NORMAL;
    }

    @Override
    public void onClicked(int x, int y, int button) {
        fireClick();
    }

    @Override
    public void onClickCancel(int x, int y, int button) {
        state = hovered ? UIState.HOVERED : UIState.NORMAL;
    }

    @Override
    public void onHoverEnter() {
        hovered = true;
        state = UIState.HOVERED;
    }

    @Override
    public void onHoverExit() {
        hovered = false;
        state = UIState.NORMAL;
    }
}
