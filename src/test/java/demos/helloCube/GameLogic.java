package demos.helloCube;

import com.sfengine.core.Application;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.HardwareManager;
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
    private Engine engine = EngineFactory.getEngine();

    public GameLogic() {

    }

    @Override
    public void run() throws AssertionError {
        redirectLoggers();

        // Convert resources
        DefaultResourceConverter converter = new DefaultResourceConverter();
        converter.runConversion();

        Application.init(CONFIG_FILE);
        HardwareManager.init();

        // Creating a thread that will wait until the engine is initialized and then
        // it will create the window.
        engine.addConfig(new WindowManager());
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
