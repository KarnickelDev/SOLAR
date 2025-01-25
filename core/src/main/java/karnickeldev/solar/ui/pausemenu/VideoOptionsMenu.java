package karnickeldev.solar.ui.pausemenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.settings.Resolution;
import karnickeldev.solar.settings.Settings;
import karnickeldev.solar.ui.Fonts;
import karnickeldev.solar.ui.MenuButton;
import karnickeldev.solar.ui.UIElement;

public class VideoOptionsMenu implements UIElement {

    private Table videoOptionsTable;

    private final Label.LabelStyle labelStyle;

    private final TextureAtlas atlas;
    private final NinePatchDrawable background;

    private Label resolutionLabel;
    private SelectBox<String> resolution;

    private Label vsyncLabel;
    private CheckBox vsync;

    private MenuButton apply;

    protected VideoOptionsMenu(Skin skin) {
        atlas = new TextureAtlas("uiskin.atlas");
        background = new NinePatchDrawable(new NinePatch(new TextureRegion(atlas.findRegion("default-round")),
            4, 4, 4, 4));

        videoOptionsTable = new Table();
        videoOptionsTable.setClip(true);
        videoOptionsTable.setBackground(background);
        videoOptionsTable.top().left();

        labelStyle = new Label.LabelStyle();
        labelStyle.fontColor = Color.WHITE;
        labelStyle.font = Fonts.MEDIUM;

        resolutionLabel = new Label("Resolution:", labelStyle);
        resolution = new SelectBox<>(skin);

        vsyncLabel = new Label("V-Sync:", labelStyle);
        vsync = new CheckBox("VSync", skin);

        apply = new MenuButton("Apply", skin, () -> {
            Settings newSettings = new Settings(SolarMain.getInstance().getSettings());

            Resolution res = Resolution.extractResolution(resolution.getSelected());
            newSettings.setScreenWidth(res.getWidth());
            newSettings.setScreenHeight(res.getHeight());

            if(!newSettings.equals(SolarMain.getInstance().getSettings())) {
                SolarMain.getInstance().getSettingsManager().updateSettings(newSettings);
                SolarMain.getInstance().getSettingsManager().saveToFile();
            }

            SolarMain.getInstance().getUIManager().getOptionsMenu().hide();
            SolarMain.getInstance().getUIManager().getMainMenu().show();
        });

        resizeUI(
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenWidth(),
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenHeight()
        );

        SolarMain.getInstance().pausedStage.addActor(videoOptionsTable);

        hide();
    }

    @Override
    public float getWidth() {return videoOptionsTable.getWidth();}

    @Override
    public float getHeight() {return videoOptionsTable.getHeight();}

    @Override
    public float getX() {return videoOptionsTable.getX();}

    @Override
    public float getY() {return videoOptionsTable.getY();}

    @Override
    public void resizeUI(int width, int height) {
        float leftHandMenuWidth = SolarMain.getInstance().getUIManager().getMainMenu().getWidth()
            + SolarMain.getInstance().getUIManager().getMainMenu().getX();

        float optionsWidth = 0.5f * Resolution.getAdjustedWidth(height);
        float optionsHeight = 1.18f * optionsWidth;

        float paddingWidth = 0.02f * optionsWidth;
        float paddingMiddle = 0.2f * optionsWidth;
        float elementWidth = (1f - paddingMiddle - 4f*paddingWidth) / 4f;

        videoOptionsTable.clear();
        videoOptionsTable.top().left();

        videoOptionsTable.add().width(paddingWidth).height(50f);
        videoOptionsTable.add().width(elementWidth);
        videoOptionsTable.add().width(paddingWidth);
        videoOptionsTable.add().width(elementWidth);

        videoOptionsTable.add().width(paddingMiddle);

        videoOptionsTable.add().width(elementWidth);
        videoOptionsTable.add().width(paddingWidth);
        videoOptionsTable.add().width(elementWidth);
        videoOptionsTable.add().width(paddingWidth);
        videoOptionsTable.row();

        videoOptionsTable.setPosition(1.05f * leftHandMenuWidth,0.5f * (height - optionsHeight));
        videoOptionsTable.setSize(optionsWidth, optionsHeight);


        labelStyle.font = Fonts.MEDIUM;
        labelStyle.fontColor = Color.WHITE;


        resolutionLabel.setStyle(labelStyle);
        resolution.setItems(Resolution.SUPPORTED_RESOLUTIONS);
        resolution.setSelected(Resolution.resolutionToString(
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenWidth(),
            SolarMain.getInstance().getSettingsManager().getSettings().getScreenHeight()
        ));

        resolution.getList().getStyle().font = Fonts.SMALL;
        resolution.getStyle().font = Fonts.SMALL;


        vsyncLabel.setStyle(labelStyle);
        vsync.getImage().setScaling(Scaling.fill);
        vsync.getImageCell().size(50f, 50f);

        videoOptionsTable.add();
        videoOptionsTable.add(resolutionLabel);
        videoOptionsTable.add();
        videoOptionsTable.add(resolution);
        videoOptionsTable.add();
        videoOptionsTable.add(vsyncLabel);
        videoOptionsTable.add();
        videoOptionsTable.add(vsync);
        videoOptionsTable.add();
        videoOptionsTable.row();

        apply.setFont(Fonts.MEDIUM);
        apply.getLabel().setAlignment(Align.center);

        videoOptionsTable.add();
        videoOptionsTable.add(apply).row();

        videoOptionsTable.layout();
    }

    @Override
    public void show() {
        videoOptionsTable.setVisible(true);
    }

    @Override
    public void hide() {
        videoOptionsTable.setVisible(false);
    }

    @Override
    public void dispose() {
        atlas.dispose();
    }
}
