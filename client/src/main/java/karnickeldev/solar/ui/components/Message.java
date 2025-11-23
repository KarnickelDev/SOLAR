package karnickeldev.solar.ui.components;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import karnickeldev.solar.ui.core.UI;

/**
 * @author KarnickelDev
 * @since 10.07.2025
 **/
public class Message implements UIComponent {

    private final Table table;
    private String text = "Empty";

    private TextArea message;

    public Message() {
        table = new Table();
    }

    @Override
    public Group getGroup() {
        return table;
    }

    public void setText(String message) {
        text = message;
        this.message.setText(text);
    }

    @Override
    public void update(float delta) {

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
                UI.getUIManager().hideComponent("message");
            }
        });

        table.add(close).pad(15);

        table.layout();
    }
}
