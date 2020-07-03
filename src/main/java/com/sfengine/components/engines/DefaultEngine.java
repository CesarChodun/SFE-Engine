package com.sfengine.components.engines;

import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineTask;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.SmartWaitQueue;
import com.sfengine.core.synchronization.SyncTask;

import java.util.concurrent.*;

/**
 * Class for scheduling the game engine tasks. It <b>SHOULD</b> be running from the first thread.
 *
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public class DefaultEngine implements Engine {

    private static final int
            CONFIG_POOL_MAX_THREADS = 64,
            FAST_POOL_MAX_THREADS = 64,
            SLOW_POOL_MAX_THREADS = 64;

    private volatile ThreadPoolExecutor configPool, fastPool, slowPool;

    private final EngineExecutor mainThread = new OscillatoryEngineExecutor();

    private final SmartWaitQueue smartQueue = new SmartWaitQueue();

    public DefaultEngine() {
        configPool =
                new ThreadPoolExecutor(
                        1,
                        4,
                        5,
                        TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<Runnable>(CONFIG_POOL_MAX_THREADS));
        fastPool =
                new ThreadPoolExecutor(
                        2,
                        4,
                        30,
                        TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<Runnable>(FAST_POOL_MAX_THREADS));
        slowPool =
                new ThreadPoolExecutor(
                        1,
                        16,
                        20,
                        TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<Runnable>(SLOW_POOL_MAX_THREADS));

    }

    /** Starts the engine. */
    @Override
    public synchronized void run() {
        mainThread.run();
    }



    /** Stops the engine(or rather informs the engine thread to stop). */
    @Override
    public synchronized void stop() {
        mainThread.shutdown();
    }

    /**
     * Tells whether the engine is currently running.
     *
     * @return True if the engine is running and false if it is not.
     */
    @Override
    public synchronized boolean isRunning() {
        return mainThread.isRunning();
    }

    @Override
    public void addTask(Runnable run, Dependency... dependencies) {
        smartQueue.addTask(new SyncTask(mainThread, run, dependencies));
    }

    @Override
    public void addConfig(Runnable run, Dependency... dependencies) {
        smartQueue.addTask(new SyncTask(configPool, run, dependencies));
    }

    @Override
    public void addFast(Runnable run, Dependency... dependencies) {
        smartQueue.addTask(new SyncTask(fastPool, run, dependencies));
    }

    @Override
    public void addSlow(Runnable run, Dependency... dependencies) {
        smartQueue.addTask(new SyncTask(slowPool, run, dependencies));
    }

    @Override
    public void addTickTask(EngineTask tickTask) {
        mainThread.addTickTask(tickTask);
    }

    @Override
    public void removeTickTask(EngineTask tickTask) {
        mainThread.removeTickTask(tickTask);
    }

    @Override
    public synchronized void destroy() {
        configPool.shutdown();
        fastPool.shutdown();
        slowPool.shutdown();
    }
}
