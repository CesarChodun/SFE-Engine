package core;

/**
 * Interface for creating an engine tasks. Both per tick tasks and one time tasks can be created
 * using this class.
 *
 * @author Cezary Chodun
 */
public interface EngineTask extends Runnable {

    @Override
    /** Performs the task. */
    public void run() throws AssertionError;
}
