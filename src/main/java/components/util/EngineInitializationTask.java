package main.java.components.util;

import main.java.core.Application;
import main.java.core.EngineTask;
import main.java.core.HardwareManager;
import main.java.core.result.VulkanException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes engine resources.
 *
 * @author Cezary
 * @since 10.01.2020
 */
public class EngineInitializationTask implements EngineTask {

    private static final Logger logger = Logger.getLogger(EngineInitializationTask.class.getName());

    private String configFile;
    private Semaphore workDone;

    public EngineInitializationTask(Semaphore workDone, String configFile) {
        this.workDone = workDone;
        this.configFile = configFile;
    }

    @Override
    public void run() {
        try {
            // Initializing application
            Application.init(configFile);
            logger.log(Level.INFO, "Application data succesfully initialized!");

        } catch (FileNotFoundException e) {
            logger.log(Level.FINE, "Failed to find the configuration file(\"" + configFile + "\")");
            e.printStackTrace();
        }

        try {
            // Initializing hardware
            HardwareManager.init(Application.getApplicationInfo(), Application.getConfigAssets());
            logger.log(Level.INFO, "Hardware succesfully initialized!");
        } catch (VulkanException e) {
            logger.log(Level.FINE, "Failed to initialize hardware due to a vulkan problem.");
            e.printStackTrace();
        } catch (IOException e) {
            logger.log(
                    Level.FINE, "Failed to initialize hardware due to an input(or output) error.");
            e.printStackTrace();
        }

        workDone.release();
    }
}
