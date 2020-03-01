package demos.hellowindow;

import core.Application;
import core.Engine;

public class Main {

    /**
     * An example main method that starts the engine, initializes it, and makes a simple report.
     *
     * @param args
     */
    public static void main(String[] args) {

        // Creates an engine object.
        Engine engine = new Engine();

        // Creates a task that will perform the game functionality.
        GameLogic logicTask = new GameLogic(engine);

        // Adds the game logic task to the configuration pool.
        engine.getConfigPool().execute(logicTask);

        try {
            // Starts the engine(on this thread).
            engine.run();
        } catch (Exception e) {
            System.err.println("Engine is shut down due to a problem:");
            e.printStackTrace();
        } finally {
            // Frees the application data
            engine.destroy();
            Application.destroy();
            System.out.println("Engine successfully shut down.");
        }
    }
}
