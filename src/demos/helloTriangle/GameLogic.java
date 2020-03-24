package demos.helloTriangle;

import core.Engine;
import demos.util.DefaultResourceConverter;
import java.util.concurrent.Semaphore;

import components.util.EngineInitializationTask;

public class GameLogic implements Runnable {

    private static final String CONFIG_FILE = "demos/hellotriangle";
    private Engine engine;

    public GameLogic(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void run() throws AssertionError {

        // Asset converter
        DefaultResourceConverter converter = new DefaultResourceConverter();

        // Performing asset conversion
        converter.runConversion();

        // Semaphore indicating initialization state
        Semaphore initialized = new Semaphore(0);

        // Adding engine initialization task to the engine task queue
        engine.addTask(new EngineInitializationTask(initialized, CONFIG_FILE));

        // Awaiting for the asset conversion
        converter.await();

        // Creating a thread that will wait until the engine is initialized and then
        // it will create the window.
        Thread waitForConfig = new Thread(new WindowManager(engine, initialized));
        waitForConfig.start();
    }
}
