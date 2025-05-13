package karnickeldev.solar.simulation;

import karnickeldev.solar.physics.KeplerianOrbitSystem;

class Simulation implements Runnable {

    private static final double SPEED = 60 * 60;

    private final double timeSeconds;

    private final KeplerianOrbitSystem os;

    private double time = 0d;

    protected Simulation(KeplerianOrbitSystem os, int tickRate) {
        this.timeSeconds = 1d / tickRate;


        this.os = os;
    }

    @Override
    public void run() {
        time += timeSeconds * SPEED;
        os.updateHCS(time);
    }
}
