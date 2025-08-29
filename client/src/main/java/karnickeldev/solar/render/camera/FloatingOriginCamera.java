package karnickeldev.solar.render.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import karnickeldev.solar.ecs.systems.HCSClientSystem;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.util.MathUtil;

public class FloatingOriginCamera {

    private static final float DEG_TO_RAD = 0.01745329252f;

    public static final byte NO_MOVE = 0b0000;
    public static final byte UP = 0b0001;
    public static final byte DOWN = 0b0010;
    public static final byte LEFT = 0b0100;
    public static final byte RIGHT = 0b1000;

    private static final float CAMERA_MOVEMENT_TICK_RATE = (1f / 60);

    private static final float MIN_SPEED = 2f;
    private static final float MAX_SPEED = 25f;
    private static final float ACCELERATION = 12f;
    private static final float DECELERATION = 30f;
    private final Vector2D directionVec = new Vector2D();
    private final Vector2D moveDirection = new Vector2D();
    private final Vector2D origin;
    private final Vector2D prevOrigin;
    private final Vector2D renderOrigin;
    private final Matrix4 projectionMatrix = new Matrix4();
    HCSClientSystem hcsClientSystem;

    private float viewportWidth;
    private float viewportHeight;
    private double accumulator = 0;
    private float speed = MIN_SPEED;
    private byte direction = NO_MOVE;
    private boolean moveRequested = false;
    private double zoom;
    private double targetZoom;
    private double renderZoom;
    private double prevZoom;
    private float rotationDegrees;
    private float targetRotationDegrees;

    private final Vector2D zoomTargetPos = new Vector2D();

    public FloatingOriginCamera(float viewportWidth, float viewportHeight, HCSClientSystem hcsClient) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;

        this.hcsClientSystem = hcsClient;

        this.origin = new Vector2D();
        this.prevOrigin = new Vector2D();
        this.renderOrigin = new Vector2D();

        zoom = prevZoom = targetZoom = renderZoom = 1f;
    }

    /**
     * Updates the Camera, necessary to apply any changes
     */
    public void update() {
        viewportHeight = Gdx.graphics.getHeight();
        viewportWidth = Gdx.graphics.getWidth();

        // execute camera movement
        moveHelper();

        renderOrigin.set(prevOrigin).lerp(origin, (float) (accumulator / CAMERA_MOVEMENT_TICK_RATE));

        renderZoom = MathUtil.lerp(prevZoom, zoom, accumulator / CAMERA_MOVEMENT_TICK_RATE);

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
     * @param worldX World X Coordinate
     * @param worldY World Y Coordinate
     * @param vec Vector to store and return result
     * @return A Vector of Screen-Coordinates
     */
    public Vector2D project(double worldX, double worldY, Vector2D vec, double zoom) {
        // Compute camera-local coordinates
        double localX = worldX - renderOrigin.getX();
        double localY = worldY - renderOrigin.getY();

        // Apply rotation
        float radians = rotationDegrees * DEG_TO_RAD;
        float cos = MathUtils.cos(radians);
        float sin = MathUtils.sin(radians);

        double rotatedX = localX * cos - localY * sin;
        double rotatedY = localX * sin + localY * cos;

        // Apply zoom and move to screen center
        return vec.set(
            rotatedX / zoom + viewportWidth / 2.0,
            -rotatedY / zoom + viewportHeight / 2.0
        );
    }

    /**
     * Converts World-Coordinates to Screen-Coordinates
     * @param worldPos World Position
     * @return A new Vector with the transformed Coordinate
     */
    public Vector2D project(Vector2D worldPos) {
        return project(worldPos.getX(), worldPos.getY(), new Vector2D(), renderZoom);
    }

    /**
     * Converts World-Coordinates to Screen-Coordinates
     * @param worldPos World Position
     * @return The transformed position stored in the previous worldPos vector
     */
    public Vector2D projectReuse(Vector2D worldPos) {
        return project(worldPos.getX(), worldPos.getY(), worldPos, renderZoom);
    }

    /**
     * Converts Screen-Coordinates to World-Coordinates
     *
     * @param screenX Screen X Coordinate
     * @param screenY Screen Y Coordinate
     * @param vec Vector to store and return result
     * @return A Vector of World-Coordinates
     */
    public Vector2D unproject(double screenX, double screenY, Vector2D vec, double zoom) {
        // Convert from screen space to local, zoomed space
        double dx = (screenX - viewportWidth / 2.0) * zoom;
        double dy = -(screenY - viewportHeight / 2.0) * zoom;

        // Inverse rotation
        float radians = rotationDegrees * DEG_TO_RAD;
        double cos = MathUtils.cos(radians);
        double sin = MathUtils.sin(radians);

        double unrotatedX = dx * cos + dy * sin;
        double unrotatedY = -dx * sin + dy * cos;

        return vec.set(
            renderOrigin.getX() + unrotatedX,
            renderOrigin.getY() + unrotatedY
        );
    }

    /**
     * Converts Screen-Coordinates to World-Coordinates
     * @param screenPos Screen Position
     * @return A new Vector with the transformed Coordinate
     */
    public Vector2D unproject(Vector2D screenPos) {
        return unproject(screenPos.getX(), screenPos.getY(), new Vector2D(), renderZoom);
    }

    /**
     * Converts Screen-Coordinates to World-Coordinates
     * @param screenPos Screen Position
     * @return The transformed position stored in the previous screenPos vector
     */
    public Vector2D unprojectReuse(Vector2D screenPos) {
        return unproject(screenPos.getX(), screenPos.getY(), screenPos, renderZoom);
    }

    /**
     * Applies a zoom value towards screenPos and smooths changes
     *
     * @param newZoom New Zoom Value
     * @param screenPos Position to zoom toward
     */
    public void zoomToward(double newZoom, Vector2D screenPos) {
        targetZoom = newZoom;
        zoomTargetPos.set(screenPos);
    }

    public double getZoom() {
        return zoom;
    }

    public double getRenderZoom() {
        return renderZoom;
    }

    public double getTargetZoom() {
        return targetZoom;
    }

    /** Rotation in Degree */
    public double getRotation() {
        return rotationDegrees;
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

    public void move(Vector2D direction) {
        double cos = Math.cos(Math.toRadians(0));
        double sin = Math.sin(Math.toRadians(0));

        origin.add(
            direction.getX() * cos - direction.getY() * sin,
            direction.getX() * sin + direction.getY() * cos
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


    public void updateProjectionMatrix(float screenWidth, float screenHeight) {
        double halfWidth = (screenWidth / 2.0) * renderZoom;
        double halfHeight = (screenHeight / 2.0) * renderZoom;

        float left = (float) -halfWidth;
        float right = (float) halfWidth;
        float bottom = (float) -halfHeight;
        float top = (float) halfHeight;

        projectionMatrix.setToOrtho2D(left, bottom, right - left, top - bottom);

        // rotate about Z
        projectionMatrix.rotate(0, 0, 1, rotationDegrees);
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

            prevOrigin.set(origin);

            prevZoom = zoom;
            Vector2D zoomPos = zoomTargetPos.copy();
            Vector2D before = unproject(zoomPos.getX(), zoomPos.getY(), new Vector2D(), zoom);

            zoom = MathUtil.lerp(zoom, targetZoom, 0.3f);

            Vector2D after = unproject(zoomPos.getX(), zoomPos.getY(), new Vector2D(), zoom);

            float radians = (float) Math.toRadians(MathUtil.normalizeRotationDeg(-rotationDegrees));
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);

            double rotatedDX = moveDirection.getX() * cos - moveDirection.getY() * sin;
            double rotatedDY = moveDirection.getX() * sin + moveDirection.getY() * cos;

            origin.add(rotatedDX * speed * zoom, rotatedDY * speed * zoom);
            origin.add(before.subtract(after));
            accumulator -= CAMERA_MOVEMENT_TICK_RATE;
        }
        moveRequested = false;
    }
}

