package karnickeldev.solar.core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.settings.Settings;
import karnickeldev.solar.settings.SettingsManager;
import karnickeldev.solar.ui.Fonts;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SolarMain extends Game {

    private static SolarMain instance;

    SpriteBatch batch;

    private GlyphLayout glyph_layout;

    private final SettingsManager settingsManager;
    private final InputManager inputManager;


    public SolarMain(Settings settings) {
        instance = this;

        this.settingsManager = new SettingsManager(settings);

        inputManager = new InputManager();
    }

    public static SolarMain getInstance() {return instance;}

    public SettingsManager getSettingsManager() {return settingsManager;}

    public Settings getSettings() {return settingsManager.getSettings();}

    public SpriteBatch getBatch() {return batch;}

    public InputManager getInputManager() {return inputManager;}

    @Override
    public void create() {
        getSettingsManager().applySettings();

        batch = new SpriteBatch();

        Fonts.generateFonts(Gdx.graphics.getHeight());

        glyph_layout = new GlyphLayout();

        Gdx.input.setInputProcessor(getInputManager().getInputMultiplexer());

        setScreen(new LoadingScreen(this, null));

        Logger.log(Logger.STARTUP, "Startup complete");
    }

    @Override
    public void render() {
        super.render();

        BitmapFont font = Fonts.SMALL;

        batch.begin();
        String text = Metadata.APP_NAME + " v" + Metadata.VERSION;
        glyph_layout.setText(font, text);
        font.draw(batch, text,0, glyph_layout.height, glyph_layout.width, Align.left, false);

        glyph_layout.setText(font, "FPS: 999999");
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(),
            Gdx.graphics.getWidth() - glyph_layout.width,
            Gdx.graphics.getHeight() - 1.01f*glyph_layout.height);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        Fonts.disposeFonts();
        batch.dispose();
        AssetWrapper.getInstance().dispose();
    }

    public void exit() {
        System.exit(0);
    }

}
