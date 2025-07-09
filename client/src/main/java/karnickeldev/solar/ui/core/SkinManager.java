package karnickeldev.solar.ui.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author : KarnickelDev
 * @since : 07.07.2025
 **/
public class SkinManager implements Disposable {

    private static SkinManager instance;

    private static TextureAtlas skinAtlas;

    public static void init() {
        if(instance == null) {
            instance = new SkinManager();
            instance.reload(Gdx.graphics.getHeight());
        }
    }

    public static SkinManager get() {
        if(instance == null) throw new IllegalStateException("SkinManager not initialized");
        return instance;
    }

    private Skin skin;
    private final FontManager fontManager;

    private float uiScale = 1f;

    private SkinManager() {
        skin = new Skin();
        fontManager = new FontManager();
        skinAtlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));
    }

    public Skin getSkin() {
        return skin;
    }

    public FontManager getFontManager() {
        return fontManager;
    }

    private void computeUIScale(int appHeight) {
        uiScale = appHeight / (float)UI.VIRTUAL_HEIGHT;
    }

    public void reload(int appHeight) {
        computeUIScale(appHeight);

        // Font

        fontManager.clearCache();

        BitmapFont regular = fontManager.getFont(14, false);
        BitmapFont bold = fontManager.getFont(14, true);

        // Textures
        skin.addRegions(skinAtlas);

        // Label style
        Label.LabelStyle labelStyleRegular = new Label.LabelStyle(regular, UI.WHITE);
        Label.LabelStyle labelStyleBold = new Label.LabelStyle(bold, UI.WHITE);
        skin.add("default", labelStyleRegular);
        skin.add("bold", labelStyleBold);


        // Button style
        NinePatchDrawable up = new NinePatchDrawable(new NinePatch(skinAtlas.findRegion("default-round"), 4, 4, 4, 4));
        NinePatchDrawable down = new NinePatchDrawable(new NinePatch(skinAtlas.findRegion("default-round"), 4, 4, 4, 4));
        NinePatchDrawable border = new NinePatchDrawable(new NinePatch(skinAtlas.findRegion("default-highlight"), 4, 4, 4, 4));
        skin.add("up", up);
        skin.add("down", down);
        skin.add("border", border);

        TextButton.TextButtonStyle buttonStyleReg = new TextButton.TextButtonStyle();
        buttonStyleReg.up = up;
        buttonStyleReg.down = down;
        buttonStyleReg.font = regular;
        buttonStyleReg.checkedFontColor = UI.WHITE;
        buttonStyleReg.fontColor = UI.WHITE;
        buttonStyleReg.overFontColor = UI.WHITE.cpy().mul(1.25f);
        buttonStyleReg.checkedOverFontColor = UI.WHITE.cpy().mul(1.25f);
        skin.add("default", buttonStyleReg);

        TextButton.TextButtonStyle buttonStyleBold = new TextButton.TextButtonStyle();
        buttonStyleBold.up = up;
        buttonStyleBold.down = down;
        buttonStyleBold.font = bold;
        buttonStyleBold.fontColor = UI.WHITE;
        buttonStyleBold.overFontColor = UI.WHITE.cpy().mul(1.25f);
        buttonStyleBold.checkedFontColor = buttonStyleBold.overFontColor;
        buttonStyleBold.checked = border;
        skin.add("bold", buttonStyleBold);

        TextButton.TextButtonStyle buttonStyleToggle = new TextButton.TextButtonStyle();
        buttonStyleToggle.up = up;
        buttonStyleToggle.down = down;
        buttonStyleToggle.font = bold;
        buttonStyleToggle.fontColor = UI.WHITE;
        buttonStyleToggle.overFontColor = UI.WHITE;
        buttonStyleToggle.checkedFontColor = buttonStyleBold.overFontColor;
        buttonStyleToggle.checked = border;
        skin.add("toggle", buttonStyleToggle);

        NinePatchDrawable selection = new NinePatchDrawable(new NinePatch(skinAtlas.findRegion("selection")));
        List.ListStyle listStyle = new List.ListStyle(regular, UI.DARK_RED, UI.WHITE, selection);
        skin.add("default", listStyle);

        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        skin.add("default", scrollPaneStyle);

        SelectBox.SelectBoxStyle selectBoxStyle = new SelectBox.SelectBoxStyle(regular, UI.WHITE, up, scrollPaneStyle,listStyle);
        skin.add("default", selectBoxStyle);

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = regular;
        textFieldStyle.fontColor = UI.WHITE;
        textFieldStyle.disabledFontColor = Color.RED;
        textFieldStyle.selection = selection;
        textFieldStyle.cursor = new NinePatchDrawable(new NinePatch(skinAtlas.findRegion("cursor")));
        textFieldStyle.background = up;
        skin.add("default", textFieldStyle);

//        NinePatchDrawable on = new NinePatchDrawable(new NinePatch(skinAtlas.findRegion("check-on")));
//        NinePatchDrawable off = new NinePatchDrawable(new NinePatch(skinAtlas.findRegion("check-off")));
//        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle(on, off, regular, UI.WHITE);
//        skin.add("default", checkBoxStyle);

    }

    public void dispose() {
        skin.dispose();
        fontManager.dispose();
        skinAtlas.dispose();
    }
}
