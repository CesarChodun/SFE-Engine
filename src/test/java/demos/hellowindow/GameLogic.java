package demos.hellowindow;

import com.sfengine.components.window.CFrame;
import com.sfengine.core.Application;
import com.sfengine.core.HardwareManager;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.engine.EngineTask;
import java.util.concurrent.Semaphore;

public class GameLogic implements EngineTask {

    static final String CONFIG_FILE = "demos/hellowindow";

    private Engine engine = EngineFactory.getEngine();

    public GameLogic() {}

    @Override
    public void run() throws AssertionError {
        Application.init(CONFIG_FILE);
        HardwareManager.init();

        CFrame frame = new CFrame("MyFrame");

        engine.addConfig(() -> {
            frame.getWindow().setVisible(true);
        }, frame.getDependency());
    }
}
