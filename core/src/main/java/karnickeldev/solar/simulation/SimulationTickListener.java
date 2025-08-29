package karnickeldev.solar.simulation;

/**
 * @author : KarnickelDev
 * @since : 30.05.2025
 **/
public interface SimulationTickListener {

    void onTickStart(long tick);
    void onTickEnd(long trick, int duration);

}
