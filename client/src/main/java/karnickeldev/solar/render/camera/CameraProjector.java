package karnickeldev.solar.render.camera;

import com.badlogic.gdx.math.Matrix4;
import karnickeldev.solar.physics.Vector2D;

/**
 * @author KarnickelDev
 * @since 16.02.2026
 **/
public final class CameraProjector {

    private final Vector2D renderOrigin = new Vector2D().zero();

    private final Matrix4 projectionMatrix = new Matrix4();

    private double viewportWidthHalf;
    private double viewportHeightHalf;

    /** cached sin of cam rotation [sin(+angle)]*/
    public double sin = 0;
    /** cached cos of cam rotation [cos(+angle)]*/
    public double cos = 1;
    /** cached sin of MINUS cam rotation [sin(-angle)]*/
    public double sin_minus = 0;
    /** cached cos of MINUS cam rotation [cos(-angle)]*/
    public double cos_minus = 1;

    public void update(CameraState state, float viewportWidth, float viewportHeight) {
        this.renderOrigin.set(
            state.x_mm * FloatingOriginCamera.WORLD_UNIT_PER_MM,
            state.y_mm * FloatingOriginCamera.WORLD_UNIT_PER_MM
        );
        this.viewportWidthHalf = viewportWidth * 0.5;
        this.viewportHeightHalf = viewportHeight * 0.5;

        sin = Math.sin(state.rotationRad);
        cos = Math.cos(state.rotationRad);
        sin_minus = Math.sin(-state.rotationRad);
        cos_minus = Math.cos(-state.rotationRad);

        float halfWidth = (float)(viewportWidthHalf * state.zoom);
        float halfHeight = (float)(viewportHeightHalf * state.zoom);
        projectionMatrix.setToOrtho2D(-halfWidth, -halfHeight, halfWidth * 2, halfHeight * 2);
        projectionMatrix.rotateRad(0, 0, 1, state.rotationRad);
    }

    public Matrix4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public Vector2D project(double worldX, double worldY, Vector2D vec, double zoom) {
        double localX = worldX - renderOrigin.getX();
        double localY = worldY - renderOrigin.getY();

        double rotatedX = localX * cos - localY * sin;
        double rotatedY = localX * sin + localY * cos;

        double invZoom = 1.0 / zoom;
        return vec.set(
            rotatedX * invZoom + viewportWidthHalf,
            -rotatedY * invZoom + viewportHeightHalf
        );
    }

    public Vector2D unproject(double screenX, double screenY, Vector2D vec, double zoom) {
        double dx = (screenX - viewportWidthHalf) * zoom;
        double dy = -(screenY - viewportHeightHalf) * zoom;

        double unrotatedX = dx * cos + dy * sin;
        double unrotatedY = -dx * sin + dy * cos;

        return vec.set(
            renderOrigin.getX() + unrotatedX,
            renderOrigin.getY() + unrotatedY
        );
    }

}
