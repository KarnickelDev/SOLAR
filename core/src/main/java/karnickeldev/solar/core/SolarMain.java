package karnickeldev.solar.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.server.servers.DefaultServer;
import karnickeldev.solar.settings.Settings;
import karnickeldev.solar.settings.SettingsManager;
import karnickeldev.solar.ui.Fonts;
import karnickeldev.solar.ui.UIManager;
import karnickeldev.solar.util.MathUtil;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SolarMain extends Game {

    private static SolarMain instance;

    SpriteBatch batch;

    private GlyphLayout glyph_layout;

    private final SettingsManager settingsManager;
    private final InputManager inputManager;
    private UIManager uiManager;

    public Stage pausedStage;
    public Stage hudStage;

    private Label fpsLabel, tpsLabel, tpsExtraLabel, versionLabel;

    public SolarMain(Settings settings) {
        instance = this;

        this.settingsManager = new SettingsManager(settings);

        this.inputManager = new InputManager();

        this.uiManager = new UIManager();
    }

    public static SolarMain getInstance() {return instance;}

    public SettingsManager getSettingsManager() {return settingsManager;}

    public Settings getSettings() {return settingsManager.getSettings();}

    public SpriteBatch getBatch() {return batch;}

    public InputManager getInputManager() {return inputManager;}

    public UIManager getUIManager() {return uiManager;}

    @Override
    public void create() {
        getSettingsManager().applySettings();

        batch = new SpriteBatch();

        Fonts.generateFonts(Gdx.graphics.getHeight());

        glyph_layout = new GlyphLayout();

        pausedStage = new Stage(new ScreenViewport(new OrthographicCamera(getSettings().getScreenWidth(), getSettings().getScreenHeight())));
        hudStage = new Stage(new ScreenViewport(new OrthographicCamera(getSettings().getScreenWidth(), getSettings().getScreenHeight())));

        getUIManager().create();

        fpsLabel = new Label("", new Label.LabelStyle(Fonts.SMALL, Color.WHITE));
        fpsLabel.setVisible(true);

        tpsLabel = new Label("", new Label.LabelStyle(Fonts.SMALL, Color.WHITE));
        tpsLabel.setVisible(true);

        tpsExtraLabel = new Label("", new Label.LabelStyle(Fonts.SMALL, Color.WHITE));
        tpsExtraLabel.setVisible(true);

        versionLabel = new Label("", new Label.LabelStyle(Fonts.SMALL, Color.WHITE));
        versionLabel.setVisible(true);

        hudStage.addActor(fpsLabel);
        hudStage.addActor(tpsLabel);
        hudStage.addActor(tpsExtraLabel);
        hudStage.addActor(versionLabel);

        Gdx.input.setInputProcessor(getInputManager().getInputMultiplexer());

        setScreen(new LoadingScreen(this, null));

        Logger.log(Logger.STARTUP, "Startup complete");
    }

    @Override
    public void render() {
        super.render();

        pausedStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        hudStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        configureLabels();

        hudStage.act(Gdx.graphics.getDeltaTime());
        hudStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        Logger.log(Logger.SHUTDOWN, "Cleaning up for shutdown...");

        Fonts.disposeFonts();
        batch.dispose();
        AssetWrapper.getInstance().dispose();

        if(SimTestScreen.server != null) SimTestScreen.server.stop();

        Logger.log(Logger.SHUTDOWN, "Shutdown complete, bye!");
    }


    public void exit() {
        Gdx.app.exit();
    }

    private final static Color normal = new Color(0.09f,0.69f,0.07f,1f);
    private final static Color mid = new Color(0.94f,0.52f,0.1f,1f);
    private final static Color bad = new Color(0.86f,0.07f,0.07f,1f);

    private void configureLabels() {
        Label.LabelStyle s = fpsLabel.getStyle();
        s.font = Fonts.SMALL;

        Color fpsColor = normal;
        Color tpsColor = normal;
        Color tpsExtraColor = normal;

        versionLabel.setStyle(s);
        versionLabel.setText(Metadata.APP_NAME + " v" + Metadata.VERSION);
        versionLabel.setPosition(0.005f*Gdx.graphics.getWidth(), 0.015f*Gdx.graphics.getHeight());

        float fps = Gdx.graphics.getFramesPerSecond();
        if(fps < 90) fpsColor = mid;
        if(fps < 50) fpsColor = bad;
        fpsLabel.setStyle(s);
        fpsLabel.setText("FPS: " + fps);
        fpsLabel.setPosition(0.92f * Gdx.graphics.getWidth(), 0.95f * Gdx.graphics.getHeight());
        fpsLabel.setColor(fpsColor);

        float tps = DefaultServer.tpsCount.getTPS();
        if(tps < 0.95f * DefaultServer.TICK_RATE) tpsColor = mid;
        if(tps < 0.85f * DefaultServer.TICK_RATE) tpsColor = bad;
        tpsLabel.setStyle(s);
        tpsLabel.setText("TPS: " + String.format("%.2f", tps));
        tpsLabel.setPosition(fpsLabel.getX(), fpsLabel.getY() - 0.02f*Gdx.graphics.getHeight());
        tpsLabel.setColor(tpsColor);


        float delay = DefaultServer.tpsCount.getDelayedness();
        if(delay >= 0.05) tpsExtraColor = mid;
        if(delay >= 0.15) tpsExtraColor = bad;
        tpsExtraLabel.setStyle(s);
        tpsExtraLabel.setText(
            "d: " + String.format("%.2f", delay)
                + "\nmax: " + String.format("%.2f", DefaultServer.tpsCount.getMaxTickDuration())
                + "\nmin: " + String.format("%.2f", DefaultServer.tpsCount.getMinTickDuration())
        );
        tpsExtraLabel.setPosition(fpsLabel.getX(), tpsLabel.getY() - 2*0.02f*Gdx.graphics.getHeight());
        tpsExtraLabel.setColor(tpsExtraColor);
    }

}
