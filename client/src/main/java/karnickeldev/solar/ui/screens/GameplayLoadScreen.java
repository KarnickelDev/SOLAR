package karnickeldev.solar.ui.screens;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.context.GameContextBuilder;
import karnickeldev.solar.context.GameContextContainer;
import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.core.SimTestScreen;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.GameState;
import karnickeldev.solar.core.gamestates.GameStateID;
import karnickeldev.solar.ecs.components.ComponentType;
import karnickeldev.solar.network.net.handlers.*;
import karnickeldev.solar.network.packets.PacketTypes;
import karnickeldev.solar.render.BackgroundStarRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : KarnickelDev
 * @since : 04.07.2025
 **/
public class GameplayLoadScreen implements GameState {

    private final boolean multiplayer;

    public GameplayLoadScreen(boolean multiplayer) {
        this.multiplayer = multiplayer;
    }

    @Override
    public void enter() {

        List<Runnable> tasks = new ArrayList<>();

        tasks.add(() -> {
            if(multiplayer) {
                GameContext.setContext(GameContextBuilder.buildClientDedicatedServer("45.81.233.223", 25566));
                //GameContext.setContext(GameContextBuilder.buildClientDedicatedServer("localhost", 25566));
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

        tasks.add(() -> GameContext.get().getNetworkThread().start());

        tasks.add(() -> GameContext.get().getClientNetwork().connect());

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

        gameCtx.getDispatcher().shutdownGracefully();
        gameCtx.getDispatcher().update();

        if(gameCtx.isSingleplayer()) {
            ServerContext.get().getServer().stop();
        }

        gameCtx.getNetworkThread().stop();

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
