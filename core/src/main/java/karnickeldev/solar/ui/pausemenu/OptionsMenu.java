package karnickeldev.solar.ui.pausemenu;

import com.badlogic.gdx.Gdx;
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
import karnickeldev.solar.ui.SkinManager;
import karnickeldev.solar.ui.UIElement;

public class OptionsMenu implements UIElement {
    Label.LabelStyle titleStyle;
    private final Label title;
    private Skin uiSkin;

    Table optionsMenu, menus, optionWindow;

    private final TextureAtlas atlas;

    private final UIElement[] subMenus;
    private int selectedSubMenu = 0;

    NinePatchDrawable background;

    private final MenuButton[] buttons;

    private boolean isVisible;

    public OptionsMenu(Skin skin) {
        this(skin, 0, false);
    }

    public OptionsMenu(Skin skin, int initSubMenu, boolean initVisible) {
        uiSkin = skin;
        selectedSubMenu = initSubMenu;
        isVisible = initVisible;

        atlas = new TextureAtlas("uiskin.atlas");
        background = new NinePatchDrawable(new NinePatch(new TextureRegion(atlas.findRegion("default-round")),
            4, 4, 4, 4));


        titleStyle = new Label.LabelStyle();
        titleStyle.font = Fonts.BIG;
        titleStyle.background = background;
        titleStyle.fontColor = Color.WHITE;

        title = new Label("Options", titleStyle);
        title.setAlignment(Align.center);

        subMenus = new UIElement[3];

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

        resizeUI(
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenWidth(),
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenHeight()
        );

        setVisible(initVisible);
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
    public float getWidth() {return optionsMenu.getWidth();}

    @Override
    public float getHeight() {return optionsMenu.getHeight();}

    @Override
    public float getX() {return optionsMenu.getX();}

    @Override
    public float getY() {return optionsMenu.getY();}

    @Override
    public void resizeUI(int width, int height) {
        float menuWidth = 0.7f * Resolution.getAdjustedWidth(height);
        float menuHeight = 0.7f * menuWidth;

        if(optionsMenu != null) optionsMenu.remove();

        optionsMenu = new Table();
        optionsMenu.setClip(true);
        optionsMenu.setBackground(background);
        optionsMenu.pad(0);
        optionsMenu.top().left();
        optionsMenu.setSize(menuWidth, menuHeight);
        optionsMenu.setPosition(
            0.5f * (Gdx.graphics.getWidth() - getWidth()),
            0.5f * (Gdx.graphics.getHeight() - getHeight())
            );

        titleStyle.font = Fonts.BIG;
        title.setStyle(titleStyle);

        float elementHeight = (0.98f * menuHeight) / ((2f * (buttons.length + 1)));

        menus = new Table();
        menus.setClip(true);
        menus.setBackground(background);
        menus.pad(0);
        menus.top().left();
        menus.setSize(0.26f * menuWidth, menuHeight);
        menus.add(title).width(menus.getWidth()).height(elementHeight).pad(0).row();
        menus.add().height(elementHeight).pad(0).row();

        for(int i = 0; i < buttons.length; i++) {
            setButtonFont(buttons[i], Fonts.MEDIUM);
            menus.add(buttons[i]).width(0.9f * menus.getWidth()).height(elementHeight).pad(0).row();
            menus.add().height(elementHeight).pad(0).row();
        }

        optionWindow = new Table();
        optionWindow.setClip(true);
        optionWindow.setBackground(background);
        optionWindow.pad(0);
        optionWindow.top().left();
        optionWindow.setSize(
            menuWidth - menus.getWidth(),
            menuHeight - menus.getHeight()
        );

        for (UIElement uiElement : subMenus) if(uiElement != null) uiElement.dispose();
        subMenus[0] = new GameplayOptionsMenu();
        subMenus[1] = new VideoOptionsMenu(optionWindow, uiSkin);
        subMenus[2] = new GameplayOptionsMenu();

        optionsMenu.add(menus).expandY().fill();
        optionsMenu.add(optionWindow).expand().fill();

        for (UIElement uiElement : subMenus) uiElement.resizeUI(width, height);

        SolarMain.getInstance().pausedStage.addActor(optionsMenu);

        selectSubMenu(getSelectedSubMenu());
        setVisible(isVisible());
    }

    @Override
    public void show() {
        optionsMenu.setVisible(true);
        selectSubMenu(getSelectedSubMenu());
        isVisible = true;
    }

    @Override
    public void hide() {
        for (UIElement subMenu : subMenus) subMenu.hide();
        optionsMenu.setVisible(false);
        isVisible = false;
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void dispose() {
        hide();
        atlas.dispose();
    }

    private static void setButtonFont(TextButton button, BitmapFont font) {
        TextButton.TextButtonStyle style = button.getStyle();
        style.font = font;
        button.setStyle(style);
    }

}
