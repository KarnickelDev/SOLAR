package karnickeldev.solar.ui.layers.hud.chat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.core.UIComponent;
import karnickeldev.solar.ui.layers.mainmenu.MultiplayerMenu;

/**
 * @author KarnickelDev
 * @since 04.04.2026
 **/
public class ChatWindow extends Table implements UIComponent {

    //private final MessageRenderer renderer;

    private final TextField textInput;

    private boolean active = false;

    public ChatWindow() {
        //renderer = new MessageRenderer(new ChatMessageStore());
        //renderer.setPad(5,5,5,5,5);

        textInput = new TextField("Type here", UI.skin());
        textInput.setTouchable(Touchable.enabled);
        textInput.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(!isActive()) activate();
                return isActive();
            }
        });
        textInput.setColor(1,1,1,0.7f);
        textInput.setMaxLength(512);

        top().left().pad(0);
        setPosition(20,32);
        setSize(420,350);

        //add(renderer).expand().fill().row();
        add().expand().fill().row();
        add(textInput).fillX().height(30);
    }

    public TextField getTextField() {
        return textInput;
    }

    @Override
    public Group getGroup() {
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        active = true;

        //getStage().setScrollFocus(renderer);
        getStage().setKeyboardFocus(textInput);
        textInput.setText("");
    }

    public void deactivate() {
        active = false;

        getStage().setScrollFocus(null);
        getStage().setKeyboardFocus(null);

        textInput.setText("Type here...");
    }

    public void handleInput() {
        String text = textInput.getText();
        if(text == null || text.isEmpty() || text.equals("\n") || text.equals("\r") || text.equals("\r\n")) {
            return;
        }

        textInput.setText("");
        addChatMessage(new ChatMessageBuilder().text(ChatMessageBuilder.DEFAULT_SENDER_COLOR, MultiplayerMenu.playerDisplayName + ": ").text(UI.WHITE, text).build());
    }

    public void addChatMessage(ChatMessage msg) {
        //renderer.addMessage(msg);
    }

    public void addLogMessage(ChatMessage msg) {

    }

    @Override
    public void update(float delta) {
        //renderer.act(delta);
    }

    @Override
    public void resize(int width, int height) {
        textInput.setStyle(UI.getSkinManager().getSkin().get("default", TextField.TextFieldStyle.class));
    }

}
