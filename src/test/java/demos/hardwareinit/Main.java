package demos.hardwareinit;

import com.sfengine.core.Application;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;

public class Main {

    /**
     * An example main method that starts the engine, initializes it, and makes a simple report.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Creates a separate thread for the game logic.
        Thread logic = new Thread(new GameLogic());
        logic.start();

        try {
            // Starts the engine(on this thread).
            EngineFactory.runEngine();
        } catch (Exception e) {
            System.err.println("Engine is shut down due to a problem:");
            e.printStackTrace();
        } finally {
            // Frees the application data
            EngineFactory.destroyEngine();
            Application.destroy();
            System.out.println("Engine successfully shut down.");
        }
    }
}
