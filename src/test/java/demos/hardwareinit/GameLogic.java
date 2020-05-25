package demos.hardwareinit;

import com.sfengine.core.Application;
import com.sfengine.core.HardwareManager;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.engine.EngineTask;

public class GameLogic implements EngineTask {

    static final String CONFIG_FILE = "demos/hardwareinit";
    /** Our engine object. We need it to shut it down when we finish our work. */
    private Engine engine = EngineFactory.getEngine();

    @Override
    public void run() throws AssertionError {
        System.out.print("The engine is running in the ");

        if (Application.RELEASE) {
            System.out.print("'RELEASE' mode. ");
        } else {
            System.out.print("developer mode. ");
        }

        System.out.print("With the debug information turned ");

        if (Application.DEBUG) {
            System.out.print("ON.\n");
        } else {
            System.out.print("OFF.\n");
        }

        // Application and hardware initialization.
        Application.init(CONFIG_FILE);
        HardwareManager.init();

        EngineReport report = new EngineReport();

        // Adding engine tasks to the engine.
        // They will be invoked on the main thread.
        // In the same order as here.
        engine.addTask(report, HardwareManager.getDependency());

        // A task that will stop the engine.
        engine.addConfig(() -> {engine.stop();}, report.getDependency());
    }
}
