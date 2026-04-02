package karnickeldev.solar.worldview.orbitsolver;

/**
 * @author KarnickelDev
 * @since 02.04.2026
 **/
public interface OrbitPrecisionManager {

    void updateContext(OrbitSolveInput input);

    OrbitJob[] buildJobs(float deltaTime);

    void validate();

}
