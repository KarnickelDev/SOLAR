package karnickeldev.solar.ui;

/**
 * Project: SOLAR
 *
 * @author KarnickelDev
 * @since 06.11.2024
 */
public interface UIElement {

    void resizeUI(int width, int height);

    void show();

    void hide();

    void dispose();

}
