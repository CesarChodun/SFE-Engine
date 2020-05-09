package com.sfengine.components.engines;

import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineTask;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.SmartWaitQueue;
import com.sfengine.core.synchronization.SyncTask;

import java.util.*;
import java.util.concurrent.*;

/**
 * Class for scheduling the game engine tasks. It <b>SHOULD</b> be running from the first thread.
 *
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public class DefaultEngine implements Engine {

    private ThreadPoolExecutor configPool, fastPool, slowPool;

    private final EngineExecutor mainThread = new OscilatoryEngineExecutor();

    private SmartWaitQueue smartQueue;

    public DefaultEngine() {
        configPool =
                new ThreadPoolExecutor(
                        1, 4, 5, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(128));
        fastPool =
                new ThreadPoolExecutor(
                        2, 4, 30, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(64));
        slowPool =
                new ThreadPoolExecutor(
                        1, 16, 20, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(32));

        smartQueue = new SmartWaitQueue();
    }

    /** Starts the engine. */
    @Override
    public void run() {
        mainThread.run();
    }



    /** Stops the engine(or rather informs the engine thread to stop). */
    @Override
    public void stop() {
        mainThread.shutdown();
    }

    /**
     * Tells whether the engine is currently running.
     *
     * @return True if the engine is running and false if it is not.
     */
    @Override
    public boolean isRunning() {
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
    public void destroy() {
        configPool.shutdown();
        fastPool.shutdown();
        slowPool.shutdown();
    }
}
