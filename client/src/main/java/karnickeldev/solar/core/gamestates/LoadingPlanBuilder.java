package karnickeldev.solar.core.gamestates;

import karnickeldev.solar.assetmanager.Asset;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * @author KarnickelDev
 * @since 27.11.2025
 **/
public final class LoadingPlanBuilder {

    private final List<Runnable> syncTasks;
    private final List<Runnable> asyncTasks;
    private final List<BooleanSupplier> conditions;
    private final List<Asset> assets;

    public LoadingPlanBuilder() {
        syncTasks = new ArrayList<>();
        asyncTasks = new ArrayList<>();
        conditions = new ArrayList<>();
        assets = new ArrayList<>();
    }

    public LoadingPlanBuilder syncTask(Runnable syncTask) {
        syncTasks.add(syncTask);
        return this;
    }

    public LoadingPlanBuilder asyncTask(Runnable asyncTask) {
        asyncTasks.add(asyncTask);
        return this;
    }

    public LoadingPlanBuilder waitUntil(BooleanSupplier condition) {
        conditions.add(condition);
        return this;
    }

    public LoadingPlanBuilder loadAsset(Asset... asset) {
        assets.addAll(List.of(asset));
        return this;
    }

    public LoadingPlan build() {
        return new LoadingPlan(syncTasks, asyncTasks, conditions, assets);
    }

    public static LoadingPlan empty() {
        return new LoadingPlanBuilder().build();
    }

}
