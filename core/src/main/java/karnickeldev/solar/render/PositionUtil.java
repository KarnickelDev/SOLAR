package karnickeldev.solar.render;

import karnickeldev.solar.physics.Vector2D;

public class PositionUtil {

    public static Vector2D getRelative(Vector2D vector, Vector2D origin) {
        return new Vector2D(vector.getX() - origin.getX(),vector.getY() - origin.getY());
    }


}
