package karnickeldev.solar.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 27.10.2024
 */
public class MenuButton extends TextButton {

    boolean clicked = false;

    public MenuButton(String text, Skin skin) {
        this(text, skin, null);
    }

    public MenuButton(String text, TextButtonStyle style, Runnable action) {
        super(text, style);
        getLabel().setAlignment(Align.left);
        getLabel().setText("  " + text);
        pad(0f);

        this.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                clicked = true;
                if(action != null) action.run();
            }
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                getLabel().setText(" >" + text);
            }
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if(!clicked) getLabel().setText("  " + text);
                clicked = false;
            }
        });
    }

    public MenuButton(String text, Skin skin, Runnable action) {
        super(text, skin);
        getLabel().setAlignment(Align.left);
        getLabel().setText("  " + text);
        pad(0f);

        this.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                clicked = true;
                if(action != null) action.run();
            }
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                getLabel().setText(" >" + text);
            }
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if(!clicked) getLabel().setText("  " + text);
                clicked = false;
            }
        });
    }


    public void setFont(BitmapFont font) {
        TextButtonStyle style = getStyle();
        style.font = font;
        setStyle(style);
    }

}
