package com.sfengine.core;

/**
 * Interface for creating an engine tasks. Both per tick tasks and one time tasks can be created
 * using this class.
 *
 * @author Cezary Chodun
 */
public interface EngineTask extends Runnable {

    /** Performs the task. */
    @Override
    public void run() throws AssertionError;
}
