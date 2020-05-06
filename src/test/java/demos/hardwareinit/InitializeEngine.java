package demos.hardwareinit;

import com.sfengine.core.Application;
import com.sfengine.core.EngineTask;
import com.sfengine.core.HardwareManager;
import com.sfengine.core.result.VulkanException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class InitializeEngine implements EngineTask {

    static final String CONFIG_FILE = "demos/hardwareinit";
    private Semaphore workDone;

    public InitializeEngine(Semaphore workDone) {
        this.workDone = workDone;
    }

    @Override
    public void run() {
        /*
         *     This method("run()") will be invoked first.
         *    It is guaranteed that it will be run on the first thread.
         *    And this is the place where you want to start programming your game.
         *    You can think about it like a 'main' method.
         *
         */

        try {
            Application.init(CONFIG_FILE);

            System.out.println("Application data succesfully initialized!");

        } catch (FileNotFoundException e) {

            System.err.println("Failed to find the configuration file(\"" + CONFIG_FILE + "\")");
            e.printStackTrace();
        }

        try {

            HardwareManager.init(Application.getApplicationInfo(), Application.getConfigAssets());
            System.out.println("Hardware succesfully initialized!");
        } catch (VulkanException e) {

            System.err.println("Failed to initialize hardware due to a vulkan problem.");
            e.printStackTrace();
        } catch (IOException e) {

            System.err.println("Failed to initialize hardware due to an input(or output) error.");
            e.printStackTrace();
        }

        workDone.release();
    }
}
