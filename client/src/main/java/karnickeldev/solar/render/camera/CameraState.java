package karnickeldev.solar.render.camera;

import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.util.SplitCoord;

/**
 * @author KarnickelDev
 * @since 14.02.2026
 **/
public final class CameraState {

    // cam position in mm
    public final SplitCoord pos = new SplitCoord();

    // rotation in radians
    public float rotationRad = 0f;

    public double logZoom = Math.log(1e4);
    public double zoom = Math.exp(logZoom);
    public double targetLogZoom = logZoom;

    // cursor anchor
    public final Vector2D zoomAnchorPx = new Vector2D();
}


