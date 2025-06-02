package karnickeldev.solar.net.network;

import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.ecs.components.HCSPositionComponent;
import karnickeldev.solar.ecs.components.HCSPositionSnapshot;
import karnickeldev.solar.ecs.components.TagComponent;
import karnickeldev.solar.net.packets.*;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

import java.util.Arrays;

public class DefaultClientNetworkListener implements ClientNetworkListener {

    private final WorldManager<ClientWorld> worldManager;

    public DefaultClientNetworkListener(WorldManager<ClientWorld> worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public void onDisconnected() {
        Logger.log("disconnected from server");
    }

    @Override
    public void onConnected() {
        Logger.log("connected to server");
    }

    @Override
    public void onPacketReceived(Packet packet) {

        if(packet.getType() == PacketTypes.WORLD_UPDATE.getType()) {
            WorldUpdatePacket p = (WorldUpdatePacket) packet;

            if(worldManager.addWorld(new ClientWorld(p.worldId)) != null) Logger.error("double world creation");
        }

        if (packet.getType() == PacketTypes.ENTITY_LIFECYCLE.getType()) {
            assert packet instanceof EntityLifecyclePacket;
            EntityLifecyclePacket p = (EntityLifecyclePacket) packet;
            int worldId = p.worldId;

            if(worldManager.getWorld(worldId) == null) {
                worldManager.addWorld(new ClientWorld(worldId));
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
            ComponentSnapshot[] snapshots = ((ECSUpdatePacket) packet).getSnapshots();
            int worldId = ((ECSUpdatePacket) packet).worldId;
            if(worldId != worldManager.getActiveWorld().getID()) {
                worldManager.changeWorld(worldId);
            }

            worldManager.getWorld(worldId).getECS().getComponentRegistry().applyAllSnapshots(snapshots);
        }

        if (packet.getType() == PacketTypes.SERVER_PERFORMANCE_METRICS.getType()) {
            assert packet instanceof ServerPerformanceMetricsPacket;
            ServerPerformanceMetricsPacket p = (ServerPerformanceMetricsPacket) packet;

            SolarMain.tps = p.tps;
            SolarMain.delay = p.delay;
        }

    }

}
