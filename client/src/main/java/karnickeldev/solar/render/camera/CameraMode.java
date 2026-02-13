package karnickeldev.solar.render.camera;

/**
 * @author KarnickelDev
 * @since 18.02.2026
 **/
public interface CameraMode {

    void onEnter(FloatingOriginCamera.CameraContext ctx);

    void onUpdate(FloatingOriginCamera.CameraContext ctx, float dt);

    void onExit(FloatingOriginCamera.CameraContext ctx);

}
