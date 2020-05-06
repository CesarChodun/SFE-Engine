package demos.hellowindow;

import com.sfengine.core.Application;
import com.sfengine.core.Engine;
import com.sfengine.core.EngineTask;
import java.util.concurrent.Semaphore;

public class GameLogic implements EngineTask {

    /** Waits until the engine is initialized and then creates the window. */
    private static class WaitForConfig implements Runnable {

        private Semaphore wait;
        private Engine engine;

        public WaitForConfig(Engine engine, Semaphore wait) {
            this.engine = engine;
            this.wait = wait;
        }

        @Override
        public void run() {
            try {
                // Waits for the initialization
                wait.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Adds the task to the engine
            // Note that the wait is necessary to get right asset("Application.getConfigAssets()")
            engine.addTask(new WindowManager(engine, Application.getConfigAssets()));
        }
    }

    private Engine engine;

    public GameLogic(Engine engine) {
        this.engine = engine;
    }

    @Override
    public void run() throws AssertionError {
        // Semaphore indicating initialization state
        Semaphore initialized = new Semaphore(0);

        // Adding engine initialization task to the engine task queue
        engine.addTask(new InitializeEngine(initialized));

        // Creating a thread that will wait until the engine is initialized and then
        // it will create the window.
        Thread waitForConfig = new Thread(new WaitForConfig(engine, initialized));
        waitForConfig.start();
    }
}
