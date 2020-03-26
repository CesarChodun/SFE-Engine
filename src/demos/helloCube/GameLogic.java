package demos.helloCube;

import components.util.EngineInitializationTask;
import core.Engine;
import core.synchronization.DependencyFence;
import demos.util.DefaultResourceConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello descriptor demo logic.
 *
 * @author Cezary Chodun
 * @since 4.03.2020
 */
public class GameLogic implements Runnable {

    private static final String CONFIG_FILE = "demos/hellocube";
    private Engine engine;

    public GameLogic(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void run() throws AssertionError {
        redirectLoggers();

        // Convert resources
        DefaultResourceConverter converter = new DefaultResourceConverter();
        converter.runConversion();

        // Semaphore indicating initialization state
        DependencyFence initialized = new DependencyFence(0);

        // Adding engine initialization task to the engine task queue
        engine.addTask(new EngineInitializationTask(initialized, CONFIG_FILE));

        // Awaits for conversion to complete
        converter.await();

        // Creating a thread that will wait until the engine is initialized and then
        // it will create the window.
        engine.addConfigSMQ(new WindowManager(engine, initialized));
    }

    /**
     * Redirects supplied loggers to the console.
     *
     * @param loggers The loggers to be redirected.
     */
    private static void redirectLoggersToConsole(List<Logger> loggers) {
        Handler consoleLog = new ConsoleHandler();
        consoleLog.setLevel(Level.ALL);

        for (Logger lg : loggers) {
            lg.addHandler(consoleLog);
        }
    }

    /** Redirects loggers output */
    private void redirectLoggers() {
        List<Logger> loggers = new ArrayList<Logger>();
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.ALL);
        loggers.add(logger);

        redirectLoggersToConsole(loggers);
    }
}
