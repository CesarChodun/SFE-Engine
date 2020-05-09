package com.sfengine.core.synchronization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A synchronized task that will be invoked inside a specified thread pool.
 *
 * @author Cezary Chodun
 * @since 26.20.2020
 */
public class SyncTask implements SynchronizedTask {

    private List<Dependency> deps;
    private Executor exec;
    private Runnable task;

    /**
     * Creates a synchronized task that will be invoked inside a specified thread pool.
     *
     * @param executor the executor.
     * @param task the task.
     * @param dependencies task's dependencies(or null).
     */
    public SyncTask(Executor executor, Runnable task, Dependency... dependencies) {
        this.exec = executor;
        this.task = task;

        deps = new ArrayList<>();
        for (Dependency d : dependencies) {
            deps.add(d);
        }
    }

    @Override
    public void run() {
        exec.execute(task);
    }

    @Override
    public List<Dependency> dependencies() {
        return deps;
    }
}
