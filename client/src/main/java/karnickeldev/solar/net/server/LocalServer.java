package karnickeldev.solar.net.server;

import karnickeldev.solar.ecs.EntityFactory;
import karnickeldev.solar.ecs.ServerECS;
import karnickeldev.solar.ecs.components.server.HCSPositionSnapshot;
import karnickeldev.solar.net.network.MainThreadDispatcher;
import karnickeldev.solar.net.network.NetworkThread;
import karnickeldev.solar.net.network.ServerNetwork;
import karnickeldev.solar.net.packets.ComponentSnapshotRegistry;
import karnickeldev.solar.physics.KeplerianOrbitSystem;
import karnickeldev.solar.physics.Scale;
import karnickeldev.solar.physics.Units;
import karnickeldev.solar.simulation.SimulationExecutor;
import karnickeldev.solar.util.MathUtil;

public class LocalServer implements GameServer {

    public static final int TICK_RATE = 60;
    public static ServerECS ecs;
    public final NetworkThread networkThread;
    private final SimulationExecutor simulationExecutor;
    private final ServerNetwork serverNetwork;

    public LocalServer(ServerNetwork serverNetwork, NetworkThread networkThread, MainThreadDispatcher dispatcher) {

        ComponentSnapshotRegistry.register(1, HCSPositionSnapshot.class, new HCSPositionSnapshot(0, 0));

        this.serverNetwork = serverNetwork;
        this.networkThread = networkThread;

        ecs = new ServerECS();
        KeplerianOrbitSystem ks = new KeplerianOrbitSystem(ecs, serverNetwork);
        ServerPerformanceMetrics performanceMetrics = new ServerPerformanceMetrics(2f);
        simulationExecutor = new SimulationExecutor(serverNetwork, performanceMetrics, ks, dispatcher, TICK_RATE);
    }

    @Override
    public void start() {

        createWorld();

        serverNetwork.start();
        networkThread.start();

        simulationExecutor.start();
    }

    @Override
    public void stop() {
        simulationExecutor.stop();

        serverNetwork.shutdown();

        networkThread.stop();

    }

    @Override
    public boolean isRunning() {
        return simulationExecutor.isRunning();
    }


    private void createWorld() {
        int sun = EntityFactory.createStar(ecs, "sun", 0, 0,
            Units.toSU(1, Units.Mass.SOLAR_MASS), 600000);

        int mercury = EntityFactory.createStaticPlanetoidHCS(ecs, "mercury",
            Units.toSU(0.055f, Units.Mass.EARTH_MASS),
            (int) Units.toSU(2439.7f, Units.Length.KILOMETER), 1,
            0.387f, 0.205f, 0, 0,
            sun, Scale.SCALE_AU);

        int venus = EntityFactory.createStaticPlanetoidHCS(ecs, "venus",
            Units.toSU(0.815f, Units.Mass.EARTH_MASS),
            (int) Units.toSU(6000, Units.Length.KILOMETER), 1,
            0.723f, 0.007f, 0, 0,
            sun, Scale.SCALE_AU);

        int earth = EntityFactory.createStaticPlanetoidHCS(ecs, "earth",
            Units.toSU(1f, Units.Mass.EARTH_MASS),
            (int) Units.toSU(6400f, Units.Length.KILOMETER), 1,
            1f, 0.017f, 0, 0,
            sun, Scale.SCALE_AU);

        int mars = EntityFactory.createStaticPlanetoidHCS(ecs, "mars",
            Units.toSU(0.107f, Units.Mass.EARTH_MASS),
            (int) Units.toSU(3396f, Units.Length.KILOMETER), 1,
            1.523f, 0.093f, 0, 0,
            sun, Scale.SCALE_AU);

        int jupiter = EntityFactory.createStaticPlanetoidHCS(ecs, "jupiter",
            Units.toSU(317.8f, Units.Mass.EARTH_MASS),
            (int) Units.toSU(70000f, Units.Length.KILOMETER), 1,
            5.2038f, 0.049f, 0, 0, sun, Scale.SCALE_AU);

        int moon = EntityFactory.createStaticPlanetoidHCS(ecs, "moon",
            Units.toSU(0.0123f, Units.Mass.EARTH_MASS),
            (int) Units.toSU(1737f, Units.Length.KILOMETER), 1,
            (float) Units.convert(384399, Units.Length.KILOMETER, Units.Length.AU),
            0.055f, 0, 0, earth, Scale.SCALE_AU);

        int s1 = EntityFactory.createStaticPlanetoidHCS(ecs, "s1",
            Units.toSU(100, Units.Mass.TON),
            1, 1,
            (float) Units.convert(7000, Units.Length.KILOMETER, Units.Length.AU),
            0.01f, 0, 0, earth, Scale.SCALE_AU);

        int s2 = EntityFactory.createStaticPlanetoidHCS(ecs, "s2",
            Units.toSU(100, Units.Mass.TON),
            1, 1,
            (float) Units.convert(7000, Units.Length.KILOMETER, Units.Length.AU),
            0.01f, 0, 2, earth, Scale.SCALE_AU);


        for (int i = 0; i < (15 * 1024); i++) {
            EntityFactory.createStaticPlanetoidHCS(ecs, "",
                Units.toSU(MathUtil.random(1, 1e10f), Units.Mass.TON),
                1, 1,
                (float) Units.convert(MathUtil.random(1e-2f, 100f), Units.Length.AU, Units.Length.AU),
                MathUtil.random(0, 0.9f), MathUtil.random(0, (float) (2 * Math.PI)), 0, sun, Scale.SCALE_AU);
        }
    }

}
