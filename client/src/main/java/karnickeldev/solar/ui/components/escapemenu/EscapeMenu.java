package karnickeldev.solar.ui.components.escapemenu;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.ui.components.UIComponent;
import karnickeldev.solar.ui.components.optionsmenu.OptionsMenu;
import karnickeldev.solar.ui.core.UI;
import karnickeldev.solar.ui.screens.LoadingScreen;
import karnickeldev.solar.ui.screens.MainMenuScreen;

/**
 * @author KarnickelDev
 * @since 10.07.2025
 **/
public class EscapeMenu implements UIComponent {

    private final Table table;

    public EscapeMenu() {
        table = new Table();
    }

    @Override
    public Group getGroup() {
        return table;
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void resize(int width, int height) {
        table.clear();
        table.setSkin(UI.skin());
        table.setBackground(UI.skin().get("up", NinePatchDrawable.class));
        table.top().center();
        table.setSize(280, 460);

        table.pad(10);

        TextButton resume = new TextButton("Resume", UI.skin());

        TextButton save = new TextButton("Save", UI.skin());

        TextButton settings = new TextButton("Settings", UI.skin());

        TextButton back = new TextButton("Back", UI.skin());

        TextButton exit = new TextButton("Exit", UI.skin());


        resume.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                UI.getUIManager().getComponent("escape_menu").hide();
            }
        });

        settings.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                UI.getUIManager().getComponent("escape_menu").hide();
                UI.getUIManager().getComponent("options_menu").show();
                UIComponent options = UI.getUIManager().getComponent("options_menu");

                if(options instanceof OptionsMenu) {
                    OptionsMenu op = (OptionsMenu) options;
                    op.setOnCloseRunnable(() -> {
                        UI.getUIManager().showComponent("escape_menu");
                    });
                }
            }
        });

        back.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                SolarMain.getInstance().setScreen(new LoadingScreen(
                    () -> GameStateManager.get().changeState(new MainMenuScreen(SolarMain.getInstance())),
                    null, null
                ));
            }
        });

        exit.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                SolarMain.shutdown();
            }
        });

        resume.pad(15);
        save.pad(15);
        settings.pad(15);
        back.pad(15);
        exit.pad(15);

        table.add(resume).pad(15).fill().expandX().row();

        table.add(save).pad(15).fill().expandX().row();

        table.add(settings).pad(15).fill().expandX().row();

        table.add(back).pad(15).fill().expandX().row();

        table.add(exit).pad(15).expandX().fill();


        table.layout();

        table.setPosition((UI.VIRTUAL_WIDTH - table.getWidth()) / 2f, (UI.VIRTUAL_HEIGHT - table.getHeight()) / 2f);
    }
}
