package karnickeldev.solar.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import karnickeldev.solar.core.SolarMain;

public class MainMenu implements UIElement {

    private Table mainMenu;

    public MainMenu(Skin skin) {


        mainMenu = new Table();

        mainMenu.add(new MenuButton("Singleplayer", skin)).row();
        mainMenu.add(new MenuButton("Multiplayer", skin)).row();
        mainMenu.add(new MenuButton("Options", skin, () -> SolarMain.getInstance().getUIManager().getOptionsMenu().show())).row();
        mainMenu.add(new MenuButton("Credits", skin)).row();
        mainMenu.add(new MenuButton("Exit", skin, () -> SolarMain.getInstance().exit())).row();

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
        hide();

        BitmapFont font = Fonts.MEDIUM_BOLD;

        GlyphLayout glyph_layout = new GlyphLayout();
        glyph_layout.setText(font,"Singleplayer");

        for(Actor actor : mainMenu.getChildren()) {
            if(actor instanceof MenuButton) {
                MenuButton button = (MenuButton) actor;
                TextButton.TextButtonStyle style = button.getStyle();
                style.font = font;
                button.setStyle(style);
            }
        }

        int groupHeight = height / 3;
        float groupSpacing = 0.5f * groupHeight;
        float elementHeight = 0.5f * groupHeight;
        int groupWidth = Math.min(width, (int)(1.5f* glyph_layout.width));

        mainMenu.setSize(groupWidth, groupHeight);
        mainMenu.setPosition(width * 0.03f, (height * 0.72f) - groupHeight);

        for(Actor actor: mainMenu.getChildren()) {
            if(actor instanceof MenuButton) {
                MenuButton button = (MenuButton) actor;
                mainMenu.getCell(button).width(groupWidth).height(elementHeight / 5f)
                    .padBottom(groupSpacing / 4f)
                    .fill()
                    .expandX();
            }
        }

        mainMenu.invalidate();
        mainMenu.layout();

        show();
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
    public void dispose() {

    }
}
