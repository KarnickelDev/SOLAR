package karnickeldev.solar.menus;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import karnickeldev.solar.core.SolarMain;

public class MenuInput extends InputAdapter {


    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.ESCAPE) {
            if(SolarMain.getInstance().getUIManager().getEscapeMenu().isVisible()) {
                SolarMain.getInstance().getUIManager().getEscapeMenu().hide();
            } else {
                SolarMain.getInstance().getUIManager().getEscapeMenu().show();
            }
            return true;
        }
        return false;
    }

}
