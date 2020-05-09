package demos.helloCube;

import com.sfengine.components.resources.MemoryBin;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.Application;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.HardwareManager;
import com.sfengine.core.resources.Asset;
import demos.helloCube.rendering.*;

/**
 * Creates a window and initializes the rendering layer for it.
 *
 * @author Cezary Chodun
 * @since 10.01.2020
 */
public class WindowManager implements Runnable {

    private final Engine engine = EngineFactory.getEngine();
    private CFrame frame;

    public WindowManager() {

    }

    @Override
    public void run() {
        final MemoryBin toDestroy = new MemoryBin();

        frame = new CFrame("MyFrame");
        frame.setCloseCallback(toDestroy);

        // Submits rendering task to the engine configuration queue.
        engine.addConfig(
                () -> {
                    // Creating the rendering task.
                    InitializeRendering rendTask =
                            new InitializeRendering(frame.getWindow());
                    toDestroy.add(rendTask);
                    engine.addTask(rendTask);
                },
                frame.getDependency());

        System.out.println("Window manager done!");
    }
}
