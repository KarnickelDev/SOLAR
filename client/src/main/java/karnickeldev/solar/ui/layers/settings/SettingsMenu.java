package karnickeldev.solar.ui.layers.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.settings.Settings;
import karnickeldev.solar.ui.core.UIComponent;
import karnickeldev.solar.ui.core.UI;

/**
 * @author KarnickelDev
 * @since 08.07.2025
 **/
public class SettingsMenu implements UIComponent {

    private final Table table;

    private final VideoOptionsMenu videoOptionsMenu;

    private static final String[] subMenuNames = {"General", "Video", "Audio"};
    private int checked = 0;

    private final Runnable onClose;

    public SettingsMenu(Runnable onClose) {
        this.onClose = onClose;

        table = new Table();
        videoOptionsMenu = new VideoOptionsMenu();
        //table.debugAll();
    }

    @Override
    public void show() {
        table.setVisible(true);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void hide() {
        table.setVisible(false);
    }

    @Override
    public Group getGroup() {
        return table;
    }

    @Override
    public void update(float delta) {
        videoOptionsMenu.update(delta);
    }

    @Override
    public void resize(int width, int height) {
        table.clear();
        table.setSkin(UI.skin());
        table.setBackground(UI.skin().get("up", NinePatchDrawable.class));
        table.top().left();

        table.setSize(1250, 700);
        table.setPosition(140 + (UI.VIRTUAL_WIDTH - table.getWidth()) / 2f, (UI.VIRTUAL_HEIGHT - table.getHeight()) / 2f);
        table.pad(0);

        Table categories = new Table(UI.skin());
        categories.setBackground(table.getBackground());
        categories.setPosition(0,0);
        categories.setSize(150, table.getHeight());
        categories.pad(0);

        Label settings = new Label("Settings", new Label.LabelStyle(UI.getFontManager().getFont(50, true), UI.WHITE));

        TextButton general = new TextButton("General", UI.skin(),"bold");
        TextButton video = new TextButton("Video", UI.skin(),"bold");
        TextButton audio = new TextButton("Audio", UI.skin(),"bold");
        TextButton back = new TextButton("Back", UI.skin(),"bold");

        general.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                videoOptionsMenu.getGroup().setVisible(false);
                checked = 0;
            }
        });

        video.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                videoOptionsMenu.getGroup().setVisible(true);
                checked = 1;
            }
        });

        audio.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                videoOptionsMenu.getGroup().setVisible(false);
                checked = 2;
            }
        });

        back.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                back.setChecked(false);
                if(onClose != null) {
                    onClose.run();
                } else {
                    Logger.get(LogTag.UI).error("onClose is null: " + this.getClass().getSimpleName());
                }
            }
        });

        general.pad(15);
        video.pad(15);
        audio.pad(15);
        back.pad(15);

        ButtonGroup<TextButton> group = new ButtonGroup<>();
        group.setMaxCheckCount(1);
        group.setMinCheckCount(1);
        group.setUncheckLast(true);
        group.add(general);
        group.add(video);
        group.add(audio);
        group.setChecked(subMenuNames[checked]);


        float catButtonHeight = 40;

        categories.add(settings).width(categories.getWidth()).expandX().fill().pad(15).row();
        categories.add(general).height(catButtonHeight).fill().pad(0).row();
        categories.add().height(catButtonHeight).fill().pad(0).row();
        categories.add(video).height(catButtonHeight).fill().pad(0).row();
        categories.add().height(catButtonHeight).fill().pad(0).row();
        categories.add(audio).height(catButtonHeight).fill().pad(0).row();
        categories.add().height(catButtonHeight).fill().pad(0).row();
        categories.add().expandY().fill().row();
        categories.layout();

        table.add(categories).height(table.getHeight() - catButtonHeight);

        videoOptionsMenu.resize(100,100);
        table.add(videoOptionsMenu.getGroup()).expandX().fill();
        table.row();

        TextButton apply = new TextButton("Apply", UI.skin());
        apply.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Settings newSettings = new Settings(SolarMain.getInstance().getSettings());
                videoOptionsMenu.applyChanges(newSettings);

                if(!newSettings.equals(SolarMain.getInstance().getSettings())) {
                    SolarMain.getInstance().getSettingsManager().updateSettings(newSettings);
                    SolarMain.getInstance().getSettingsManager().saveToFile();
                }
                resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        });

        table.add(back).height(catButtonHeight).fill();
        table.add(apply).height(catButtonHeight).align(Align.right).width(100);

        table.layout();
    }
}
