package karnickeldev.solar.ui.components;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIComponent;

/**
 * @author KarnickelDev
 * @since 10.07.2025
 **/
public class Message implements UIComponent {

    private final Table table;
    private final String text;

    private TextArea message;

    private final Runnable onClose;

    public Message(String test, Runnable onClose) {
        this.onClose = onClose;
        this.text = test;
        table = new Table();
    }

    @Override
    public Group getGroup() {
        return table;
    }

    @Override
    public void update(float delta) {
        message.setText(text);
    }

    @Override
    public void resize(int width, int height) {
        table.clear();
        table.setSkin(UI.skin());
        table.setBackground(UI.skin().get("up", NinePatchDrawable.class));
        table.top().center().pad(10);

        table.setSize(500,420);
        table.setPosition((UI.VIRTUAL_WIDTH - table.getWidth()) / 2f, (UI.VIRTUAL_HEIGHT - table.getHeight()) / 2f);

        message = new TextArea(text, UI.skin());
        message.setDisabled(true);

        table.add(message).fill().expand().row();

        TextButton close = new TextButton("Close", UI.skin());
        close.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if(onClose != null) {
                    onClose.run();
                } else {
                    Logger.get(LogTag.UI).warn("onClose is null: " + this.getClass().getSimpleName());
                }
            }
        });

        table.add(close).pad(15);

        table.layout();
    }
}
