package karnickeldev.solar.ui.pausemenu;

import com.badlogic.gdx.Gdx;
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
import karnickeldev.solar.ui.UIManager;

public class VideoOptionsMenu implements UIElement {

    private Table videoOptionsTable;

    private final Label.LabelStyle labelStyle;

    private final TextureAtlas atlas;
    private final NinePatchDrawable background;

    private Label resolutionLabel;
    private SelectBox<String> resolution;

    private Label vsyncLabel;
    private CheckBox vsyncBox;

    private Label borderlessLabel;
    private CheckBox borderlessBox;

    private Label fullscreenLabel;
    private CheckBox fullscreenBox;

    private Label fpsLimitLabel;
    private TextField fpsLimitField;

    private MenuButton apply;

    private final OptionsMenu parent;

    protected VideoOptionsMenu(Table rootTable, Skin skin, OptionsMenu parent) {
        this.parent = parent;

        atlas = new TextureAtlas("uiskin.atlas");
        background = new NinePatchDrawable(new NinePatch(new TextureRegion(atlas.findRegion("default-round")),
            4, 4, 4, 4));

        videoOptionsTable = rootTable;
        videoOptionsTable.setClip(true);
        videoOptionsTable.setBackground(background);
        videoOptionsTable.top().left();

        labelStyle = new Label.LabelStyle();
        labelStyle.fontColor = Color.WHITE;
        labelStyle.font = Fonts.MEDIUM;

        resolutionLabel = new Label("Resolution:", labelStyle);
        resolution = new SelectBox<>(skin);

        vsyncLabel = new Label("V-Sync:", labelStyle);
        vsyncBox = new CheckBox("", skin);

        borderlessLabel = new Label("Borderless:", labelStyle);
        borderlessBox = new CheckBox("", skin);

        fullscreenLabel = new Label("Fullscreen:", labelStyle);
        fullscreenBox = new CheckBox("", skin);

        fpsLimitLabel = new Label("FPS-Limit:", labelStyle);
        fpsLimitField = new TextField("60", skin);

        apply = new MenuButton("Apply", skin, () -> {
            Settings newSettings = new Settings(SolarMain.getInstance().getSettings());

            Resolution res = Resolution.extractResolution(resolution.getSelected());
            newSettings.setScreenWidth(res.getWidth());
            newSettings.setScreenHeight(res.getHeight());

            newSettings.setVsync(vsyncBox.isChecked());
            newSettings.setBorderless(borderlessBox.isChecked());
            newSettings.setFullscreen(fullscreenBox.isChecked());

            String fpsLimit = fpsLimitField.getText();
            int newFPSLimit = SolarMain.getInstance().getSettings().getFpsLimit();
            try {
                newFPSLimit = Integer.parseInt(fpsLimit);
            } catch (NumberFormatException ignored) {}
            newSettings.setFpsLimit(newFPSLimit);

            if(!newSettings.equals(SolarMain.getInstance().getSettings())) {
                SolarMain.getInstance().getSettingsManager().updateSettings(newSettings);
                SolarMain.getInstance().getSettingsManager().saveToFile();
            }

            int subMenu = parent.getSelectedSubMenu();
            parent.hide();
            parent.show();
            parent.selectSubMenu(subMenu);
            SolarMain.getInstance().getUIManager().resize(
                SolarMain.getInstance().getSettings().getScreenWidth(),
                SolarMain.getInstance().getSettings().getScreenHeight()
            );
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
        float optionsWidth = videoOptionsTable.getWidth();
        float optionsHeight = videoOptionsTable.getHeight();

        float paddingWidth = 0.02f * optionsWidth;
        float paddingMiddle = 0.12f * optionsWidth;
        float checkboxSize = 0.05f * optionsWidth;
        float elementWidth = (1f - paddingMiddle - 4f*paddingWidth - checkboxSize) / 3f;

        videoOptionsTable.clear();
        videoOptionsTable.top().left();

        videoOptionsTable.add().width(paddingWidth).height(checkboxSize);
        videoOptionsTable.add().width(elementWidth).height(checkboxSize);
        videoOptionsTable.add().width(paddingWidth).height(checkboxSize);
        videoOptionsTable.add().width(elementWidth).height(checkboxSize);

        videoOptionsTable.add().width(paddingMiddle).height(checkboxSize);

        videoOptionsTable.add().width(elementWidth).height(checkboxSize);
        videoOptionsTable.add().width(paddingWidth).height(checkboxSize);
        videoOptionsTable.add().width(checkboxSize).height(checkboxSize);
        videoOptionsTable.add().width(paddingWidth).height(checkboxSize);
        videoOptionsTable.row();


        labelStyle.font = Fonts.MEDIUM;
        labelStyle.fontColor = Color.WHITE;


        resolutionLabel.setStyle(labelStyle);
        resolution.setItems(Resolution.SUPPORTED_RESOLUTIONS);

        List.ListStyle listStyle = resolution.getList().getStyle();
        listStyle.font = Fonts.SMALL;
        resolution.getList().setStyle(listStyle);
        SelectBox.SelectBoxStyle selectBoxStyle = resolution.getStyle();
        selectBoxStyle.font = Fonts.SMALL;
        resolution.setStyle(selectBoxStyle);

        vsyncLabel.setStyle(labelStyle);
        vsyncBox.getImage().setScaling(Scaling.fill);
        vsyncBox.getImageCell().fill();

        borderlessLabel.setStyle(labelStyle);
        borderlessBox.getImage().setScaling(Scaling.fill);
        borderlessBox.getImageCell().fill();

        fullscreenLabel.setStyle(labelStyle);
        fullscreenBox.getImage().setScaling(Scaling.fill);
        fullscreenBox.getImageCell().fill();

        fpsLimitLabel.setStyle(labelStyle);
        TextField.TextFieldStyle textFieldStyle = fpsLimitField.getStyle();
        textFieldStyle.font = Fonts.SMALL;
        fpsLimitField.setStyle(textFieldStyle);
        fpsLimitField.clearSelection();

        videoOptionsTable.add().height(checkboxSize);
        videoOptionsTable.add(resolutionLabel);
        videoOptionsTable.add();
        videoOptionsTable.add(resolution);
        videoOptionsTable.add();
        videoOptionsTable.add(vsyncLabel);
        videoOptionsTable.add();
        videoOptionsTable.add(vsyncBox);
        videoOptionsTable.add();
        videoOptionsTable.row();
        videoOptionsTable.add().height(checkboxSize).row();

        videoOptionsTable.add().height(checkboxSize);
        videoOptionsTable.add(fpsLimitLabel);
        videoOptionsTable.add();
        videoOptionsTable.add(fpsLimitField);
        videoOptionsTable.add();
        videoOptionsTable.add(fullscreenLabel);
        videoOptionsTable.add();
        videoOptionsTable.add(fullscreenBox);
        videoOptionsTable.add();
        videoOptionsTable.row();
        videoOptionsTable.add().height(checkboxSize).row();

        videoOptionsTable.add().height(checkboxSize);
        videoOptionsTable.add();
        videoOptionsTable.add();
        videoOptionsTable.add();
        videoOptionsTable.add();
        videoOptionsTable.add(borderlessLabel);
        videoOptionsTable.add();
        videoOptionsTable.add(borderlessBox);
        videoOptionsTable.add();
        videoOptionsTable.row();
        videoOptionsTable.add().height(checkboxSize).row();

        apply.setFont(Fonts.MEDIUM);
        apply.getLabel().setAlignment(Align.left);

        videoOptionsTable.add().height(checkboxSize);
        videoOptionsTable.add(apply).fill().row();

        videoOptionsTable.layout();
    }

    public void update() {
        resolution.setSelected(Resolution.resolutionToString(
            SolarMain.getInstance().getSettings().getScreenWidth(),
            SolarMain.getInstance().getSettings().getScreenHeight()
        ));

        vsyncBox.setChecked(SolarMain.getInstance().getSettings().isVsync());
        fullscreenBox.setChecked(SolarMain.getInstance().getSettings().isFullscreen());
        borderlessBox.setChecked(SolarMain.getInstance().getSettings().isBorderless());

        fpsLimitField.setText("" + SolarMain.getInstance().getSettings().getFpsLimit());
    }

    @Override
    public void show() {
        update();
        videoOptionsTable.setVisible(true);
    }

    @Override
    public void hide() {
        videoOptionsTable.setVisible(false);
    }

    @Override
    public boolean isVisible() {
        return videoOptionsTable.isVisible();
    }

    @Override
    public void dispose() {
        atlas.dispose();
    }
}
