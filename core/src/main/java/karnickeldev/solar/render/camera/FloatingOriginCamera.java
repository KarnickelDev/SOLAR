package karnickeldev.solar.render.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import karnickeldev.solar.ecs.EntityReference;
import karnickeldev.solar.physics.Vector2D;
import karnickeldev.solar.util.MathUtil;

public class FloatingOriginCamera {

    public static final byte NO_MOVE =  0b0000;
    public static final byte UP =       0b0001;
    public static final byte DOWN =     0b0010;
    public static final byte LEFT =     0b0100;
    public static final byte RIGHT =    0b1000;

    private static final float CAMERA_MOVEMENT_TICK_RATE = (1f / 128);

    private static final float MIN_SPEED = 1f;
    private static final float MAX_SPEED = 10f;
    private static final float ACCELERATION = 5.2f;
    private static final float DECELERATION = 18f;

    private float viewportWidth;
    private float viewportHeight;

    private double accumulator = 0;
    private float speed = MIN_SPEED;
    private byte direction = NO_MOVE;
    private boolean moveRequested = false;

    private final Vector2D directionVec = new Vector2D();
    private final Vector2D moveDirection = new Vector2D();
    private final Vector2D origin;
    private final Vector2D prevPosition;
    private final Vector2D renderOrigin;

    public EntityReference trackedEntity;

    private double zoom;

    private float rotationDegrees;
    private float targetRotationDegrees;
    private final Vector2D rotationCenter = new Vector2D();

    private final Matrix4 projectionMatrix = new Matrix4();

    public FloatingOriginCamera(float viewportWidth, float viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;

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
//        Vector2D position = new Vector2D(origin);
//
//        if(trackedEntity != null) {
//            position.add(
//                trackedEntity.getEntityManager().hcs.getLocalX(trackedEntity.getEntityId()),
//                trackedEntity.getEntityManager().hcs.getLocalY(trackedEntity.getEntityId())
//            );
//            rotationCenter.set(
//                trackedEntity.getEntityManager().hcs.getLocalX(trackedEntity.getEntityId()),
//                trackedEntity.getEntityManager().hcs.getLocalY(trackedEntity.getEntityId())
//            );
//            prevPosition.add(
//                trackedEntity.getEntityManager().hcs.getLocalX(trackedEntity.getEntityId()),
//                trackedEntity.getEntityManager().hcs.getLocalY(trackedEntity.getEntityId())
//            );
//        }

        //renderOrigin.set(prevPosition).lerp(origin, (float) (accumulator / CAMERA_MOVEMENT_TICK_RATE));
        renderOrigin.set(origin);
        updateProjectionMatrix(viewportWidth, viewportHeight);
    }

    public void track(EntityReference trackedEntity) {
        if(trackedEntity == null) return;
        this.trackedEntity = trackedEntity;
    }

    /**
     * Get Camera Position in World-Space
     * @return Camera Origin
     */
    public Vector2D getOrigin() {
        return origin;
    }

    /**
     * Get Camera Render-Position in World-Space
     * @return Camera Render-Origin
     */
    public Vector2D getRenderOrigin() {
        return renderOrigin;
    }

    /**
     * Converts World-Coordinates to Screen-Coordinates
     * @param worldPos World Coordinates
     * @return A Vector of Screen-Coordinates
     */
    public Vector2D project(Vector2D worldPos) {
        // Translate relative to pivot
        double translatedX = worldPos.getX() - rotationCenter.getX();
        double translatedY = worldPos.getY() - rotationCenter.getY();

        // Apply rotation around pivot
        float radians = (float) Math.toRadians(rotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double rotatedX = translatedX * cos - translatedY * sin;
        double rotatedY = translatedX * sin + translatedY * cos;

        // Translate relative to renderOrigin
        rotatedX -= (renderOrigin.getX() - rotationCenter.getX());
        rotatedY -= (renderOrigin.getY() - rotationCenter.getY());

        // Scale by zoom and move to screen center
        return new Vector2D(
            (rotatedX / zoom) + viewportWidth / 2.0,
            (-rotatedY / zoom) + viewportHeight / 2.0 // flip Y axis
        );
    }

    /**
     * Converts Screen-Coordinates to World-Coordinates
     * @param screenPos Screen Coordinates
     * @return A Vector of World-Coordinates
     */
    public Vector2D unproject(Vector2D screenPos) {
        double halfW = viewportWidth / 2.0;
        double halfH = viewportHeight / 2.0;

        // Convert from screen to relative zoom space
        double x = (screenPos.getX() - halfW) * zoom;
        double y = -(screenPos.getY() - halfH) * zoom; // flip Y

        // Apply inverse rotation around rotationCenter
        float radians = (float) Math.toRadians(-rotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        // Translate to pivot (rotationCenter)
        double translatedX = x + (renderOrigin.getX() - rotationCenter.getX());
        double translatedY = y + (renderOrigin.getY() - rotationCenter.getY());

        double rotatedX = translatedX * cos - translatedY * sin;
        double rotatedY = translatedX * sin + translatedY * cos;

        // Final world position relative to pivot
        rotatedX += rotationCenter.getX();
        rotatedY += rotationCenter.getY();

        return new Vector2D(rotatedX, rotatedY);
    }


    /**
     * Set Camera Position
     * @param x X Coordinate
     * @param y Y Coordinate
     */
    public void setPosition(double x, double y) {
        origin.set(x, y);
    }

    /**
     * Set Camera Position
     * @param position Position
     */
    public void setPosition(Vector2D position) {
        origin.set(position);
    }

    /**
     * Sets the Cameras Rotation
     * @param angle Angle
     */
    public void setRotation(float angle) {
        targetRotationDegrees = MathUtil.normalizeRotationDeg(angle);
    }

    /**
     * Changes rotation by adding an angle
     * @param angleDelta Angle to be added to rotation
     */
    public void rotate(float angleDelta) {
        targetRotationDegrees = MathUtil.normalizeRotationDeg(
            targetRotationDegrees + (angleDelta * Gdx.graphics.getDeltaTime())
        );
    }

    /**
     * Signals camera to move, only gets executed on camera.update();
     * @param direction Direction to move, use bitwise or to combine directions
     */
    public void move(byte direction) {
        if(!moveRequested) {
            this.direction = direction;
            moveRequested = true;
        } else this.direction |= direction;
    }

    public void setZoom(double newZoom) {
        this.zoom = newZoom;
    }

    public double getZoom() {
        return zoom;
    }

    /**
     * Applies a zoom value towards screenPos and smooths changes
     * @param newZoom New Zoom Value
     * @param screenPos Position to zoom toward
     */
    void zoomToward(float newZoom, Vector2D screenPos) {
        Vector2D worldBefore = unproject(screenPos);

        float dt = Math.max(Gdx.graphics.getDeltaTime(), CAMERA_MOVEMENT_TICK_RATE);
        zoom = MathUtil.lerp(zoom, newZoom, 10 * dt);

        // TODO: maybe cleaner solution possible
        //renderOrigin.set(prevPosition).lerp(origin, (float) (accumulator / CAMERA_MOVEMENT_TICK_RATE));

        Vector2D worldAfter = unproject(screenPos);

        Vector2D delta = worldBefore.subtract(worldAfter);

        float radians = (float) Math.toRadians(rotationDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double rotatedDeltaX = delta.getX() * cos - delta.getY() * sin;
        double rotatedDeltaY = delta.getX() * sin + delta.getY() * cos;

        origin.add(rotatedDeltaX, rotatedDeltaY);
    }


    public void updateProjectionMatrix(float screenWidth, float screenHeight) {
        double halfWidth = (screenWidth / 2.0) * zoom;
        double halfHeight = (screenHeight / 2.0) * zoom;


        float left = (float) (origin.getX() - halfWidth);
        float right = (float) (origin.getX() + halfWidth);
        float bottom = (float) (origin.getY() - halfHeight);
        float top = (float) (origin.getY() + halfHeight);

        projectionMatrix.setToOrtho2D(left, bottom, right - left, top - bottom);
//
//        // translate so the pivot is at the origin
//        projectionMatrix.translate((float) rotationCenter.getX(), (float) rotationCenter.getY(), 0);
//
//        // rotate about Z
//        projectionMatrix.rotate(0, 0, 1, rotationDegrees);
//
//        // translate back
//        projectionMatrix.translate((float) -rotationCenter.getX(), (float) -rotationCenter.getY(), 0);
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

        while(accumulator >= CAMERA_MOVEMENT_TICK_RATE) {
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
            origin.add(moveDirection.getX() * (speed * zoom), moveDirection.getY() * (speed * zoom));
            accumulator -= CAMERA_MOVEMENT_TICK_RATE;
        }
        moveRequested = false;
    }

}

