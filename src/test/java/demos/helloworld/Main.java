package demos.helloworld;

import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.engine.EngineTask;

public class Main {

    private static class ExampleClass implements Runnable, EngineTask {

        /** Our engine object. We need it to shut it down when we finish our work. */
        private Engine engine = EngineFactory.getEngine();

        @Override
        public void run() {
            /*
             *     This method("run()") will be invoked first.
             *    It is guaranteed that it will be run on the first thread.
             *    And this is the place where you want to start programming your game.
             *    You can think about it like a 'main' method.
             *
             */

            System.out.println("The engine is running.");

            System.out.println("Hello Vulkan Game Engine!");

            engine.stop();
        }
    }

    /**
     * An example main method that starts the engine.
     *
     * @param args
     */
    public static void main(String[] args) {

        // Creates a task that will be performed by the engine.
        ExampleClass example = new ExampleClass();

        // Creates a separate thread for the game logic.
        Thread exampleThread = new Thread(example);
        exampleThread.start();

        try {
            // Starts the engine(on this thread).
            EngineFactory.runEngine();
        } catch (Exception e) {
            System.err.println("Engine is shut down due to a problem:");
            e.printStackTrace();
        } finally {
            // Frees the application data
            EngineFactory.destroyEngine();
            System.out.println("Engine successfully shut down.");
        }
    }
}
