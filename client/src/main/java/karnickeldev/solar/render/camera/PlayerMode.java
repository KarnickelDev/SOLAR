package karnickeldev.solar.render.camera;

import com.badlogic.gdx.Gdx;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.util.MathUtil;
import karnickeldev.solar.util.SplitCoord;
import karnickeldev.solar.util.SplitCoordMath;

/**
 * @author KarnickelDev
 * @since 14.02.2026
 **/
public final class PlayerMode implements CameraMode {

    // screen speed in screen-sizes per second
    private static final float MAX_SPEED = 0.6f;
    private static final float MIN_SPEED = 0.01f;
    private static final float ACCEL = 0.42f;
    private static final float DECEL = 0.8f;

    // zoom tuning
    private static final double ZOOM_STEP = 0.15;     // per scroll notch
    private static final double ZOOM_RESPONSE = 12.0; // higher = snappier

    // state
    private final Vector2D targetDir = new Vector2D();
    private final Vector2D smoothDir = new Vector2D();

    private double speed_screen_per_s = 0.0;

    @Override
    public void onUpdate(FloatingOriginCamera.CameraContext ctx, float dt) {
        // rotation
        ctx.state().rotationRad = (float) MathUtil.normalizeRotationRad(ctx.state().rotationRad + ctx.playerInput().rotationRad);

        double dx, dy;
        if(ctx.playerInput().panning) {
            dx = ctx.playerInput().dirX * ctx.state().zoom;
            dy = ctx.playerInput().dirY * ctx.state().zoom;
        } else {
            // direction
            targetDir.set(ctx.playerInput().dirX, ctx.playerInput().dirY);

            if(!targetDir.isZero()) targetDir.nor();
            smoothDir.lerp(targetDir, 7.5f * dt);

            // move
            float viewportSizePx = (float) Math.hypot(ctx.viewportWidth(), ctx.viewportHeight());
            double speed = computePlayerSpeed(ctx.state(), dt, !targetDir.isZero(), viewportSizePx);

            dx = smoothDir.getX() * speed * dt;
            dy = smoothDir.getY() * speed * dt;
        }

        double cos = ctx.projector().cos_minus;
        double sin = ctx.projector().sin_minus;

        ctx.state().pos.lx += (dx * cos - dy * sin);
        ctx.state().pos.ly += (dx * sin + dy * cos);

        SplitCoordMath.normalize(ctx.state().pos);

        updateZoom(ctx.playerInput(), ctx.state(), ctx.projector(), dt);
    }

    private void updateZoom(CameraPlayerInput signal, CameraState state, CameraProjector projector, double delta) {
        if (Math.abs(signal.zoomImpulse) > 1e-12) {
            state.zoomAnchorPx.set((int)signal.zoomCursor.getX(), (int)signal.zoomCursor.getY());
            state.targetLogZoom += signal.zoomImpulse * ZOOM_STEP;
            state.targetLogZoom = MathUtil.clamp(
                state.targetLogZoom,
                Math.log(FloatingOriginCamera.MIN_ZOOM),
                Math.log(FloatingOriginCamera.MAX_ZOOM)
            );
        }

        double response = 1.0 - Math.exp(-ZOOM_RESPONSE * delta);
        double oldZoom = state.zoom;
        state.logZoom += (state.targetLogZoom - state.logZoom) * response;
        state.zoom = Math.exp(state.logZoom);

        if (Math.abs(state.zoom - oldZoom) < 1e-12) return;

        double dxScreen = state.zoomAnchorPx.getX() - Gdx.graphics.getWidth() / 2.0;
        double dyScreen = state.zoomAnchorPx.getY() - Gdx.graphics.getHeight() / 2.0;

        double dZoom = state.zoom - oldZoom;
        double deltaX = dxScreen * dZoom;
        double deltaY = -dyScreen * dZoom;

        double rdx = deltaX * projector.cos_minus - deltaY * projector.sin_minus;
        double rdy = deltaX * projector.sin_minus + deltaY * projector.cos_minus;

        state.pos.lx -= rdx;
        state.pos.ly -= rdy;

        SplitCoordMath.normalize(state.pos);
    }


    private double computePlayerSpeed(CameraState state, double delta, boolean hasDirection, float viewportSizePx) {
        // accelerate in SCREEN space
        if (!hasDirection) {
            speed_screen_per_s = Math.max(MIN_SPEED, speed_screen_per_s - DECEL * delta);
        } else {
            speed_screen_per_s = Math.min(MAX_SPEED, speed_screen_per_s + ACCEL * delta);
        }

        // convert screen -> world
        return speed_screen_per_s * viewportSizePx * state.zoom;
    }

    @Override
    public void onEnter(FloatingOriginCamera.CameraContext ctx) {
        speed_screen_per_s = 0.0;
        smoothDir.zero();
        targetDir.zero();
    }

    @Override
    public void onExit(FloatingOriginCamera.CameraContext ctx) {
        speed_screen_per_s = 0.0;
        smoothDir.zero();
        targetDir.zero();
        ctx.playerInput().clear();
    }
}


