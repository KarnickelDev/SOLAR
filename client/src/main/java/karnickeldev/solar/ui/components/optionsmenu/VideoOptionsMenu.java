package karnickeldev.solar.ui.components.optionsmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.settings.Resolution;
import karnickeldev.solar.settings.Settings;
import karnickeldev.solar.ui.components.UIComponent;
import karnickeldev.solar.ui.core.UI;

/**
 * @author KarnickelDev
 * @since 09.07.2025
 **/
public class VideoOptionsMenu implements UIComponent {

    private final Table table;

    private SelectBox<String> resolutionSel;
    private CheckButton fullscreen;
    private CheckButton borderless;
    private CheckButton vsync;
    private TextField fpsLimit;

    protected VideoOptionsMenu() {
        table = new Table();
        table.setVisible(false);
        //table.debugAll();
    }

    @Override
    public Group getGroup() {
        return table;
    }

    public void applyChanges(Settings newSettings) {
        Resolution res = Resolution.extractResolution(resolutionSel.getSelected());
        newSettings.setScreenWidth(res.getWidth());
        newSettings.setScreenHeight(res.getHeight());
        newSettings.setVsync(vsync.isChecked());
        newSettings.setFullscreen(fullscreen.isChecked());
        newSettings.setBorderless(borderless.isChecked());

        int fpsMax = newSettings.getFpsLimit();
        try {
            fpsMax = Integer.parseInt(fpsLimit.getText());
        } catch (NumberFormatException ignored) {}
        newSettings.setFpsLimit(fpsMax);
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void show() {
        table.setVisible(true);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        table.clear();
        table.setSkin(UI.skin());
        table.top().center();

        table.pad(10);

        Settings settings = SolarMain.getInstance().getSettings();

        float elementHeight = 64;

        Label resolutionLabel = new Label("Resolution:", UI.skin());
        resolutionSel = new SelectBox<>(UI.skin());
        resolutionSel.setItems(Resolution.SUPPORTED_RESOLUTIONS);
        resolutionSel.setSelected(Resolution.resolutionToString(settings.getScreenWidth(), settings.getScreenHeight()));
        resolutionSel.setAlignment(Align.center);

        vsync = new CheckButton("VSync:", settings.isVsync());

        Label fpsLimitLabel = new Label("FPS-Limit:", UI.skin());
        fpsLimit = new TextField(""+settings.getFpsLimit(), UI.skin());
        fpsLimit.setAlignment(Align.left);

        fullscreen = new CheckButton("Fullscreen:", settings.isFullscreen());

        borderless = new CheckButton("Borderless:", settings.isBorderless());

        table.add(resolutionLabel).pad(20).padRight(5).height(elementHeight);
        table.add(resolutionSel).width(resolutionSel.getPrefWidth()*1.1f).pad(20);
        table.add(fullscreen).pad(20).expandX();
        table.row();

        table.add(fpsLimitLabel).pad(20).padRight(5).height(elementHeight);
        table.add(fpsLimit).pad(20);
        table.add(borderless).expandX();
        table.row();

        table.add().pad(20).height(elementHeight);
        table.add().pad(20);
        table.add(vsync).pad(20);
        table.row();

        table.add().expandY().row();
        table.add();

        table.layout();
    }
}
