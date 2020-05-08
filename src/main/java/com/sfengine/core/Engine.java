package com.sfengine.core;

import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.synchronization.Dependency;

/**
 * Synchronization engine interface.
 *
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public interface Engine extends Runnable, Destroyable {

    /**
     * Adds a task to the task queue. If the task is given to the engine it is ensured that it will
     * be performed within a few engine ticks.
     *
     * @param task Task to be added.
     */
    void addTask(EngineTask task);

    void addConfig(Runnable run, Dependency... dependencies);

    void addFast(Runnable run, Dependency... dependencies);

    void addSlow(Runnable run, Dependency... dependencies);

    /**
     * Adds a task to the per tick task list. The added task will execute once per tick.
     *
     * @param tickTask Per tick task that will be added to the list.
     */
    void addTickTask(EngineTask tickTask);

    /**
     * Removes a per tick task from the list.
     *
     * @param tickTask Per tick task to be removed.
     */
    void removeTickTask(EngineTask tickTask);

    /**
     * Tells whether the engine is currently running.
     *
     * @return True if the engine is running and false if it is not.
     */
    boolean isRunning();

    /** Stops the engine(or rather informs the engine thread to stop). */
    void stop();
}
