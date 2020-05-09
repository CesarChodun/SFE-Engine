package demos.hardwareinit;

import com.sfengine.core.Application;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.engine.EngineTask;
import com.sfengine.core.HardwareManager;

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
        Application.init(CONFIG_FILE);
        HardwareManager.init();
        EngineFactory.getEngine().addConfig(() -> {
            workDone.release();
        }, HardwareManager.getDependency());
    }
}
