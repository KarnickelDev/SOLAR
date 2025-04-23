package karnickeldev.solar.render.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import karnickeldev.solar.physics.Vector2D;

public class FloatingOriginCamera {

    public static final byte NO_MOVE =  0b0000;
    public static final byte UP =       0b0001;
    public static final byte DOWN =     0b0010;
    public static final byte LEFT =     0b0100;
    public static final byte RIGHT =    0b1000;

    private static final float MIN_SPEED = 0.9f / 6;
    private static final float MAX_SPEED = 7.3f / 6;
    private static final float ACCELERATION = 5.3f / 6;
    private static final float DECELERATION = 18f / 6;

    public OrthographicCamera camera;
    private final Vector2D origin;    // The current simulation-space origin
    private float scale;

    private float minSpeed;
    private float maxSpeed;
    private float acceleration;
    private float deceleration;

    private float speed = MIN_SPEED;
    private final Vector2D directionVec = new Vector2D();
    private final Vector2D moveDirection = new Vector2D();

    private byte direction = NO_MOVE;
    private boolean hasMoved = false;

    public FloatingOriginCamera(float viewportWidth, float viewportHeight, float scale) {
        this.camera = new OrthographicCamera(viewportWidth, viewportHeight);
        this.origin = new Vector2D(0, 0);

        setScale(scale);
        setZoom(scale / 100);
    }

    public void update() {
        // execute camera movement
        moveHelper();

        // The camera always stays at (0,0) in render space
        camera.position.set(0, 0, 0);
        camera.update();
    }

    public void setScale(float scale) {
        this.scale = scale;

        minSpeed = MIN_SPEED / scale;
        maxSpeed = MAX_SPEED / scale;
        acceleration = ACCELERATION / scale;
        deceleration = DECELERATION / scale;
    }

    public float getScale() {
        return scale;
    }

    public Matrix4 getCombinedMatrix() {
        return camera.combined;
    }

    public Vector2D worldToRender(Vector2D worldPos) {
        return new Vector2D(worldPos).subtract(origin).scale(scale);
    }

    public void setPosition(double x, double y) {
        origin.set(x, y);
    }

    /**
     * Signals camera to move, only gets executed on camera.update();
     * @param direction Direction to move, use bitwise or to combine directions
     */
    public void move(byte direction) {
        if(!hasMoved) {
            this.direction = direction;
            hasMoved = true;
        } else {
            this.direction |= direction;
        }
    }

    private void moveHelper() {
        if (!hasMoved) {
            direction = 0b0000;
        }

        short x = 0;
        short y = 0;
        if ((direction & UP) == UP) y += 1;
        if ((direction & DOWN) == DOWN) y -= 1;
        if ((direction & RIGHT) == RIGHT) x += 1;
        if ((direction & LEFT) == LEFT) x -= 1;

        directionVec.set(x, y);
        if (directionVec.len2() > 0) {
            directionVec.nor(); // New target direction
        }

        float dt = Gdx.graphics.getDeltaTime();

        // Smooth the direction vector to avoid snappy turns
        float directionSmoothness = 11f;
        moveDirection.lerp(directionVec, directionSmoothness * dt);

        if (directionVec.isZero()) {
            // Decelerate speed when no input
            speed = Math.max(minSpeed, speed - (deceleration * dt));
        } else {
            // Accelerate up to max
            speed = Math.min(maxSpeed, speed + (acceleration * dt));
        }

        origin.add(moveDirection.getX() * speed * getZoom(), moveDirection.getY() * speed * getZoom());

        hasMoved = false;
    }




    public void zoom(float delta) {
        camera.zoom += delta * Gdx.graphics.getDeltaTime();
    }

    public void setZoom(float zoom) {
        camera.zoom = zoom;
    }

    public float getZoom() {
        return camera.zoom;
    }

    public Vector2D getOrigin() {
        return origin;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}

