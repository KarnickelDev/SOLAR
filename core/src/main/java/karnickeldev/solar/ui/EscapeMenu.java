package karnickeldev.solar.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.core.LoadingScreen;
import karnickeldev.solar.core.MainMenuScreen;
import karnickeldev.solar.core.SimTestScreen;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.server.servers.DefaultServer;
import karnickeldev.solar.settings.Resolution;
import karnickeldev.solar.ui.pausemenu.OptionsMenu;

public class EscapeMenu implements UIElement {

    private Table escapeTable;

    private final MenuButton resume;
    private final MenuButton options;
    private final MenuButton backToMenu;

    private final OptionsMenu optionsMenu;

    private boolean isVisible = false;

    public EscapeMenu() {
        optionsMenu = new OptionsMenu(SkinManager.getUISkin(), this);

        resume = new MenuButton("Resume", SkinManager.getTextButtonStyle(Fonts.REGULAR), this::hide);

        options = new MenuButton("Options", SkinManager.getTextButtonStyle(Fonts.REGULAR), () -> {
            hide();
            optionsMenu.show();
        });

        backToMenu = new MenuButton("Back", SkinManager.getTextButtonStyle(Fonts.REGULAR), () -> {
            hide();
            SimTestScreen.server.stop();
            SolarMain.getInstance().setScreen(new LoadingScreen(SolarMain.getInstance(), null));
        });

        resizeUI(
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenWidth(),
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenHeight()
        );
    }

    @Override
    public void resizeUI(int width, int height) {
        float menuWidth = 0.15f * Resolution.getAdjustedWidth(height);
        float menuHeight = 0.35f * height;

        if(escapeTable != null) escapeTable.remove();

        escapeTable = new Table();
        escapeTable.setClip(true);
        escapeTable.setBackground(SkinManager.getTableBackground());
        escapeTable.pad(0);
        escapeTable.top().left();
        escapeTable.setSize(menuWidth, menuHeight);
        escapeTable.setPosition(
            0.5f * (Gdx.graphics.getWidth() - getWidth()),
            0.5f * (Gdx.graphics.getHeight() - getHeight())
        );

        escapeTable.align(Align.center);


        resume.setStyle(SkinManager.getTextButtonStyle(SkinManager.BUTTON_MEDIUM));
        options.setStyle(SkinManager.getTextButtonStyle(SkinManager.BUTTON_MEDIUM));
        backToMenu.setStyle(SkinManager.getTextButtonStyle(SkinManager.BUTTON_MEDIUM));

        float elementHeight = 0.9f * menuHeight / 5;

        escapeTable.add().height(0.05f * menuHeight).row();

        escapeTable.add(resume).width(0.9f * menuWidth).height(elementHeight).row();
        escapeTable.add().height(elementHeight).row();
        escapeTable.add(options).width(0.9f * menuWidth).height(elementHeight).row();
        escapeTable.add().height(elementHeight).row();
        escapeTable.add(backToMenu).width(0.9f * menuWidth).height(elementHeight).row();

        escapeTable.add().height(0.05f * menuHeight);

        escapeTable.invalidate();
        escapeTable.layout();

        SolarMain.getInstance().pausedStage.addActor(escapeTable);

        optionsMenu.resizeUI(width, height);

        setVisible(isVisible());
    }

    @Override
    public void show() {
        isVisible = true;
        escapeTable.setVisible(true);

        SolarMain.getInstance().getInputManager().getInputMultiplexer().removeProcessor(SimTestScreen.cameraInput);
    }

    @Override
    public void hide() {
        isVisible = false;
        escapeTable.setVisible(false);

        if(SimTestScreen.cameraInput != null) SolarMain.getInstance().getInputManager().getInputMultiplexer().addProcessor(SimTestScreen.cameraInput);
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void dispose() {
        hide();
    }

    @Override
    public float getWidth() {
        return escapeTable.getWidth();
    }

    @Override
    public float getHeight() {
        return escapeTable.getHeight();
    }

    @Override
    public float getX() {
        return escapeTable.getX();
    }

    @Override
    public float getY() {
        return escapeTable.getY();
    }
}
