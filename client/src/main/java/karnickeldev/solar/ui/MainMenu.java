package karnickeldev.solar.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import karnickeldev.solar.core.SimTestScreen;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.settings.Resolution;

public class MainMenu implements UIElement {

    private Table mainMenu;

    protected MainMenu(Skin skin) {


        mainMenu = new Table();
        mainMenu.pad(0f);
        mainMenu.setClip(true);
        mainMenu.top();

        mainMenu.add(new MenuButton("Singleplayer", skin, () -> {
            hide();
            SolarMain.getInstance().setScreen(new SimTestScreen());
        })).expandX().fillX().row();
        mainMenu.add(new MenuButton("Multiplayer", skin)).expandX().fillX().row();
        mainMenu.add(new MenuButton("Options", skin, () -> {
            //hide();
            SolarMain.getInstance().getUIManager().getMainMenuOptionsMenu().show();
        })).expandX().fillX().row();
        mainMenu.add(new MenuButton("Credits", skin)).expandX().fillX().row();
        mainMenu.add(new MenuButton("Exit", skin, () -> SolarMain.getInstance().exit())).expandX().fillX().row();

        resizeUI(
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenWidth(),
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenHeight()
        );

        SolarMain.getInstance().pausedStage.addActor(mainMenu);

        show();
    }

    @Override
    public float getWidth() {
        return mainMenu.getWidth();
    }

    @Override
    public float getHeight() {
        return mainMenu.getHeight();
    }

    @Override
    public float getX() {
        return mainMenu.getX();
    }

    @Override
    public float getY() {
        return mainMenu.getY();
    }

    @Override
    public void resizeUI(int width, int height) {
        BitmapFont font = Fonts.MEDIUM_BOLD;

        GlyphLayout glyph_layout = new GlyphLayout();
        glyph_layout.setText(font, "Singleplayer");

        for (Actor actor : mainMenu.getChildren()) {
            if (actor instanceof MenuButton) {
                MenuButton button = (MenuButton) actor;
                TextButton.TextButtonStyle style = button.getStyle();
                style.font = font;
                button.setStyle(style);
            }
        }

        float groupWidth = 0.22f * Resolution.getAdjustedWidth(height);
        float groupHeight = 1.5f * groupWidth;
        float groupSpacing = 0.49f * groupHeight;
        float elementHeight = 0.49f * groupHeight;

        mainMenu.setSize(groupWidth, groupHeight);
        mainMenu.setPosition(width * 0.03f, 0.5f * (height - getHeight()));

        for (Actor actor : mainMenu.getChildren()) {
            if (actor instanceof MenuButton) {
                MenuButton button = (MenuButton) actor;
                mainMenu.getCell(button).width(groupWidth).height(elementHeight / 5f)
                    .padBottom(groupSpacing / 4f)
                    .fill()
                    .expandX();
            }
        }

        mainMenu.invalidate();
        mainMenu.layout();
    }

    @Override
    public void show() {
        mainMenu.setVisible(true);
    }

    @Override
    public void hide() {
        mainMenu.setVisible(false);
    }

    @Override
    public boolean isVisible() {
        return mainMenu.isVisible();
    }

    @Override
    public void dispose() {

    }
}
