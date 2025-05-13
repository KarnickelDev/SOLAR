package karnickeldev.solar.ui.pausemenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.settings.Resolution;
import karnickeldev.solar.ui.Fonts;
import karnickeldev.solar.ui.MenuButton;
import karnickeldev.solar.ui.SkinManager;
import karnickeldev.solar.ui.UIElement;

public class OptionsMenu implements UIElement {
    private final Label title;
    private final UIElement[] subMenus;
    private final UIElement parent;
    private final MenuButton[] buttons;
    Label.LabelStyle titleStyle;
    Table optionsMenu, menus, optionWindow;
    private Skin uiSkin;
    private int selectedSubMenu = 0;
    private boolean isVisible;

    public OptionsMenu(Skin skin, UIElement parent) {
        this(skin, 0, false, parent);
    }

    public OptionsMenu(Skin skin, int initSubMenu, boolean initVisible, UIElement parent) {
        uiSkin = skin;
        selectedSubMenu = initSubMenu;
        isVisible = initVisible;
        this.parent = parent;


        titleStyle = new Label.LabelStyle();
        titleStyle.font = Fonts.BIG;
        titleStyle.background = SkinManager.getTableBackground();
        titleStyle.fontColor = Color.WHITE;

        title = new Label("Options", titleStyle);
        title.setAlignment(Align.center);

        subMenus = new UIElement[3];

        buttons = new MenuButton[]{
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
                if (parent != null) {
                    parent.hide();
                    parent.show();
                }
            }),
        };

        assert (subMenus.length == buttons.length - 1);

        resizeUI(
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenWidth(),
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenHeight()
        );

        setVisible(initVisible);
    }

    private static void setButtonFont(TextButton button, BitmapFont font) {
        TextButton.TextButtonStyle style = button.getStyle();
        style.font = font;
        button.setStyle(style);
    }

    public void selectSubMenu(int subMenu) {
        subMenus[selectedSubMenu].hide();
        setButtonFont(buttons[selectedSubMenu], Fonts.MEDIUM);
        this.selectedSubMenu = subMenu;
        setButtonFont(buttons[selectedSubMenu], Fonts.MEDIUM_BOLD);
        subMenus[selectedSubMenu].show();
    }

    public int getSelectedSubMenu() {
        return selectedSubMenu;
    }

    @Override
    public float getWidth() {
        return optionsMenu.getWidth();
    }

    @Override
    public float getHeight() {
        return optionsMenu.getHeight();
    }

    @Override
    public float getX() {
        return optionsMenu.getX();
    }

    @Override
    public float getY() {
        return optionsMenu.getY();
    }

    @Override
    public void resizeUI(int width, int height) {
        float menuWidth = 0.7f * Resolution.getAdjustedWidth(height);
        float menuHeight = 0.7f * menuWidth;

        if (optionsMenu != null) optionsMenu.remove();

        optionsMenu = new Table();
        optionsMenu.setClip(true);
        optionsMenu.setBackground(SkinManager.getTableBackground());
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
        menus.setBackground(SkinManager.getTableBackground());
        menus.pad(0);
        menus.top().left();
        menus.setSize(0.26f * menuWidth, menuHeight);
        menus.add(title).width(menus.getWidth()).height(elementHeight).pad(0).row();
        menus.add().height(elementHeight).pad(0).row();

        for (int i = 0; i < buttons.length; i++) {
            setButtonFont(buttons[i], Fonts.MEDIUM);
            menus.add(buttons[i]).width(0.9f * menus.getWidth()).height(elementHeight).pad(0).row();
            menus.add().height(elementHeight).pad(0).row();
        }

        optionWindow = new Table();
        optionWindow.setClip(true);
        optionWindow.setBackground(SkinManager.getTableBackground());
        optionWindow.pad(0);
        optionWindow.top().left();
        optionWindow.setSize(
            menuWidth - menus.getWidth(),
            menuHeight - menus.getHeight()
        );

        for (UIElement uiElement : subMenus) if (uiElement != null) uiElement.dispose();
        subMenus[0] = new GameplayOptionsMenu();
        subMenus[1] = new VideoOptionsMenu(optionWindow, uiSkin, this);
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
    }

}
