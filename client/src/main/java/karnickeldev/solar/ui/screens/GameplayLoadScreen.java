package karnickeldev.solar.ui.screens;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.context.GameContextBuilder;
import karnickeldev.solar.context.GameContextContainer;
import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.core.SimTestScreen;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameState;
import karnickeldev.solar.core.gamestates.GameStateID;
import karnickeldev.solar.core.gamestates.GameStateManager;
import karnickeldev.solar.ecs.components.ComponentType;
import karnickeldev.solar.network.net.handlers.*;
import karnickeldev.solar.network.packets.PacketTypes;
import karnickeldev.solar.render.BackgroundStarRenderer;
import karnickeldev.solar.ui.core.UIManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author : KarnickelDev
 * @since : 04.07.2025
 **/
public class GameplayLoadScreen implements GameState {

    private final boolean multiplayer;
    private final String ip;

    public GameplayLoadScreen(boolean multiplayer, String ip) {
        this.multiplayer = multiplayer;
        this.ip = ip;
    }

    @Override
    public void enter() {
        List<Runnable> tasks = new ArrayList<>();

        tasks.add(() -> {
            if(multiplayer) {
                GameContext.setContext(GameContextBuilder.buildClientDedicatedServer(ip, 25566));
            } else {
                GameContext.setContext(GameContextBuilder.buildClientLocalServer());
            }
        });

        tasks.add(() -> {
            if(multiplayer) PacketTypes.registerCommon();

            PacketTypes.PONG.registerHandler(new PingPongHandler());
            PacketTypes.ECS_UPDATE.registerHandler(new ECSUpdateHandler());
            PacketTypes.ENTITY_LIFECYCLE.registerHandler(new EntityLifecycleHandler());
            PacketTypes.WORLD_UPDATE.registerHandler(new WorldUpdateHandler());
            PacketTypes.SERVER_PERFORMANCE_METRICS.registerHandler(new ServerPerformanceMetricsHandler());
            PacketTypes.FULL_SNAPSHOT.registerHandler(new FullSnapshotHandler());
        });

        tasks.add(ComponentType::registerSnapshotDeserializers);

        tasks.add(BackgroundStarRenderer::loadAssets);

        tasks.add(() -> {
            if(GameContext.get().isSingleplayer()) {
                ServerContext.get().getServer().start();
            }
        });


        tasks.add(() -> {
            boolean success = GameContext.get().getClientNetwork().connect();
            if(!success) {
                SolarMain.getInstance().setScreen(
                    new LoadingScreen(
                        () -> GameStateManager.get().changeState(
                            new MainMenuScreen(SolarMain.getInstance(), () -> UIManager.get().showMessage("Connection failed!"))
                        ),
                        null
                    )
                );
            }
        });

        SolarMain.getInstance().setScreen(
            new LoadingScreen(
                () -> SolarMain.getInstance().setScreen(new SimTestScreen()),
                tasks
            )
        );
    }

    @Override
    public void exit() {
        GameContextContainer gameCtx = GameContext.get();

        gameCtx.getClientNetwork().disconnect();

        gameCtx.getDispatcher().shutdown();
        gameCtx.getDispatcher().update(30_000);

        if(gameCtx.isSingleplayer()) {
            ServerContext.get().getServer().stop();
        }


        GameContext.clear();
    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public GameStateID getID() {
        return GameStateID.GAMEPLAY;
    }
}
