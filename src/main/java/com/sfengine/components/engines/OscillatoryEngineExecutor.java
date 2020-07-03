package com.sfengine.components.engines;

import com.sfengine.core.engine.EngineTask;
import com.sfengine.core.synchronization.Dependency;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OscillatoryEngineExecutor implements EngineExecutor {
    /** A minimal amount of tasks to be performed per tick. */
    private static final int MINIMUM_TASKS_PER_TICK = 5;
    /** A percentage of tasks that will be executed per tick(from queue). */
    private static final float TASKS_PER_TICK_SCALING = 0.5f;

    /** A list of tasks that should be executed once per engine tick. */
    private Set<EngineTask> tickTasks = Collections.synchronizedSet(new HashSet<>());
    /** A list of task for engine to complete. */
    private Queue<Runnable> tasks = new LinkedList<>();
    /** Tells whether the engine should be running or if it should shut itself down. */
    private volatile boolean running = false;

    private volatile boolean acceptNewTasks = true;

    @Override
    public void execute(@NotNull Runnable command) {
        if (acceptNewTasks)
            tasks.add(command);
    }

    @Override
    public void run() {
        running = true;

        try {
            while (acceptNewTasks || tasks.size() > 0) {
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
        finally {
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void shutdown() {
        acceptNewTasks = false;
    }

    /**
     * Adds a task to the per tick task list. The added task will execute once per tick.
     *
     * @param tickTask Per tick task that will be added to the list.
     */
    @Override
    public void addTickTask(EngineTask tickTask) {
        if (acceptNewTasks)
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
}
