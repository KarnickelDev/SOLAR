package karnickeldev.solar.net.server;

import karnickeldev.solar.ecs.EntityFactory;
import karnickeldev.solar.ecs.ServerECS;
import karnickeldev.solar.ecs.systems.HCSClientSystem;
import karnickeldev.solar.net.network.MainThreadDispatcher;
import karnickeldev.solar.net.network.NetworkThread;
import karnickeldev.solar.net.network.ServerNetwork;
import karnickeldev.solar.physics.KeplerianOrbitSystem;
import karnickeldev.solar.physics.Scale;
import karnickeldev.solar.physics.Units;
import karnickeldev.solar.simulation.SimulationExecutor;
import karnickeldev.solar.util.MathUtil;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.WorldManager;

public class LocalServer extends Server implements GameServer {

    public static LocalServer create(ServerNetwork serverNetwork, NetworkThread networkThread, MainThreadDispatcher dispatcher) {
        WorldManager<ServerWorld> worldManager = new WorldManager<>(new ServerWorld(serverNetwork));
        KeplerianOrbitSystem ks = new KeplerianOrbitSystem(worldManager, serverNetwork);
        ServerPerformanceMetrics performanceMetrics = new ServerPerformanceMetrics(2f);
        SimulationExecutor simulationExecutor = new SimulationExecutor(serverNetwork, performanceMetrics, ks, dispatcher, TICK_RATE);

        return new LocalServer(serverNetwork, networkThread, worldManager, simulationExecutor, dispatcher);
    }

    private LocalServer(ServerNetwork serverNetwork, NetworkThread networkThread, WorldManager<ServerWorld> worldManager, SimulationExecutor simulationExecutor, MainThreadDispatcher dispatcher) {
        super(serverNetwork, networkThread, worldManager, simulationExecutor, dispatcher);

        createWorld();
        worldManager.changeWorld(1);
    }

    @Override
    protected void preTick() {}

    @Override
    protected void postTick() {}


    private void createWorld() {
        worldManager.addWorld(new ServerWorld(serverNetwork));
        int sun = EntityFactory.createStar(worldManager.getWorld(1).getECS(), "sun", 0, 0,
            Units.toSU(1, Units.Mass.SOLAR_MASS), 600000);

        int mercury = EntityFactory.createStaticPlanetoidHCS(worldManager.getWorld(1).getECS(), "mercury",
            Units.toSU(0.055f, Units.Mass.EARTH_MASS),
            (int) Units.toSU(2439.7f, Units.Length.KILOMETER), 1,
            0.387f, 0.205f, 0, 0,
            sun);

        int venus = EntityFactory.createStaticPlanetoidHCS(worldManager.getWorld(1).getECS(), "venus",
            Units.toSU(0.815f, Units.Mass.EARTH_MASS),
            (int) Units.toSU(6000, Units.Length.KILOMETER), 1,
            0.723f, 0.007f, 0, 0,
            sun);

        int earth = EntityFactory.createStaticPlanetoidHCS(worldManager.getWorld(1).getECS(), "earth",
            Units.toSU(1f, Units.Mass.EARTH_MASS),
            (int) Units.toSU(6400f, Units.Length.KILOMETER), 1,
            1f, 0.017f, 0, 0,
            sun);

        int mars = EntityFactory.createStaticPlanetoidHCS(worldManager.getWorld(1).getECS(), "mars",
            Units.toSU(0.107f, Units.Mass.EARTH_MASS),
            (int) Units.toSU(3396f, Units.Length.KILOMETER), 1,
            1.523f, 0.093f, 0, 0,
            sun);

        int jupiter = EntityFactory.createStaticPlanetoidHCS(worldManager.getWorld(1).getECS(), "jupiter",
            Units.toSU(317.8f, Units.Mass.EARTH_MASS),
            (int) Units.toSU(70000f, Units.Length.KILOMETER), 1,
            5.2038f, 0.049f, 0, 0, sun);

        int moon = EntityFactory.createStaticPlanetoidHCS(worldManager.getWorld(1).getECS(), "moon",
            Units.toSU(0.0123f, Units.Mass.EARTH_MASS),
            (int) Units.toSU(1737f, Units.Length.KILOMETER), 1,
            (float) Units.convert(384399, Units.Length.KILOMETER, Units.Length.AU),
            0.055f, 0, 0, earth);

        int s1 = EntityFactory.createStaticPlanetoidHCS(worldManager.getWorld(1).getECS(), "s1",
            Units.toSU(100, Units.Mass.TON),
            1, 1,
            (float) Units.convert(7000, Units.Length.KILOMETER, Units.Length.AU),
            0.01f, 0, 0, earth);

        int s2 = EntityFactory.createStaticPlanetoidHCS(worldManager.getWorld(1).getECS(), "s2",
            Units.toSU(100, Units.Mass.TON),
            1, 1,
            (float) Units.convert(7000, Units.Length.KILOMETER, Units.Length.AU),
            0.01f, 0, 2, earth);

        ServerWorld world2 = new ServerWorld(serverNetwork);
        worldManager.addWorld(world2);

        int sun2 = EntityFactory.createStar(worldManager.getWorld(2).getECS(), "sun2", 0, 0,
            Units.toSU(1, Units.Mass.SOLAR_MASS), 600000);
        for (int i = 0; i < (15 * 1024); i++) {
            EntityFactory.createStaticPlanetoidHCS(worldManager.getWorld(2).getECS(), "",
                Units.toSU(MathUtil.random(1, 1e10f), Units.Mass.TON),
                1, 1,
                (float) Units.convert(MathUtil.random(1e-2f, 100f), Units.Length.AU, Units.Length.AU),
                MathUtil.random(0, 0.9f), MathUtil.random(0, (float) (2 * Math.PI)), 0, sun2);
        }
    }

}
