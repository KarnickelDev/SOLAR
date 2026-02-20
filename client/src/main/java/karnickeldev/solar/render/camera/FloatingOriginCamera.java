package karnickeldev.solar.render.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.util.SplitCoord;
import karnickeldev.solar.util.SplitCoordMath;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @author KarnickelDev
 * @since 16.02.2026
 **/
public final class FloatingOriginCamera {

    public static final float MIN_ZOOM = 1e-6f;
    public static final float MAX_ZOOM = 2e9f;

    public record CameraContext(
        CameraState state,
        CameraProjector projector,
        CameraPlayerInput playerInput,
        int viewportWidth,
        int viewportHeight
    ) {}

    private final CameraState state = new CameraState();
    private final CameraProjector projector = new CameraProjector();

    private final CameraPlayerInput playerInput = new CameraPlayerInput();

    private final Deque<CameraMode> modeStack = new LinkedList<>();

    public FloatingOriginCamera() {
        pushMode(new PlayerMode());
    }

    public void pushMode(CameraMode mode) {
        if(mode == null) return;
        modeStack.push(mode);
    }

    public CameraMode popMode() {
        if(modeStack.isEmpty()) return null;
        return modeStack.pop();
    }

    public CameraMode peekMode() {
        return modeStack.peek();
    }

    public void update(float frameDelta) {
        CameraContext ctx = new CameraContext(state, projector, playerInput, getViewportWidth(), getViewportHeight());

        assert modeStack.peek() != null;
        modeStack.peek().onUpdate(ctx, frameDelta);

        projector.update(state, getViewportWidth(), getViewportHeight());

        playerInput.clear();
    }

    public CameraPlayerInput getPlayerInput() {
        return playerInput;
    }

    public int getViewportWidth() {
        return Gdx.graphics.getWidth();
    }

    public int getViewportHeight() {
        return Gdx.graphics.getHeight();
    }

    public Matrix4 getCombinedMatrix() {
        return projector.getProjectionMatrix();
    }

    //#########################
    // API
    //#########################

    public long getOriginXmm() {
        return state.pos.sx * 150_000_000_000_000L + Math.round(state.pos.lx * 1e6);
    }

    public long getOriginYmm() {
        return state.pos.sy * 150_000_000_000_000L + Math.round(state.pos.ly * 1e6);
    }

    public double getZoom() {
        return state.zoom;
    }

    public float getRotation() {
        return state.rotationRad;
    }

    public Vector2D getRenderOrigin() {
        return new Vector2D(SplitCoordMath.toDoubleX(state.pos), SplitCoordMath.toDoubleY(state.pos));
    }

    public Vector2D project(double worldX, double worldY, Vector2D vec, double zoom) {
        return projector.project(worldX, worldY, vec, zoom);
    }

    public Vector2D project(Vector2D worldPos) {
        return project(worldPos.getX(), worldPos.getY(), new Vector2D(), state.zoom);
    }

    public Vector2D projectReuse(Vector2D worldPos) {
        return project(worldPos.getX(), worldPos.getY(), worldPos, state.zoom);
    }

    public Vector2D unproject(double screenX, double screenY, Vector2D vec, double zoom) {
        return projector.unproject(screenX, screenY, vec, zoom);
    }

    public Vector2D unproject(Vector2D screenPos) {
        return unproject(screenPos.getX(), screenPos.getY(), new Vector2D(), state.zoom);
    }

    public Vector2D unprojectReuse(Vector2D screenPos) {
        return unproject(screenPos.getX(), screenPos.getY(), screenPos, state.zoom);
    }

    public double projectLength(double worldLength) {
        return worldLength / state.zoom;
    }

    public double unprojectLength(double pixelLength) {
        return pixelLength * state.zoom;
    }

}
