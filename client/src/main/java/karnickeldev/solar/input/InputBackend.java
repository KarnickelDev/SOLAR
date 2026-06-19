package karnickeldev.solar.input;

import com.badlogic.gdx.utils.Disposable;

/**
 * @author KarnickelDev
 * @since 20.06.2026
 **/
public interface InputBackend extends Disposable {

    void init(InputManager input);

    void poll();

    @Override
    void dispose();
}
