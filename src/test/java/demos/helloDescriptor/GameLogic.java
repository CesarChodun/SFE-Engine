package demos.helloDescriptor;

import com.sfengine.components.contexts.DefaultContexts;
import com.sfengine.components.resources.MemoryBin;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.Application;
import com.sfengine.core.HardwareManager;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.synchronization.Dependency;
import demos.helloDescriptor.rendering.InitializeRendering;
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
 * @since 10.01.2020
 */
public class GameLogic implements Runnable {

    private static final String CONFIG_FILE = "demos/hellodescriptor";
    private Engine engine = EngineFactory.getEngine();
    final MemoryBin toDestroy = new MemoryBin();

    public GameLogic() {

    }

    @Override
    public void run() throws AssertionError {
        redirectLoggers();

        // Convert resources
        DefaultResourceConverter converter = new DefaultResourceConverter();
        converter.runConversion();

        // Awaits for conversion to complete
        converter.await();

        // Application and hardware initialization.
        Application.init(CONFIG_FILE);
        HardwareManager.init();

        CFrame frame = new CFrame("MyFrame");
        frame.setCloseCallback(toDestroy);

        System.err.println("CFrame work submited!");

        List<Dependency> deps = DefaultContexts.getDependencies();
        deps.add(frame.getDependency());

        Dependency[] depsArr = new Dependency[deps.size()];
        for (int i = 0; i < deps.size(); i++)
            depsArr[i] = deps.get(i);

        engine.addConfig(
                () -> {
                    // Creating the rendering task.
                    InitializeRendering rendTask =
                            new InitializeRendering(frame.getWindow());
                    engine.addTask(rendTask);
                    toDestroy.add((Destroyable) rendTask);
                    frame.setCloseCallback(toDestroy);
                },
                depsArr);

        System.err.println("Window manager done!");
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
