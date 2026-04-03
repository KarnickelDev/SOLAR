package karnickeldev.solar.ui.components.mainmenu;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.screens.GameplayLoadScreen;

/**
 * @author KarnickelDev
 * @since 11.07.2025
 **/
public class ServerListEntry extends Table {

    private final Skin skin;

    private final String name;
    private final String ip;

    private final Label nameLabel;
    private final Label ipLabel;

    private final TextButton connect;
    private final TextButton remove;

    public ServerListEntry(String name, String ip, MultiplayerMenu menu, Skin skin) {
        this.skin = skin;

        this.name = name;
        this.ip = ip;

        nameLabel = new Label(name, skin);

        Label.LabelStyle small = new Label.LabelStyle(nameLabel.getStyle());
        small.font = UI.getFontManager().getFont(20, false);
        ipLabel = new Label('[' + ip + ']', small);

        connect = new TextButton("Join", skin);
        remove = new TextButton("Remove", skin);

        clear();
        setBackground(UI.skin().get("up", NinePatchDrawable.class));
        top().left().pad(10).padLeft(15).padRight(15);

        float buttonWidth = 100;

        add(nameLabel).expandX().fill();

        add(connect).expandX().width(buttonWidth).align(Align.right);

        row();

        add(ipLabel).expandX().fill();

        add(remove).expandX().width(buttonWidth).align(Align.right);


        connect.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                GameStateManager.get().requestStateLoading(new GameplayLoadScreen(true, ip));
            }
        });

        remove.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                menu.removeServer(name);
                menu.updateServerList();
            }
        });

    }



}
