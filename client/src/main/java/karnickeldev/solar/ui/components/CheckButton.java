package karnickeldev.solar.ui.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.util.MathUtil;

import java.util.function.Consumer;

/**
 * @author KarnickelDev
 * @since 08.06.2026
 **/
public class CheckButton extends Label {

    private final Color a = Color.RED;
    private final Color b = Color.GREEN;

    private final String textOff, textOn;
    private Consumer<Boolean> onClick;

    private boolean checked = false;

    public CheckButton(String textOff, String textOn, Consumer<Boolean> onClick) {
        super(textOff);
        this.textOff = textOff;
        this.textOn = textOn;

        setBackgroundColor(a);
        setOnClick(onClick);
    }

    public CheckButton(String textOff, String textOn) {
        this(textOff, textOn, null);
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

    @Override
    public boolean handleInput(InputEvent event) {
        float my = Gdx.graphics.getHeight() - event.getStageY();
        if(!MathUtil.AABB(event.getStageX(), my, getX(), getY(), getRight(), getTop())) {
            return false;
        }

        if(event.getType() == InputEvent.Type.touchDown) {
            if(event.getButton() == Input.Buttons.LEFT) {
                toggle();
                return true;
            }
        }

        return false;
    }

    private void toggle() {
        if(checked) {
            checked = false;
            setBackgroundColor(a);
            setText(textOff);
        } else {
            checked = true;
            setBackgroundColor(b);
            setText(textOn);
        }

        try {
            this.onClick.accept(this.checked);
        } catch(Exception ex) {
            Logger.get(this.getClass().getSimpleName()).warn("error on click: " + ex.getMessage());
        }
    }

}
