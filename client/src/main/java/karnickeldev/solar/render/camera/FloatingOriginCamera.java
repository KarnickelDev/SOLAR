package karnickeldev.solar.render.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import karnickeldev.solar.ecs.components.client.HCSClientSystem;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.util.MathUtil;

public class FloatingOriginCamera {

    public static final byte NO_MOVE = 0b0000;
    public static final byte UP = 0b0001;
    public static final byte DOWN = 0b0010;
    public static final byte LEFT = 0b0100;
    public static final byte RIGHT = 0b1000;

    private static final float CAMERA_MOVEMENT_TICK_RATE = (1f / 128);

    private static final float MIN_SPEED = 1f;
    private static final float MAX_SPEED = 10f;
    private static final float ACCELERATION = 5.2f;
    private static final float DECELERATION = 18f;
    private final Vector2D directionVec = new Vector2D();
    private final Vector2D moveDirection = new Vector2D();
    private final Vector2D origin;
    private final Vector2D prevPosition;
    private final Vector2D renderOrigin;
    private final Matrix4 projectionMatrix = new Matrix4();
    HCSClientSystem hcsClientSystem;
    Vector2D rotationCenter = new Vector2D();
    private float viewportWidth;
    private float viewportHeight;
    private double accumulator = 0;
    private float speed = MIN_SPEED;
    private byte direction = NO_MOVE;
    private boolean moveRequested = false;
    private double zoom;
    private float rotationDegrees;
    private float targetRotationDegrees;

    public FloatingOriginCamera(float viewportWidth, float viewportHeight, HCSClientSystem hcsClient) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;

        this.hcsClientSystem = hcsClient;

        this.origin = new Vector2D();
        this.prevPosition = new Vector2D();
        this.renderOrigin = new Vector2D();

        setZoom(1f);
    }

    /**
     * Updates the Camera, necessary to apply any changes
     */
    public void update() {
        viewportHeight = Gdx.graphics.getHeight();
        viewportWidth = Gdx.graphics.getWidth();

        // execute camera movement
        moveHelper();

        renderOrigin.set(prevPosition).lerp(origin, (float) (accumulator / CAMERA_MOVEMENT_TICK_RATE));

        updateProjectionMatrix(viewportWidth, viewportHeight);
    }

    /**
     * Get Camera Position in World-Space
     *
     * @return Camera Origin
     */
    public Vector2D getOrigin() {
        return origin;
    }

    /**
     * Get Camera Render-Position in World-Space
     *
     * @return Camera Render-Origin
     */
    public Vector2D getRenderOrigin() {
        return renderOrigin;
    }

    /**
     * Converts World-Coordinates to Screen-Coordinates
     *
     * @param worldPos World Coordinates
     * @return A Vector of Screen-Coordinates
     */
    public Vector2D project(Vector2D worldPos) {
        // Compute camera-local coordinates
        double localX = worldPos.getX() - renderOrigin.getX();
        double localY = worldPos.getY() - renderOrigin.getY();

        // Apply rotation
        float radians = (float) Math.toRadians(rotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double rotatedX = localX * cos - localY * sin;
        double rotatedY = localX * sin + localY * cos;

        // Apply zoom and move to screen center
        return new Vector2D(
            rotatedX / zoom + viewportWidth / 2.0,
            -rotatedY / zoom + viewportHeight / 2.0
        );
    }

    /**
     * Converts World-Coordinates to Screen-Coordinates
     *
     * @param screenPos World Coordinates
     * @return A Vector of Screen-Coordinates
     */
    public Vector2D unproject(Vector2D screenPos) {
        // Convert from screen space to local, zoomed space
        double dx = (screenPos.getX() - viewportWidth / 2.0) * zoom;
        double dy = -(screenPos.getY() - viewportHeight / 2.0) * zoom;

        // Inverse rotation
        float radians = (float) Math.toRadians(rotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double unrotatedX = dx * cos + dy * sin;
        double unrotatedY = -dx * sin + dy * cos;

        return new Vector2D(
            renderOrigin.getX() + unrotatedX,
            renderOrigin.getY() + unrotatedY
        );
    }


    /**
     * Set Camera Position
     *
     * @param x X Coordinate
     * @param y Y Coordinate
     */
    public void setPosition(double x, double y) {
        origin.set(x, y);
    }

    /**
     * Set Camera Position
     *
     * @param position Position
     */
    public void setPosition(Vector2D position) {
        origin.set(position);
    }

    /**
     * Sets the Cameras Rotation
     *
     * @param angle Angle
     */
    public void setRotation(float angle) {
        targetRotationDegrees = MathUtil.normalizeRotationDeg(angle);
    }

    /**
     * Changes rotation by adding an angle
     *
     * @param angleDelta Angle to be added to rotation
     */
    public void rotate(float angleDelta) {
        targetRotationDegrees = MathUtil.normalizeRotationDeg(
            targetRotationDegrees + (angleDelta * Gdx.graphics.getDeltaTime())
        );
    }

    /**
     * Signals camera to move, only gets executed on camera.update();
     *
     * @param direction Direction to move, use bitwise or to combine directions
     */
    public void move(byte direction) {
        if (!moveRequested) {
            this.direction = direction;
            moveRequested = true;
        } else this.direction |= direction;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double newZoom) {
        this.zoom = newZoom;
    }

    /**
     * Applies a zoom value towards screenPos and smooths changes
     *
     * @param newZoom   New Zoom Value
     * @param screenPos Position to zoom toward
     */
    public void zoomToward(double newZoom, Vector2D screenPos) {
        Vector2D worldBefore = unproject(screenPos);

        double dt = Math.max(Gdx.graphics.getDeltaTime(), CAMERA_MOVEMENT_TICK_RATE);
        zoom = MathUtil.lerp(zoom, newZoom, 10 * dt);

        Vector2D worldAfter = unproject(screenPos);

        Vector2D delta = worldBefore.subtract(worldAfter);
        origin.add(delta.getX(), delta.getY());
    }


    public void updateProjectionMatrix(float screenWidth, float screenHeight) {
        double halfWidth = (screenWidth / 2.0) * zoom;
        double halfHeight = (screenHeight / 2.0) * zoom;

        float left = (float) (-halfWidth);
        float right = (float) (+halfWidth);
        float bottom = (float) (-halfHeight);
        float top = (float) (+halfHeight);

        projectionMatrix.setToOrtho2D(left, bottom, right - left, top - bottom);


        // translate so the pivot is at the origin
        //projectionMatrix.translate((float) renderOrigin.getX(), (float) renderOrigin.getY(), 0);

        // rotate about Z
        projectionMatrix.rotate(0, 0, 1, rotationDegrees);

        // translate back
        //projectionMatrix.translate((float) -renderOrigin.getX(), (float) -renderOrigin.getY(), 0);
    }

    public Matrix4 getCombinedMatrix() {
        return projectionMatrix;
    }

    public float getViewportWidth() {
        return viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    /*
        Fixed time-step method to execute camera movement/rotation, applying smoothing with lerp
         */
    private void moveHelper() {
        if (!moveRequested) direction = 0b0000;

        accumulator += Gdx.graphics.getDeltaTime();

        short x = 0;
        short y = 0;
        if ((direction & UP) == UP) y += 1;
        if ((direction & DOWN) == DOWN) y -= 1;
        if ((direction & RIGHT) == RIGHT) x += 1;
        if ((direction & LEFT) == LEFT) x -= 1;

        directionVec.set(x, y);
        if (directionVec.len2() > 0) directionVec.nor();


        while (accumulator >= CAMERA_MOVEMENT_TICK_RATE) {
            // Smooth the direction vector to avoid snappy turns
            float directionSmoothness = 7.3f;
            moveDirection.lerp(directionVec, directionSmoothness * CAMERA_MOVEMENT_TICK_RATE);
            if (directionVec.isZero()) {
                // Decelerate speed when no input
                speed = Math.max(MIN_SPEED, speed - (DECELERATION * CAMERA_MOVEMENT_TICK_RATE));
            } else {
                // Accelerate up to max
                speed = Math.min(MAX_SPEED, speed + (ACCELERATION * CAMERA_MOVEMENT_TICK_RATE));
            }

            rotationDegrees = targetRotationDegrees;

            prevPosition.set(origin);
            float radians = (float) Math.toRadians(MathUtil.normalizeRotationDeg(-rotationDegrees));
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);

            double rotatedDX = moveDirection.getX() * cos - moveDirection.getY() * sin;
            double rotatedDY = moveDirection.getX() * sin + moveDirection.getY() * cos;

            origin.add(rotatedDX * speed * zoom, rotatedDY * speed * zoom);

            accumulator -= CAMERA_MOVEMENT_TICK_RATE;
        }
        moveRequested = false;
    }

}

