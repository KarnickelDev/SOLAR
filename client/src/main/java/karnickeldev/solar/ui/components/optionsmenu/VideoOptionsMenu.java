package karnickeldev.solar.ui.components.optionsmenu;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.settings.Resolution;
import karnickeldev.solar.settings.Settings;
import karnickeldev.solar.ui.components.UIComponent;
import karnickeldev.solar.ui.core.UI;

/**
 * @author : KarnickelDev
 * @since : 09.07.2025
 **/
public class VideoOptionsMenu implements UIComponent {

    private final Table table;

    private SelectBox<String> resolutionSel;

    protected VideoOptionsMenu() {
        table = new Table();
        table.setVisible(false);
        //table.debugAll();
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
        table.top().center();
        table.setSize(width, height);

        table.pad(10);

        Settings settings = SolarMain.getInstance().getSettings();

        Label resolutionLabel = new Label("Resolution:", UI.skin());
        resolutionSel = new SelectBox<>(UI.skin());
        resolutionSel.setItems(Resolution.SUPPORTED_RESOLUTIONS);
        resolutionSel.setSelected(Resolution.resolutionToString(settings.getScreenWidth(), settings.getScreenHeight()));
        resolutionSel.setAlignment(Align.center);

        CheckButton vsync = new CheckButton("VSync:", settings.isVsync());

        Label fpsLimitLabel = new Label("FPS-Limit:", UI.skin());
        TextField fpsLimit = new TextField(""+settings.getFpsLimit(), UI.skin());
        fpsLimit.setAlignment(Align.left);

        CheckButton fullscreen = new CheckButton("Fullscreen:", settings.isFullscreen());

        CheckButton borderless = new CheckButton("Borderless:", settings.isBorderless());

        TextButton apply = new TextButton("Apply", UI.skin());
        apply.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if(SolarMain.getInstance().getSettingsManager().applySettings(
                    resolutionSel.getSelected(), vsync.isChecked(),
                    fullscreen.isChecked(), borderless.isChecked(), fpsLimit.getText()
                )) {
                    SolarMain.getInstance().getSettingsManager().saveToFile();

                    // TODO: ugly, could be done without recursion
                    resize(width, height);
                }
            }
        });

        table.add(resolutionLabel).pad(20).padRight(5);
        table.add(resolutionSel).width(resolutionSel.getPrefWidth()*1.1f).pad(20);
        table.add(fullscreen).pad(20).expandX();
        table.row();

        table.add(fpsLimitLabel).pad(20).padRight(5);
        table.add(fpsLimit).pad(20);
        table.add(borderless).expandX();
        table.row();

        table.add().pad(20);
        table.add().pad(20);
        table.add(vsync).pad(20);
        table.row();

        table.add().expandY().row();
        table.add();
        table.add(apply);

        table.layout();
    }
}
