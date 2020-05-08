package com.sfengine.core;

import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.SmartWaitQueue;
import com.sfengine.core.synchronization.ThreadPoolSyncTask;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class for scheduling the game engine tasks. It <b>SHOULD</b> be running from the first thread.
 *
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public class DefaultEngine implements Engine {

    /** A minimal amount of tasks to be performed per tick. */
    private static final int MINIMUM_TASKS_PER_TICK = 5;
    /** A percentage of tasks that will be executed per tick(from queue). */
    private static final float TASKS_PER_TICK_SCALING = 0.5f;

    private ThreadPoolExecutor configPool, fastPool, slowPool;

    private SmartWaitQueue smartQueue;

    /** A list of tasks that should be executed once per engine tick. */
    private Set<EngineTask> tickTasks = Collections.synchronizedSet(new HashSet<>());
    /** A list of task for engine to complete. */
    private Queue<EngineTask> tasks = new LinkedList<>();
    /** Tells whether the engine should be running or if it should shut itself down. */
    private volatile boolean running = false;

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
        running = true;

        try {
            while (running) {
                // Performs simple tasks(one time tasks)
                int taskToComplete =
                        (int) (tasks.size() * TASKS_PER_TICK_SCALING) + MINIMUM_TASKS_PER_TICK;
                for (int i = 0; i < taskToComplete && tasks.size() > 0; i++) {
                    synchronized (tasks) {
                        tasks.poll().run();
                    }
                }

                // Performs per-tick tasks.
                List<EngineTask> tmp = new ArrayList<EngineTask>(tickTasks);
                for (EngineTask tickTask : tmp) {
                    if (running == true) {
                        tickTask.run();
                    }
                }
            }
        } catch (AssertionError e) {
            running = false;
            e.printStackTrace();
        }
    }

    /**
     * Adds a task to the task queue. If the task is given to the engine it is ensured that it will
     * be performed within a few engine ticks.
     *
     * @param task Task to be added.
     */
    @Override
    public synchronized void addTask(EngineTask task) {
        tasks.add(task);
    }

    /**
     * Adds a task to the per tick task list. The added task will execute once per tick.
     *
     * @param tickTask Per tick task that will be added to the list.
     */
    @Override
    public void addTickTask(EngineTask tickTask) {
        tickTasks.add(tickTask);
    }

    /**
     * Removes a per tick task from the list.
     *
     * @param tickTask Per tick task to be removed.
     */
    @Override
    public void removeTickTask(EngineTask tickTask) {
        tickTasks.remove(tickTask);
    }

    /** Stops the engine(or rather informs the engine thread to stop). */
    @Override
    public void stop() {
        running = false;
    }

    /**
     * Tells whether the engine is currently running.
     *
     * @return True if the engine is running and false if it is not.
     */
    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns publicly available thread pool for configuration related tasks.
     *
     * @return
     */
    public ThreadPoolExecutor getConfigPool() {
        return configPool;
    }

    /**
     * Returns publicly available thread pool for time sensitive tasks.
     *
     * @return
     */
    public ThreadPoolExecutor getFastPool() {
        return fastPool;
    }

    /**
     * Returns publicly available thread pool for large tasks(eg. large IO).
     *
     * @return
     */
    public ThreadPoolExecutor getSlowPool() {
        return slowPool;
    }

    @Override
    public void addConfig(Runnable run, Dependency... dependencies) {
        smartQueue.addTask(new ThreadPoolSyncTask(configPool, run, dependencies));
    }

    @Override
    public void addFast(Runnable run, Dependency... dependencies) {
        smartQueue.addTask(new ThreadPoolSyncTask(fastPool, run, dependencies));
    }

    @Override
    public void addSlow(Runnable run, Dependency... dependencies) {
        smartQueue.addTask(new ThreadPoolSyncTask(slowPool, run, dependencies));
    }

    @Override
    public void destroy() {
        configPool.shutdown();
        fastPool.shutdown();
        slowPool.shutdown();
    }
}
