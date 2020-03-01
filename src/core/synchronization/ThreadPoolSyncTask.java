package core.synchronization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A synchronized task that will be invoked inside a specified thread pool.
 *
 * @author Cezary Chodun
 * @since 26.20.2020
 */
public class ThreadPoolSyncTask implements SynchronizedTask {

    private List<Dependency> deps;
    private ThreadPoolExecutor pool;
    private Runnable task;

    /**
     * Creates a synchronized task that will be invoked inside a specified thread pool.
     *
     * @param pool the pool.
     * @param task the task.
     * @param dependencies task's dependencies(or null).
     */
    public ThreadPoolSyncTask(ThreadPoolExecutor pool, Runnable task, Dependency... dependencies) {
        this.pool = pool;
        this.task = task;

        deps = new ArrayList<>();
        for (Dependency d : dependencies) deps.add(d);
    }

    @Override
    public void run() {
        pool.execute(task);
    }

    @Override
    public List<Dependency> dependencies() {
        return deps;
    }
}
