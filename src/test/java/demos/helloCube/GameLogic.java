package demos.helloCube;

import com.sfengine.components.contexts.DefaultContexts;
import com.sfengine.components.resources.MemoryBin;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.Application;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.HardwareManager;
import com.sfengine.core.synchronization.Dependency;
import demos.helloCube.rendering.InitializeRendering;
import com.sfengine.components.util.DefaultResourceConverter;
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
    private final MemoryBin toDestroy = new MemoryBin();

    @Override
    public void run() throws AssertionError {
        redirectLoggers();

        // Convert resources
        DefaultResourceConverter converter = new DefaultResourceConverter();
        converter.runConversion();

        // Application and hardware initialization.
        Application.init(CONFIG_FILE);
        HardwareManager.init();

        CFrame frame = new CFrame("MyFrame");
        frame.setCloseCallback(toDestroy);

        List<Dependency> deps = DefaultContexts.getDependencies();
        deps.add(frame.getDependency());

        Dependency[] depsArr = new Dependency[deps.size()];
        for (int i = 0; i < deps.size(); i++)
            depsArr[i] = deps.get(i);

        // Submits rendering task to the engine configuration queue.
        engine.addConfig(
                () -> {
                    // Creating the rendering task.
                    InitializeRendering rendTask =
                            new InitializeRendering(frame.getWindow());
                    toDestroy.add(rendTask);
                    engine.addTask(rendTask);
                }, depsArr);

        System.out.println("Window manager done!");
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
