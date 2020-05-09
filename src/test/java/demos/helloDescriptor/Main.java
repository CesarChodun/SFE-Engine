package demos.helloDescriptor;

import com.sfengine.core.Application;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;

/**
 * Main class of the hello descriptors demo.
 *
 * @author Cezary Chodun
 * @since 10.01.2020
 */
public class Main {

    public static final String DEMO_NAME = "helloDescriptor";

    /**
     * An example main method that starts the engine, initializes it, and makes a simple report.
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Running \"Hello Descriptor\" demo.");

        // Gets default synchronization engine
        Engine engine = EngineFactory.getEngine();

        // Creates a task that will perform the game functionality.
        GameLogic logicTask = new GameLogic(engine);

        // Creates a separate thread for the game logic.
        Thread logic = new Thread(logicTask);
        logic.start();

        try {
            // Starts the engine(on this thread).
            engine.run();
        } catch (Exception e) {
            System.err.println("Engine was shut down due to a problem:");
            e.printStackTrace();
        } finally {
            // Frees the application data
            engine.destroy();
            Application.destroy();
            System.out.println("Engine successfully shut down.");
        }
    }
}
