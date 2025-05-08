package karnickeldev.solar.server.servers;

import karnickeldev.solar.ecs.EntityFactory;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.EntityReference;
import karnickeldev.solar.ecs.components.OrbitData;
import karnickeldev.solar.level.StarSystem;
import karnickeldev.solar.physics.PhysicsUtil;
import karnickeldev.solar.physics.Scale;
import karnickeldev.solar.physics.Units;
import karnickeldev.solar.server.TPSCount;
import karnickeldev.solar.server.packets.Packet;
import karnickeldev.solar.server.simulation.SimulationExecutor;
import karnickeldev.solar.util.MathUtil;

public class DefaultServer {

    public static final int TICK_RATE = 60;

    private final SimulationExecutor simulationExecutor;

    private final EntityManager em;

    public static final TPSCount tpsCount = new TPSCount(2f);

    public DefaultServer() {
        this.em = new EntityManager();
        simulationExecutor = new SimulationExecutor(em, TICK_RATE);
    }


    public void start() {

        int sun = EntityFactory.createStar(em, "sun", 0, 0,
            Units.toSU(1, Units.Mass.SOLAR_MASS), 600000);

        int mercury = EntityFactory.createStaticPlanetoidHCS(em,"mercury",
            Units.toSU(0.055f, Units.Mass.EARTH_MASS),
            (int)Units.toSU(2439.7f, Units.Length.KILOMETER), 1,
            0.387f, 0.205f, 0, 0,
                sun, Scale.SCALE_AU);

        int venus = EntityFactory.createStaticPlanetoidHCS(em,"venus",
            Units.toSU(0.815f, Units.Mass.EARTH_MASS),
            (int)Units.toSU(6000, Units.Length.KILOMETER), 1,
            0.723f, 0.007f, 0, 0,
                sun, Scale.SCALE_AU);

        int earth = EntityFactory.createStaticPlanetoidHCS(em,"earth",
            Units.toSU(1f, Units.Mass.EARTH_MASS),
            (int)Units.toSU(6400f, Units.Length.KILOMETER), 1,
            1f, 0.017f, 0, 0,
                sun, Scale.SCALE_AU);

        int mars = EntityFactory.createStaticPlanetoidHCS(em,"mars",
            Units.toSU(0.107f, Units.Mass.EARTH_MASS),
            (int)Units.toSU(3396f, Units.Length.KILOMETER), 1,
            1.523f, 0.093f, 0, 0,
                sun, Scale.SCALE_AU);

        int jupiter = EntityFactory.createStaticPlanetoidHCS(em,"jupiter",
            Units.toSU(317.8f, Units.Mass.EARTH_MASS),
            (int)Units.toSU(70000f, Units.Length.KILOMETER), 1,
            5.2038f, 0.049f, 0, 0, sun, Scale.SCALE_AU);

        int moon = EntityFactory.createStaticPlanetoidHCS(em,"moon",
            Units.toSU(0.0123f, Units.Mass.EARTH_MASS),
            (int)Units.toSU(1737f, Units.Length.KILOMETER), 1,
            (float)Units.convert(384399, Units.Length.KILOMETER, Units.Length.AU),
            0.055f, 0, 0, earth, Scale.SCALE_AU);

        int s1 = EntityFactory.createStaticPlanetoidHCS(em,"s1",
            Units.toSU(100, Units.Mass.TON),
            1, 1,
            (float)Units.convert(7000, Units.Length.KILOMETER, Units.Length.AU),
            0.01f, 0, 0, earth, Scale.SCALE_AU);

        int s2 = EntityFactory.createStaticPlanetoidHCS(em,"s2",
            Units.toSU(100, Units.Mass.TON),
            1, 1,
            (float)Units.convert(7000, Units.Length.KILOMETER, Units.Length.AU),
            0.01f, 0, 2, earth, Scale.SCALE_AU);

        for(int i = 0; i < 0_000; i++) {
            EntityFactory.createStaticPlanetoidHCS(em, ""+i,
                MathUtil.random(1e-12f, 5e-6f),
                (int) Units.toSU(MathUtil.random(0.01f, 1e5f), Units.Length.KILOMETER),
                PhysicsUtil.estimateSOIPlanet(Units.toSU(1, Units.Mass.TON)),
                MathUtil.random(0.1f, 100f),
                MathUtil.random(0, 0.8f),
                MathUtil.random(0, 6.3f),
                0,
                sun, Scale.SCALE_AU
            );
        }

        simulationExecutor.start();
    }

    public void stop() {
        simulationExecutor.stop();

    }

    public EntityManager getEntityManager() {
        return em;
    }

}
