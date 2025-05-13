package karnickeldev.solar.net.network;

import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.net.packets.*;
import karnickeldev.solar.util.Logger;

public class DefaultClientNetworkListener implements ClientNetworkListener {

    private final ClientECS ecs;

    public DefaultClientNetworkListener(ClientECS ecs) {
        this.ecs = ecs;
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

        if (packet.getType() == PacketTypes.ENTITY_LIFECYCLE.getType()) {
            EntityLifecyclePacket p = (EntityLifecyclePacket) packet;

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
            ecs.getComponentRegistry().applyAllSnapshots(snapshots);
        }

        if (packet.getType() == PacketTypes.SERVER_PERFORMANCE_METRICS.getType()) {
            assert packet instanceof ServerPerformanceMetricsPacket;
            ServerPerformanceMetricsPacket p = (ServerPerformanceMetricsPacket) packet;

            SolarMain.tps = p.tps;
            SolarMain.delay = p.delay;
        }

    }

}
