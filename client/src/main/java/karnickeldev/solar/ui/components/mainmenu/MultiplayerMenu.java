package karnickeldev.solar.ui.components.mainmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.ui.components.UIComponent;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.screens.GameplayLoadScreen;

/**
 * @author KarnickelDev
 * @since 10.07.2025
 **/
public class MultiplayerMenu implements UIComponent {

    private final Table table;

    private String playerDisplayName;

    private final ServerListIni serverListIni = new ServerListIni();

    public MultiplayerMenu() {
        table = new Table();
        String chars = "0123456789";
        playerDisplayName = "Player";
        for(int i = 0; i < 4; i++) {
            playerDisplayName += chars.charAt((int)(Math.random()*chars.length()));
        }
    }

    @Override
    public Group getGroup() {
        return table;
    }

    @Override
    public void show() {
        table.setVisible(true);
        serverListIni.load();
    }

    @Override
    public void hide() {
        table.setVisible(false);
        serverListIni.save();
    }

    public void addServer(String name, String ip, String password) {
        serverListIni.addServer(name, ip, password);
    }

    public void removeServer(String name) {
        serverListIni.removeServer(name);
    }

    public void updateServerList() {
        serverListIni.save();
        serverListIni.load();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void resize(int width, int height) {
        serverListIni.load();

        java.util.List<String[]> serverList = serverListIni.getServerList();

        table.clear();
        table.setSkin(UI.skin());
        table.setBackground(UI.skin().get("up", NinePatchDrawable.class));
        table.top().center();
        table.pad(0);

        table.setSize(850, 700);
        table.setPosition((UI.VIRTUAL_WIDTH - table.getWidth()) / 2f, (UI.VIRTUAL_HEIGHT - table.getHeight()) / 2f);

        Table serverData = new Table();
        serverData.setSkin(UI.skin());
        serverData.background(table.getBackground());
        serverData.top().center();
        serverData.pad(5);

        Label nameLabel = new Label("Name:", UI.skin());
        Label ipLabel = new Label("IP:", UI.skin());
        Label passwordLabel = new Label("Password:", UI.skin());

        TextField name = new TextField("My Server", UI.skin());
        TextField ip = new TextField("localhost", UI.skin());
        TextField password = new TextField("password", UI.skin());
        password.setPasswordCharacter('*');
        password.setPasswordMode(true);


        TextButton join = new TextButton("Direct join", UI.skin());
        join.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                GameStateManager.get().changeState(new GameplayLoadScreen(true, ip.getText()));
            }
        });

        TextButton add = new TextButton("Add server", UI.skin());
        add.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                String[] server = {
                    name.getText(),
                    ip.getText(),
                    password.getText()
                };

                if(!serverList.contains(server)) {
                    addServer(server[0], server[1], server[2]);
                }
                updateServerList();
            }
        });


        VerticalGroup joinAdd = new VerticalGroup();
        joinAdd.addActor(join);
        joinAdd.space(5);
        joinAdd.addActor(add);
        joinAdd.fill();

        serverData.add(nameLabel).pad(5);
        serverData.add(name).pad(5);
        serverData.add(ipLabel).pad(5);
        serverData.add(ip).pad(5);
        serverData.add(passwordLabel).pad(5);
        serverData.add(password).pad(5);
        serverData.add().expandX();
        serverData.add(joinAdd).pad(5).align(Align.right);


        table.add(serverData).width(table.getWidth()).expandX().row();


        VerticalGroup serverListGroup = new VerticalGroup();
        serverListGroup.fill();
        serverListGroup.expand();
        serverListGroup.space(10);
        for(String[] server: serverList) {
            serverListGroup.addActor(new ServerListEntry(server[0], server[1], this, UI.skin()));
            serverListGroup.fill();
        }

        table.add(serverListGroup).fill().expandX().expandY().pad(25).row();

        TextButton back = new TextButton("Back", UI.skin());
        back.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                UI.getUIManager().hideComponent("multiplayer_menu");
            }
        });

        Label playerNameLabel = new Label("Your Username:", UI.skin());
        TextField nameInput = new TextField(playerDisplayName, UI.skin());
        nameInput.setAlignment(Align.center);

        Table playerName = new Table();
        playerName.right().pad(0);
        playerName.add(playerNameLabel).padRight(10);
        playerName.add(nameInput).width(230);

        Table bottomBar = new Table();
        bottomBar.right();
        bottomBar.pad(0);
        bottomBar.setSkin(UI.skin());
        bottomBar.background(UI.skin().get("up", NinePatchDrawable.class));
        bottomBar.add(back).expandX().fill().pad(10);
        bottomBar.add(playerName).expandX().fill().pad(10);

        table.add(bottomBar).width(table.getWidth()).expandX().pad(0);

        table.layout();
    }
}
