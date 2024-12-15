package karnickeldev.solar.ui.pausemenu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.settings.Resolution;
import karnickeldev.solar.ui.Fonts;
import karnickeldev.solar.ui.MenuButton;
import karnickeldev.solar.ui.UIElement;

public class OptionsMenu implements UIElement {
    Label.LabelStyle titleStyle;
    private final Label title;
    private final Table optionsMenuTable;

    private final TextureAtlas atlas;

    private final UIElement[] subMenus;
    private int selectedSubMenu = 0;

    private final MenuButton[] buttons;

    public OptionsMenu(Skin skin) {

        subMenus = new UIElement[] {
            new GameplayOptionsMenu(),
            new VideoOptionsMenu(skin),
            new GameplayOptionsMenu()
        };

        buttons = new MenuButton[] {
            new MenuButton("Gameplay", skin, () -> {
                selectSubMenu(0);
            }),
            new MenuButton("Video", skin, () -> {
                selectSubMenu(1);
            }),
            new MenuButton("Audio", skin, () -> {
                selectSubMenu(2);
            }),
            new MenuButton("Back", skin, () -> {
                hide();
                SolarMain.getInstance().getUIManager().getMainMenu().show();
            }),
        };

        assert(subMenus.length == buttons.length-1);

        atlas = new TextureAtlas("uiskin.atlas");
        NinePatchDrawable background = new NinePatchDrawable(new NinePatch(new TextureRegion(atlas.findRegion("default-round")),
            4, 4, 4, 4));


        titleStyle = new Label.LabelStyle();
        titleStyle.font = Fonts.BIG;
        titleStyle.background = background;
        titleStyle.fontColor = Color.WHITE;

        title = new Label("Options", titleStyle);
        title.setAlignment(Align.center);

        optionsMenuTable = new Table();
        optionsMenuTable.setClip(true);
        optionsMenuTable.setBackground(background);
        optionsMenuTable.pad(0);
        optionsMenuTable.top().left();

        resizeUI(
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenWidth(),
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenHeight()
        );

        SolarMain.getInstance().pausedStage.addActor(optionsMenuTable);

        hide();
    }

    public void selectSubMenu(int subMenu) {
        subMenus[selectedSubMenu].hide();
        setButtonFont(buttons[selectedSubMenu], Fonts.MEDIUM);
        this.selectedSubMenu = subMenu;
        setButtonFont(buttons[selectedSubMenu], Fonts.MEDIUM_BOLD);
        subMenus[selectedSubMenu].show();
    }

    public int getSelectedSubMenu() {return selectedSubMenu;}

    @Override
    public float getWidth() {return optionsMenuTable.getWidth();}

    @Override
    public float getHeight() {return optionsMenuTable.getHeight();}

    @Override
    public float getX() {return optionsMenuTable.getX();}

    @Override
    public float getY() {return optionsMenuTable.getY();}

    @Override
    public void resizeUI(int width, int height) {
        float menuWidth = 0.22f * Resolution.getAdjustedWidth(height);
        float menuHeight = 1.5f * menuWidth;

        optionsMenuTable.clear();

        titleStyle.font = Fonts.BIG;
        title.setStyle(titleStyle);

        optionsMenuTable.setSize(menuWidth, menuHeight);
        optionsMenuTable.setPosition(0.03f * width, 0.5f * (height - getHeight()));

        float titleHeight = 0.15f * menuHeight;
        float spacing = (0.48f * (menuHeight - titleHeight)) / (buttons.length-1);
        float elementHeight = (0.48f * (menuHeight - titleHeight)) / buttons.length;

        optionsMenuTable.add(title).width(menuWidth).height(0.7f * titleHeight).fillX().expandX().row();
        optionsMenuTable.add().height(0.3f * titleHeight).expandX().row();


        for(int i = 0; i < buttons.length; i++) {
            setButtonFont(buttons[i], Fonts.MEDIUM);
            optionsMenuTable.add(buttons[i]).width(0.93f * menuWidth).height(elementHeight).row();
            if(i < buttons.length - 1) optionsMenuTable.add().height(spacing).expandX().row();
        }


        optionsMenuTable.layout();

        for (UIElement uiElement : subMenus) {
            uiElement.resizeUI(width, height);
        }
    }

    @Override
    public void show() {
        optionsMenuTable.setVisible(true);
        selectSubMenu(0);
    }

    @Override
    public void hide() {
        for (UIElement subMenu : subMenus) subMenu.hide();
        optionsMenuTable.setVisible(false);
    }

    @Override
    public void dispose() {
        atlas.dispose();
    }

    private static void setButtonFont(TextButton button, BitmapFont font) {
        TextButton.TextButtonStyle style = button.getStyle();
        style.font = font;
        button.setStyle(style);
    }

}
