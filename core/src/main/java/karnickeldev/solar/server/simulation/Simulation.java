package karnickeldev.solar.server.simulation;

import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.server.physics.KeplerianOrbitSystem;

class Simulation implements Runnable {

    private static final double SPEED = 3600 * 24 * 30;
    private static final double timeFactor = SPEED / (24 * 60 * 60);

    private final double timeSeconds;
    private final EntityManager em;

    private final KeplerianOrbitSystem os;

    private double time = 0d;


    protected Simulation(EntityManager entityManager, int tickRate) {
        this.timeSeconds = 1d / tickRate;

        this.em = entityManager;
        os = new KeplerianOrbitSystem(em);
    }

    @Override
    public void run() {
        time += timeSeconds * timeFactor;
        os.updateHCS(time);
        PlanetoidRenderSystem.lastFixedUpdateTime = System.currentTimeMillis();
    }
}
