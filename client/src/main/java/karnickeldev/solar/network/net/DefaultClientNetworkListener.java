package karnickeldev.solar.network.net;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameStateID;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.packets.FullSnapshotRequestPacket;
import karnickeldev.solar.ui.core.UIManager;
import karnickeldev.solar.ui.layers.dialog.MessageLayer;
import karnickeldev.solar.ui.screens.MainMenuScreen;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

import java.util.HashMap;
import java.util.Map;

public class DefaultClientNetworkListener implements ClientNetworkListener {

    private final Logger logger;

    private final WorldManager<ClientWorld> worldManager;

    public DefaultClientNetworkListener(WorldManager<ClientWorld> worldManager) {
        this.logger = Logger.get(LogTag.GENERAL);
        this.worldManager = worldManager;
    }

    @Override
    public void onDisconnected() {
        logger.info("Client disconnected from server");

        if(GameStateManager.get().getState().getID() == GameStateID.GAMEPLAY) {
            //GameStateManager.get().requestStateLoading(new MainMenuScreen(SolarMain.getInstance(), () -> UIManager.get().showMessage("Connection failed")));
            GameStateManager.get().requestStateLoading(new MainMenuScreen(SolarMain.getInstance(), () -> UIManager.get().push(new MessageLayer("Connection failed"))));
        }
    }

    @Override
    public void onConnected() {
        logger.info("Client connected to server");
        GameContext.get().getClientNetwork().send(new FullSnapshotRequestPacket(1));
    }

    public static final Map<Integer, Double[]> clientCamPos = new HashMap<>();

}
