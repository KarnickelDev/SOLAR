package karnickeldev.solar.ui.layers.mainmenu;

import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.ui.components.*;
import karnickeldev.solar.ui.components.container.VerticalGroup;
import karnickeldev.solar.ui.components.styles.TextWidgetStyle;
import karnickeldev.solar.ui.components.widgets.Spacer;
import karnickeldev.solar.ui.components.widgets.TextButton;
import karnickeldev.solar.ui.screens.GameplayLoadScreen;

/**
 * @author KarnickelDev
 * @since 08.07.2025
 **/
public final class MainMenu extends VerticalGroup {

    public MainMenu(TextWidgetStyle textStyle) {

        setPadding(15f);

        float buttonPad = 10f;

        TextButton singleplayer = new TextButton("Singleplayer", textStyle, () ->
            GameStateManager.get().requestStateLoading(new GameplayLoadScreen(false, "localhost")));
        singleplayer.setPadding(buttonPad);

        TextButton multiplayer = new TextButton("Multiplayer", textStyle, null);
        multiplayer.setPadding(buttonPad);

        TextButton settings = new TextButton("Settings", textStyle, null);
        settings.setPadding(buttonPad);

        TextButton credits = new TextButton("Credits", textStyle, null);
        credits.setPadding(buttonPad);

        TextButton exit = new TextButton("Exit", textStyle, SolarMain::shutdown);
        exit.setPadding(buttonPad);

        Spacer spacer = new Spacer(10,10);

        add(singleplayer, new UILayout());
        add(spacer, new UILayout().fillHeight(1f));

        add(multiplayer, new UILayout());
        add(spacer, new UILayout().fillHeight(1f));

        add(settings, new UILayout());
        add(spacer, new UILayout().fillHeight(1f));

        add(credits, new UILayout());
        add(spacer, new UILayout().fillHeight(1f));

        add(exit, new UILayout());
    }

}
/*
public class MainMenu implements UIComponent {


    private final MainMenuLayer mainMenuLayer;

    private final Table table;

    public MainMenu(MainMenuLayer mainMenuLayer) {
        this.mainMenuLayer = mainMenuLayer;

        table = new Table();
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
        table.top().left();

        TextButton.TextButtonStyle menuButtonStyle = new TextButton.TextButtonStyle(UI.skin().get("default", TextButton.TextButtonStyle.class));
        menuButtonStyle.font = UI.getFontManager().getFont(44, true);
        menuButtonStyle.up = null;
        menuButtonStyle.down = null;
        menuButtonStyle.overFontColor = menuButtonStyle.fontColor.cpy().mul(1.25f);

        TextButton singleplayer = new TextButton("Singleplayer", menuButtonStyle);
        TextButton multiplayer = new TextButton("Multiplayer", menuButtonStyle);
        TextButton options = new TextButton("Settings", menuButtonStyle);
        TextButton credits = new TextButton("Credits", menuButtonStyle);
        TextButton exit = new TextButton("Exit", menuButtonStyle);

        singleplayer.getLabel().setAlignment(Align.left);
        singleplayer.pad(4);
        multiplayer.getLabel().setAlignment(Align.left);
        multiplayer.pad(4);
        options.getLabel().setAlignment(Align.left);
        options.pad(4);
        credits.getLabel().setAlignment(Align.left);
        credits.pad(4);
        exit.getLabel().setAlignment(Align.left);
        exit.pad(4);

        singleplayer.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                GameStateManager.get().requestStateLoading(new GameplayLoadScreen(false, "localhost"));
            }
        });

        multiplayer.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                mainMenuLayer.hideComponent("options_menu");
                mainMenuLayer.hideComponent("singleplayer_menu");
                mainMenuLayer.hideComponent("credits_menu");
                mainMenuLayer.hideComponent("message");
                mainMenuLayer.showComponent("multiplayer_menu");
            }
        });

        options.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                // TODO: maybe don't create new Object
                UI.getUIManager().push(new SettingsMenuLayer());
            }
        });

        exit.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                SolarMain.shutdown();
            }
        });

        float padLeft = 15;
        float padRight = 15;
        float padTop = 15;
        float padBottom = 15;

        BitmapFont titleFont = UI.getFontManager().getFont(100, true);
        TextWidget titleLabel = new TextWidget(Metadata.APP_NAME, new TextWidget.TextWidgetStyle(titleFont, UI.WHITE));
        titleLabel.setAlignment(Align.left);

        float fontDimension = 1.2f * titleFont.getCapHeight();

        Image icon = new Image((Texture) AssetWrapper.getInstance().getAsset(Asset.GAME_ICON));
        icon.setScaling(Scaling.fit);
        icon.setSize(fontDimension, fontDimension);
        icon.setColor(titleLabel.getStyle().fontColor);

        Table title = new Table();
        title.add(titleLabel).align(Align.left).padRight(padRight).height(fontDimension).expand().fill();
        title.add(icon).size(fontDimension).align(Align.right);

        table.add(title).pad(padTop, padLeft, padBottom, padRight).row();
        table.add(singleplayer).pad(padTop, padLeft, padBottom, padRight).align(Align.left).row();
        table.add(multiplayer).pad(padTop, padLeft, padBottom, padRight).align(Align.left).row();
        table.add(options).pad(padTop, padLeft, padBottom, padRight).align(Align.left).row();
        table.add(credits).pad(padTop, padLeft, padBottom, padRight).align(Align.left).row();
        table.add(exit).pad(padTop, padLeft, padBottom, padRight).align(Align.left);

        table.setVisible(true);
        table.setSize(280, 360);
        table.setPosition(35, 1.3f * ((UI.VIRTUAL_HEIGHT - table.getHeight()) / 2f));

        table.layout();
    }
}
 */
