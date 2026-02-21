package karnickeldev.solar.render.camera;

import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.util.WorldPos;

/**
 * @author KarnickelDev
 * @since 14.02.2026
 **/
public final class CameraState {

    // cam position in mm
    public final WorldPos pos = new WorldPos();

    // rotation in radians
    public double rotationRad = 0;

    public double logZoom = Math.log(1e4);
    public double zoom = Math.exp(logZoom);
    public double targetLogZoom = logZoom;

    // cursor anchor
    public final Vector2D zoomAnchorPx = new Vector2D();
}


