package karnickeldev.solar.network.net;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.ecs.systems.HCSClientSystem;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.packets.*;
import karnickeldev.solar.network.util.PingTracker;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.WorldManager;

import java.util.HashMap;
import java.util.Map;

public class DefaultClientNetworkListener implements ClientNetworkListener {

    private final WorldManager<ClientWorld> worldManager;

    public DefaultClientNetworkListener(WorldManager<ClientWorld> worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public void onDisconnected() {
        Logger.log("disconnected from server");
        ServerWorld.IDS = 0;    //TODO: BAD, FIXME
        //GameStateManager.get().changeState(new MainMenuScreen(SolarMain.getInstance()));
    }

    @Override
    public void onConnected() {
        Logger.log("connected to server");
        GameContext.get().getClientNetwork().send(new FullSnapshotRequestPacket(1));
    }

    public static final Map<Integer, Double[]> clientCamPos = new HashMap<>();

    @Override
    public void onPacketReceived(Packet packet) {

        if(packet instanceof GameStatePacket) {
            GameStatePacket p = (GameStatePacket) packet;
            GameContext.get().getTime().setSimTime(p.getSimTimeMicros() + (1000L * PingTracker.getPing()));
        }

        if(packet.getType() == PacketTypes.PONG.getType()) {
            //HandlerRegistry.getHandler(packet).handle(packet);
            PingPongPacket pp = (PingPongPacket) packet;
            long rtt = System.nanoTime() - pp.getClientSendTime();
            PingTracker.add(rtt / 1_000);
        }

        if(packet.getType() == PacketTypes.TEST_CAM.getType()) {
            TestCamPacket p = (TestCamPacket) packet;
            double x = p.x;
            double y = p.y;
            int clientId = p.clientId;

            clientCamPos.put(clientId, new Double[]{x,y});
        }

        if(packet.getType() == PacketTypes.WORLD_UPDATE.getType()) {
            WorldUpdatePacket p = (WorldUpdatePacket) packet;
            if(worldManager.containsWorld(p.getWorldId())) {
                Logger.error("double world creation");
            } else {
                worldManager.addWorld(new ClientWorld(p.getWorldId()));
                Logger.log(Logger.GENERAL, "Added new World " + p.getWorldId());
            }
        }

        if (packet.getType() == PacketTypes.ENTITY_LIFECYCLE.getType()) {
            assert packet instanceof EntityLifecyclePacket;
            EntityLifecyclePacket p = (EntityLifecyclePacket) packet;
            int worldId = p.getWorldId();

            if(!worldManager.containsWorld(worldId)) {
                Logger.error("Error handling EntityLifecyclePacket: unknown WorldId " + worldId);
                return;
            }

            ClientECS ecs = worldManager.getWorld(worldId).getECS();

            for (int i = 0; i < p.destroyedEntityIds.length; i++) {
                ecs.getEntityManager().destroy(p.destroyedEntityIds[i]);
            }
            for (int i = 0; i < p.createdEntityIds.length; i++) {
                ecs.getEntityManager().registerEntity(p.createdEntityIds[i]);
            }
        }

        if (packet.getType() == PacketTypes.ECS_UPDATE.getType()) {
            assert packet instanceof ECSUpdatePacket;
            ECSUpdatePacket p = (ECSUpdatePacket) packet;
            ComponentSnapshot[] snapshots = p.getSnapshots();

            int worldId = p.getWorldId();
            if(worldManager.containsWorld(worldId)) {
                if(worldId != worldManager.getActiveWorld().getID()) {
                    worldManager.changeWorld(worldId);
                }
            } else {
                Logger.error("Received ECS Update for unknown World " + worldId);
                return;
            }
            HCSClientSystem.simSpeed = p.getSimSpeed();
            worldManager.getWorld(worldId).getECS().getComponentRegistry().applyAllSnapshots(snapshots);
        }

        if(packet.getType() == PacketTypes.FULL_SNAPSHOT.getType()) {
            Logger.log("Resyncing with server");
            assert packet instanceof FullSnapshotPacket;
            FullSnapshotPacket full = (FullSnapshotPacket) packet;
            for(int i = 0; i < full.packets.length; i++) {
                onPacketReceived(full.packets[i]);
            }
        }

        if (packet.getType() == PacketTypes.SERVER_PERFORMANCE_METRICS.getType()) {
            assert packet instanceof ServerPerformanceMetricsPacket;
            ServerPerformanceMetricsPacket p = (ServerPerformanceMetricsPacket) packet;

            SolarMain.tps = p.tps;
            SolarMain.delay = p.delay;
        }

    }

}
