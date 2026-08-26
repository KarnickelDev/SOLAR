package karnickeldev.solar.ui.components.widgets;

import karnickeldev.solar.input.Buttons;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.ui.components.interaction.Clickable;
import karnickeldev.solar.ui.components.styles.TextWidgetStyle;

import java.util.function.Consumer;

/**
 * @author KarnickelDev
 * @since 08.06.2026
 **/
public class CheckButton extends TextWidget implements Clickable {

    private final String textOff, textOn;
    private Consumer<Boolean> onClick;

    private boolean checked = false;

    public CheckButton(String textOff, String textOn, TextWidgetStyle style, Consumer<Boolean> onClick) {
        super(textOff, style);
        this.textOff = textOff;
        this.textOn = textOn;

        setOnClick(onClick);
    }

    public CheckButton(String textOff, String textOn) {
        this(textOff, textOn, new TextWidgetStyle(), null);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        if(this.checked != checked) {
            toggle();
        }
    }

    public void setOnClick(Consumer<Boolean> onClick) {
        this.onClick = onClick;
    }

    private void toggle() {
        if(checked) {
            checked = false;
            setText(textOff);
        } else {
            checked = true;
            setText(textOn);
        }

        try {
            this.onClick.accept(this.checked);
        } catch(Exception ex) {
            Logger.get(this.getClass().getSimpleName()).warn("error on click: " + ex.getMessage());
        }
    }

    @Override
    public boolean onMouseDown(int x, int y, int button) {
        return button == Buttons.LEFT;
    }

    @Override
    public void onMouseUp(int x, int y, int button) {}

    @Override
    public void onPressed(int x, int y, int button) {
        toggle();
    }
}
