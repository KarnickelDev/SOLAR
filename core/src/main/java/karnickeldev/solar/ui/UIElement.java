package karnickeldev.solar.ui;

import com.badlogic.gdx.utils.Disposable;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 06.11.2024
 */
public interface UIElement extends Disposable {

    float getWidth();
    float getHeight();
    float getX();
    float getY();

    void resizeUI(int width, int height);

    void show();

    void hide();

}
