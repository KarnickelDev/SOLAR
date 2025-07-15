package karnickeldev.solar.ui.components;

import com.badlogic.gdx.scenes.scene2d.Group;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;

/**
 * @author : KarnickelDev
 * @since : 08.07.2025
 **/
public interface UIComponent {

    @NotNull
    Group getGroup();

    default void show() {
        getGroup().setVisible(true);
    }

    default void hide() {
        getGroup().setVisible(false);
    }

    void update(float delta);

    void resize(int width, int height);

}
