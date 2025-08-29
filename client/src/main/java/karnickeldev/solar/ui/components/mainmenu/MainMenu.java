package karnickeldev.solar.ui.components.mainmenu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import karnickeldev.solar.Metadata;
import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.ui.components.UIComponent;
import karnickeldev.solar.ui.screens.GameplayLoadScreen;
import karnickeldev.solar.ui.core.UI;

/**
 * @author : KarnickelDev
 * @since : 08.07.2025
 **/
public class MainMenu implements UIComponent {

    private final Table table;

    public MainMenu() {
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
        menuButtonStyle.font = UI.getFontManager().getFont(22, true);
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
                GameStateManager.get().changeState(new GameplayLoadScreen(false, "localhost"));
            }
        });

        multiplayer.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                UI.getUIManager().hideComponent("options_menu");
                UI.getUIManager().hideComponent("singleplayer_menu");
                UI.getUIManager().hideComponent("credits_menu");
                UI.getUIManager().hideComponent("message");
                UI.getUIManager().showComponent("multiplayer_menu");
            }
        });

        options.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                UI.getUIManager().hideComponent("options_menu");
                UI.getUIManager().hideComponent("singleplayer_menu");
                UI.getUIManager().hideComponent("credits_menu");
                UI.getUIManager().hideComponent("message");
                UI.getUIManager().hideComponent("multiplayer_menu");
                UI.getUIManager().showComponent("options_menu");
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

        BitmapFont titleFont = UI.getFontManager().getFont(50, true);
        Label titleLabel = new Label(Metadata.APP_NAME, new Label.LabelStyle(titleFont, UI.WHITE));
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
