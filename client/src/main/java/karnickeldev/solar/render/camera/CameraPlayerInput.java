package karnickeldev.solar.render.camera;

import karnickeldev.solar.physics.Vector2D;

/**
 * @author KarnickelDev
 * @since 16.02.2026
 **/
public final class CameraPlayerInput {

    /** direction in screen space, not normalized yet */
    public double dirX, dirY;

    public float rotationRad = 0.0f;

    // zoom intent
    public double zoomImpulse = 0.0;
    public final Vector2D zoomCursor = new Vector2D().zero();

    public boolean panning = false;

    public void clear() {
        zoomImpulse = 0.0;
        rotationRad = 0.0f;
        dirX = dirY = 0.0;
        zoomCursor.zero();
        panning = false;
    }
}
