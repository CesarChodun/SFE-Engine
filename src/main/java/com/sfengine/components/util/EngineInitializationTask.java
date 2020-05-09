package com.sfengine.components.util;

import com.sfengine.core.Application;
import com.sfengine.core.engine.EngineTask;
import com.sfengine.core.HardwareManager;
import com.sfengine.core.result.VulkanException;
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
        Application.init(configFile);
        HardwareManager.init();
        workDone.release();
    }
}
