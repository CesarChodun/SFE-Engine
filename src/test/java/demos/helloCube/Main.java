package demos.helloCube;

import com.sfengine.core.Application;
import com.sfengine.core.engine.EngineFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * Main class of the hello cube demo.
 *
 * @author Cezary Chodun
 * @since 10.01.2020
 */
@SpringBootApplication
public class Main implements CommandLineRunner {

    public static final String DEMO_NAME = "hellocube";

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("Running \"Hello Cube\" demo.");

        // Creates a task that will perform the game functionality.
        GameLogic logicTask = new GameLogic();

        // Creates a separate thread for the game logic.
        Thread logic = new Thread(logicTask);
        logic.start();

        try {
            // Starts the engine(on this thread).
            EngineFactory.runEngine();
        } catch (Exception e) {
            System.err.println("Engine was shut down due to a problem:");
            e.printStackTrace();
        } finally {
            // Frees the application data
            EngineFactory.destroyEngine();
            Application.destroy();
            System.out.println("Engine successfully shut down.");
        }
    }
}
