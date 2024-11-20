package karnickeldev.solar.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import karnickeldev.solar.core.Logger;
import karnickeldev.solar.core.MainMenuScreen;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.settings.Resolution;
import karnickeldev.solar.settings.Settings;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 27.10.2024
 */
public class OptionsMenu implements UIElement {

    private Stage stage;
    private Table table;
    private Skin skin;

    Label.LabelStyle labelStyle;

    public OptionsMenu(Stage stage, int width, int height) {
        this.stage = stage;
        this.table = new Table();

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        TextureAtlas atlas = new TextureAtlas("uiskin.atlas");

        table.setSize(0.6f * width, 0.7f * height);
        table.setPosition(1.05f*MainMenuScreen.getMainMenuWidth(), 0.5f * (height - table.getHeight()));
        table.setClip(true);
        table.background(new NinePatchDrawable(new NinePatch(new TextureRegion(atlas.findRegion("default-round")), 4, 4, 4, 4)));

        Label.LabelStyle titleLabelStyle = new Label.LabelStyle();
        titleLabelStyle.font = Fonts.BIG;
        titleLabelStyle.fontColor = Color.WHITE;
        titleLabelStyle.background = new NinePatchDrawable(new NinePatch(new TextureRegion(atlas.findRegion("default-round")), 4, 4, 4, 4));

        Label title = new Label("Options", titleLabelStyle);
        title.setEllipsis(true);
        title.setAlignment(Align.center);


        table.pad(0);
        table.padTop(0);
        table.padBottom(0);
        table.padLeft(0);
        table.padRight(0);
        table.top().left();


        initOptionsMenu();



        stage.addActor(table);

        hide();
    }


    public void hide() {
        table.setVisible(false);
    }

    public void show() {
        table.setVisible(true);
    }


    @Override
    public void resizeUI(int width, int height) {

    }

    private void initOptionsMenu() {

        labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.MEDIUM;
        labelStyle.fontColor = Color.WHITE;

        float elementHeight = 0.04f * table.getHeight();
        float spacingHeight = 0.06f * table.getHeight();

        table.add().height(spacingHeight).row();

        Label resolutionLabel = new Label("Resolution:", labelStyle);
        resolutionLabel.setAlignment(Align.left);
        resolutionLabel.setStyle(labelStyle);

        SelectBox<String> resolutionSelector = new SelectBox<>(skin);
        resolutionSelector.getStyle().font = Fonts.MEDIUM;
        resolutionSelector.getList().getStyle().font = Fonts.MEDIUM;
        resolutionSelector.setItems(Resolution.SUPPORTED_RESOLUTIONS);
        resolutionSelector.setAlignment(Align.left);
        String currResolution = SolarMain.getInstance().getSettingsManager().getSettings().getScreenWidth()
            + "x" + SolarMain.getInstance().getSettingsManager().getSettings().getScreenHeight();
        resolutionSelector.setSelected(currResolution);

        HorizontalGroup resolutionGroup = new HorizontalGroup();
        resolutionGroup.space(10);
        resolutionGroup.addActor(resolutionLabel);
        resolutionGroup.addActor(resolutionSelector);

        Label vsyncLabel = new Label("VSync:", labelStyle);
        vsyncLabel.setAlignment(Align.left);

        CheckBox vsyncbox = new CheckBox("Vsync", skin);
        vsyncbox.getImage().setScaling(Scaling.fill);
        vsyncbox.getImageCell().size(elementHeight, elementHeight);
        vsyncbox.setChecked(SolarMain.getInstance().getSettingsManager().getSettings().isVsync());

        HorizontalGroup vsyncGroup = new HorizontalGroup();
        vsyncGroup.space(10);
        vsyncGroup.addActor(vsyncLabel);
        vsyncGroup.addActor(vsyncbox);

        Label fullscreenLabel = new Label("Fullscreen:", labelStyle);
        fullscreenLabel.setAlignment(Align.left);

        CheckBox fullscreen = new CheckBox("Fullscreen", skin);
        fullscreen.getImage().setScaling(Scaling.fill);
        fullscreen.getImageCell().size(elementHeight, elementHeight);
        fullscreen.setChecked(SolarMain.getInstance().getSettingsManager().getSettings().isFullscreen());

        HorizontalGroup fullscreenGroup = new HorizontalGroup();
        fullscreenGroup.space(10);
        fullscreenGroup.addActor(fullscreenLabel);
        fullscreenGroup.addActor(fullscreen);

        Label borderlessLabel = new Label("Borderless:", labelStyle);
        vsyncLabel.setAlignment(Align.left);

        CheckBox borderless = new CheckBox("Borderless", skin);
        borderless.getImage().setScaling(Scaling.fill);
        borderless.getImageCell().size(elementHeight, elementHeight);
        borderless.setChecked(SolarMain.getInstance().getSettingsManager().getSettings().isBorderless());

        HorizontalGroup borderlessGroup = new HorizontalGroup();
        borderlessGroup.space(10);
        borderlessGroup.addActor(borderlessLabel);
        borderlessGroup.addActor(borderless);


        Label fpsLimitLabel = new Label("FpsLimit:", labelStyle);
        fpsLimitLabel.setAlignment(Align.left);

        Label fpsTextLabel = new Label("", labelStyle);
        fpsTextLabel.setAlignment(Align.left);
        fpsTextLabel.setFontScale(0.5f);
        fpsTextLabel.setText(SolarMain.getInstance().getSettingsManager().getSettings().getFpsLimit());

        Slider fpsLimit = new Slider(10, 500, 10, false, skin);
        fpsLimit.setVisualPercent(SolarMain.getInstance().getSettingsManager().getSettings().getFpsLimit()/fpsLimit.getMaxValue());
        fpsLimit.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                fpsTextLabel.setText("" + fpsLimit.getValue());
            }
        });

        HorizontalGroup fpsLimitGroup = new HorizontalGroup();
        fpsLimitGroup.space(10);
        fpsLimitGroup.addActor(fpsLimitLabel);
        fpsLimitGroup.addActor(fpsLimit);
        fpsLimitGroup.addActor(fpsTextLabel);

        // Add both groups to the same row in the table with spacing between them
        table.add(resolutionGroup).height(elementHeight).width(0.43f*table.getWidth()).padLeft(0.02f * table.getWidth());
        table.add().width(0.1f*table.getWidth());
        table.add(vsyncGroup).height(elementHeight).width(0.43f*table.getWidth()).padRight(0.02f * table.getWidth());
        table.row();
        table.add().height(spacingHeight);
        table.row();

        table.add(fullscreenGroup).height(elementHeight).width(0.43f*table.getWidth()).padLeft(0.02f * table.getWidth());
        table.add().width(0.1f*table.getWidth());
        table.add(borderlessGroup).height(elementHeight).width(0.43f*table.getWidth()).padRight(0.02f * table.getWidth());
        table.row();
        table.add().height(spacingHeight);
        table.row();

        table.add(fpsLimitGroup).height(elementHeight).width(0.43f*table.getWidth()).padLeft(0.02f * table.getWidth());
        table.row();
        table.add().height(spacingHeight);
        table.row();

        TextButton.TextButtonStyle buttonStyle = new TextButton("", skin).getStyle();
        buttonStyle.font = Fonts.MEDIUM;

        MenuButton back = new MenuButton("Back", buttonStyle, this::hide);

        MenuButton apply = new MenuButton("Apply", buttonStyle, () -> {
            Settings settings = new Settings(SolarMain.getInstance().getSettingsManager().getSettings());
            String resolutionString = resolutionSelector.getSelected();
            Resolution resolution = null;
            try {
                resolution = Resolution.extractResolution(resolutionString);
            } catch (IllegalArgumentException e) {
                Logger.error(Logger.UI, "", e);
            }

            if(resolution != null) {
                settings.setScreenWidth(resolution.getWidth());
                settings.setScreenHeight(resolution.getHeight());
                settings.setVsync(vsyncbox.isChecked());
                settings.setFullscreen(fullscreen.isChecked());
                settings.setBorderless(borderless.isChecked());
                settings.setFpsLimit((int)fpsLimit.getValue());
                if(settings.getFpsLimit() >= 500) settings.setFpsLimit(1200);

                if(!settings.equals(SolarMain.getInstance().getSettingsManager().getSettings())) {
                    SolarMain.getInstance().getSettingsManager().updateSettings(settings);
                    SolarMain.getInstance().getSettingsManager().saveToFile();
                }
            }

            hide();
        });

        table.add(back).height(elementHeight).width(0.43f*table.getWidth()).padLeft(0.02f * table.getWidth());
        table.add().width(0.1f*table.getWidth());
        table.add(apply).height(elementHeight).width(0.43f*table.getWidth()).padRight(0.02f * table.getWidth());

        //table.debug();
    }

    @Override
    public void dispose() {
        hide();
        table.remove();
    }

}
