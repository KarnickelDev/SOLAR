package karnickeldev.solar.worldview.orbitsolver;

/**
 * @author KarnickelDev
 * @since 12.03.2026
 **/
public interface OrbitSolver {

    void beginNextFrame(OrbitSolveInput input);

    void finishFrame();

    OrbitLocalFrame getCurrentFrame();

    void shutdown();
}
