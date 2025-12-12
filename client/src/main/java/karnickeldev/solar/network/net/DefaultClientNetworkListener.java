package karnickeldev.solar.network.net;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameState;
import karnickeldev.solar.core.gamestates.GameStateID;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.packets.FullSnapshotRequestPacket;
import karnickeldev.solar.ui.core.UIManager;
import karnickeldev.solar.ui.screens.LoadingScreen;
import karnickeldev.solar.ui.screens.MainMenuScreen;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ClientWorld;
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

        if(GameStateManager.get().getState().getID() == GameStateID.GAMEPLAY) {
            GameStateManager.get().requestStateLoading(new MainMenuScreen(SolarMain.getInstance(), () -> UIManager.get().showMessage("Connection failed")));
        }
    }

    @Override
    public void onConnected() {
        Logger.log("connected to server");
        GameContext.get().getClientNetwork().send(new FullSnapshotRequestPacket(1));
    }

    public static final Map<Integer, Double[]> clientCamPos = new HashMap<>();

}
