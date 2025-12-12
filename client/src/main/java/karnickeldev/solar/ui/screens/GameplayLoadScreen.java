package karnickeldev.solar.ui.screens;

import karnickeldev.solar.context.*;
import karnickeldev.solar.core.SimTestScreen;
import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.core.gamestates.*;
import karnickeldev.solar.ecs.components.ComponentType;
import karnickeldev.solar.network.net.handlers.*;
import karnickeldev.solar.network.packets.PacketTypes;
import karnickeldev.solar.render.BackgroundStarRenderer;
import karnickeldev.solar.render.orbitupdate.OrbitUpdater;
import karnickeldev.solar.ui.core.UIManager;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.util.threadlayout.ClientThreadLayout;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author KarnickelDev
 * @since 04.07.2025
 **/
public final class GameplayLoadScreen implements GameState {

    private final boolean multiplayer;
    private final String ip;

    public GameplayLoadScreen(boolean multiplayer, String ip) {
        this.multiplayer = multiplayer;
        this.ip = ip;
    }

    @Override
    public LoadingPlan preEnterLoadingPlan() {
        LoadingPlanBuilder plan = new LoadingPlanBuilder();

        // IMPORTANT: MUST BE FIRST STEP!!!
        if(multiplayer) {
            ClientThreadLayout threadLayout = ClientThreadLayout.create(1,true, true);
            GameContext.setContext(GameContextBuilder.buildClientDedicatedServer(ip, 25566, threadLayout));
            Logger.log(Logger.STARTUP, "Using ThreadLayout: " + threadLayout);
        } else {
            ClientThreadLayout threadLayout = ClientThreadLayout.create(1,true, false);
            GameContext.setContext(GameContextBuilder.buildClientLocalServer(threadLayout));
            Logger.log(Logger.STARTUP, "Using ThreadLayout: " + threadLayout);
        }

        plan.syncTask(() -> {
                if (multiplayer) PacketTypes.registerCommon();

                PacketTypes.PONG.registerHandler(new PingPongHandler());
                PacketTypes.TIMESTAMP.registerHandler(new TimestampHandler());
                PacketTypes.ECS_UPDATE.registerHandler(new ECSUpdateHandler());
                PacketTypes.ENTITY_LIFECYCLE.registerHandler(new EntityLifecycleHandler());
                PacketTypes.WORLD_UPDATE.registerHandler(new WorldUpdateHandler());
                PacketTypes.SERVER_PERFORMANCE_METRICS.registerHandler(new ServerPerformanceMetricsHandler());
                PacketTypes.FULL_SNAPSHOT.registerHandler(new FullSnapshotHandler());
            })
            .syncTask(ComponentType::registerSnapshotDeserializers)
            .syncTask((BackgroundStarRenderer::loadAssets))
            .syncTask(() -> {
                if (GameContext.get().isSingleplayer()) {
                    ServerContext.get().getServer().start();
                }
            });
//            .syncTask(() -> {
//                boolean success = GameContext.get().getClientNetwork().connect();
//                if (!success) {
//                    throw new RuntimeException("Connection failed");
//                }
//            })

        // connect task
        plan.asyncTask(() -> {
            if(!GameContext.get().getClientNetwork().connect()) throw new RuntimeException("Failed to connect to the server");
        });

        plan.waitUntil(() -> GameContext.get().getClientNetwork().isConnected())
            .waitUntil(() -> {
                GameContextContainer ctx = GameContext.get();
                boolean worldReady = ctx.getWorldManager().containsWorld(1);
                if (worldReady) return true;

                ctx.getClock().updateFrameClockTime();

                // do not use current here, we manually subtract PacketSyncDelay
                ctx.getSyncLayer().update(ctx.getClock().getFrameClockTime());

                // probably better to do after processing input
                ctx.getScheduler().main().update();

                OrbitUpdater p = ctx.getPlanetoidRenderSystem().orbitUpdater;
                if (ctx.getWorldManager().containsWorld(1)) {
                    p.prepare(GameContext.get().getWorldManager().getWorld(1).getECS());
                    return true;
                }

                return false;
            })
            .waitUntil(() -> !GameContext.get().getPlanetoidRenderSystem().orbitUpdater.isWarmupActive())
            ;

        return plan.build();
    }

    @Override
    public void enter() {
        SolarMain.getInstance().setScreen(new SimTestScreen());
    }

    @Override
    public void exit() {

    }

    @Override
    public LoadingPlan postExitLoadingPlan() {
        LoadingPlanBuilder plan = new LoadingPlanBuilder();

        GameContextContainer gameCtx = GameContext.get();
        plan.syncTask(() -> gameCtx.getPlanetoidRenderSystem().shutdown());
        plan.asyncTask(() -> gameCtx.getClientNetwork().disconnect());
        plan.syncTask(() -> {
            gameCtx.getScheduler().shutdown();
            gameCtx.getScheduler().main().update(30_000);
            gameCtx.getScheduler().timer().update(System.currentTimeMillis());
        });

        plan.syncTask(() -> {
            if(gameCtx.isSingleplayer()) {
                ServerContext.get().getServer().stop();
            }
        });

        plan.syncTask(GameContext::clear);

        return plan.build();
    }

    @Override
    public GameStateID getID() {
        return GameStateID.GAMEPLAY;
    }
}
