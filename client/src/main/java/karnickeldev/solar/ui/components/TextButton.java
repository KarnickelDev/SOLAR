package karnickeldev.solar.ui.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.util.MathUtil;

/**
 * @author KarnickelDev
 * @since 13.04.2026
 **/
public class TextButton extends Label {

    private boolean hover = false;
    private boolean checked = false;
    private Runnable onClick;

    public TextButton(String text, Runnable onClick) {
        super(text);
        this.onClick = onClick;
    }

    public TextButton(String text) {
        this(text, null);
    }

    public void setOnClick(Runnable onClick) {
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
                onClick();
                return true;
            }
        }

        if(event.getType() == InputEvent.Type.enter) {
            if(!hover) onHover();
            hover = true;
            return true;
        }
        if(event.getType() == InputEvent.Type.exit) {
            hover = false;
            return true;
        }

        return false;
    }

    private void onClick() {
        try {
            onClick.run();
        } catch (Exception ex) {
            Logger.get(LogTag.UI).warn("TextButton: error on click: " + ex.getMessage());
        }
        checked = !checked;
    }

    private void onHover() {
        setFontColor(Color.rgba8888(Color.GREEN));
    }

}
