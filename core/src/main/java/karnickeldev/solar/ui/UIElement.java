package karnickeldev.solar.ui;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 06.11.2024
 */
public interface UIElement {

    float getWidth();
    float getHeight();
    float getX();
    float getY();

    void resizeUI(int width, int height);

    void show();

    void hide();

    void dispose();

}
