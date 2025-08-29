package karnickeldev.solar.ui.components.optionsmenu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.ui.core.UI;

/**
 * @author : KarnickelDev
 * @since : 09.07.2025
 **/
public class CheckButton extends TextButton {

    private static final String OFF = "OFF", ON = "ON";

    private boolean checked;
    private final String text;
    float prefWidth;

    public CheckButton(String text, boolean checked) {
        super(text + ' ' + OFF, UI.skin().get("toggle", TextButtonStyle.class));
        this.checked = checked;
        this.text = text;
        this.prefWidth = getLabel().getPrefWidth();
        setText(text + ' ' + (checked ? ON : OFF));
        setChecked(checked);
        addListener(new ToggleListener());
        getLabel().setAlignment(Align.left);
        pad(5);
    }

    @Override
    public float getPrefWidth() {
        return prefWidth*1.2f;
    }

    public boolean isChecked() {
        return checked;
    }

    private class ToggleListener extends ChangeListener {

        private ToggleListener() {}

        @Override
        public void changed(ChangeEvent event, Actor actor) {
            checked = !checked;
            setText(text + ' ' + (checked ? ON : OFF));
        }
    }

}
