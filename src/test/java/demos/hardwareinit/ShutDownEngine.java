package demos.hardwareinit;

import com.sfengine.core.Engine;
import java.util.concurrent.Semaphore;

public class ShutDownEngine implements Runnable {

    private Semaphore shouldShutDown;
    private Engine engine;

    public ShutDownEngine(Engine engine, Semaphore shouldShutDown) {
        this.shouldShutDown = shouldShutDown;
        this.engine = engine;
    }

    @Override
    public void run() {
        try {
            // Waits for the semaphore
            shouldShutDown.acquire();

        } catch (InterruptedException e) {
            System.err.println("Engine shutdown thread was interrupted.");
            e.printStackTrace();
        } finally {
            // Shuts the engine down.
            engine.stop();
        }
    }
}
