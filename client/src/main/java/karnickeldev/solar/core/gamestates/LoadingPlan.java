package karnickeldev.solar.core.gamestates;

import karnickeldev.solar.assetmanager.Asset;
import karnickeldev.solar.assetmanager.AssetWrapper;
import karnickeldev.solar.scheduler.ClientScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

/**
 * @author KarnickelDev
 * @since 27.11.2025
 **/
public final class LoadingPlan {

    private final List<Runnable> syncTasks;
    private final List<Runnable> asyncTasks;
    private final List<BooleanSupplier> conditions;
    private final List<Asset> assets;

    private final ClientScheduler scheduler = new ClientScheduler();
    private final List<Future<?>> futureConditions = new ArrayList<>();

    private int syncIndex = 0;
    private volatile boolean begun = false;
    private final int totalWork;

    private final AtomicReference<Throwable> failure = new AtomicReference<>(null);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public LoadingPlan(List<Runnable> syncTasks, List<Runnable> asyncTasks, List<BooleanSupplier> conditions, List<Asset> assets) {
        this.syncTasks = syncTasks != null ? syncTasks : List.of();
        this.asyncTasks = asyncTasks != null ? asyncTasks : List.of();
        this.conditions = conditions != null ? conditions : List.of();
        this.assets = assets != null ? assets : List.of();

        totalWork = this.syncTasks.size() + this.asyncTasks.size() + this.conditions.size() + this.assets.size();
    }

    public boolean failed() {
        return failure.get() != null;
    }

    public Throwable getFailure() {
        return failure.get();
    }

    private void fail(Throwable t) {
        if (t == null) return;
        if (failure.compareAndSet(null, t)) {
            cancel();
        }
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    /** Best effort cancelling of LoadingPlan */
    public void cancel() {
        if (!cancelled.compareAndSet(false, true)) return;
        for(Future<?> f: futureConditions) {
            try {
                f.cancel(true);
            } catch (Throwable ignored) {}
        }
    }

    public void begin() {
        if(begun) return;
        begun = true;

        // submit sync tasks
        for(Runnable task: syncTasks) {
            scheduler.main().dispatch(() -> {
                if(failed() || isCancelled()) return;
                try {
                    task.run();
                    syncIndex++;
                } catch (Throwable t) {
                    fail(t);
                }
            });
        }

        // submit async tasks
        for(Runnable task: asyncTasks) {
            FutureTask<?> futureTask = new FutureTask<>(() -> {
                if(failed() || isCancelled()) return null;
                try {
                    task.run();
                } catch (Throwable t) {
                    fail(t);
                }
                return null;
            });

            futureConditions.add(futureTask);
            scheduler.scheduleAsync(futureTask);
        }

        // load assets
        for(Asset a : assets) {
            try {
                if (!AssetWrapper.getInstance().isLoaded(a)) {
                    AssetWrapper.getInstance().loadGlobal(a);
                }
            } catch (Throwable t) {
                fail(t);
            }
        }

    }

    public boolean step(int ms) {
        if(failed() || isCancelled()) return true;

        // IMPORTANT: DO THIS FIRST
        if(!scheduler.main().update(ms)) return false;

        if(!AssetWrapper.getInstance().update(ms)) return false;

        for (BooleanSupplier c : conditions) {
            boolean ok;
            try {
                ok = c.getAsBoolean();
            } catch (Throwable t) {
                fail(t);
                return true;
            }
            if (!ok) return false;
        }

        for(Future<?> f: futureConditions) {
            if(!f.isDone()) return false;

            try {
                f.get();
            } catch (ExecutionException ex) {
                fail(ex.getCause() != null ? ex.getCause() : ex);
                return true;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                fail(ie);
                return true;
            } catch (CancellationException ce) {
                cancel();
                return true;
            } catch (Throwable t) {
                fail(t);
                return true;
            }
        }

        return true;
    }

    public float getProgress() {
        if(totalWork == 0) return 1f;

        int completed = syncIndex;

        for(Future<?> f : futureConditions) {
            if(f.isDone()) completed++;
        }

        for (BooleanSupplier c : conditions) {
            try { if (c.getAsBoolean()) completed++; }
            catch (Throwable ignored) {}
        }

        for(Asset a: assets) {
            if(AssetWrapper.getInstance().isLoaded(a)) completed++;
        }

        return completed / (float) totalWork;
    }

}
