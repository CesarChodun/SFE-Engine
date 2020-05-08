package demos.helloCube;

import com.sfengine.components.resources.MemoryBin;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.Application;
import com.sfengine.core.Engine;
import com.sfengine.core.EngineFactory;
import com.sfengine.core.resources.Asset;
import com.sfengine.core.synchronization.DependencyFence;
import demos.helloCube.rendering.*;

/**
 * Creates a window and initializes the rendering layer for it.
 *
 * @author Cezary Chodun
 * @since 10.01.2020
 */
public class WindowManager implements Runnable {

    private final Engine engine = EngineFactory.getEngine();
    private DependencyFence wait;
    private CFrame frame;

    public WindowManager(DependencyFence wait) {
        this.wait = wait;
    }

    @Override
    public void run() {
        final MemoryBin toDestroy = new MemoryBin();
        DependencyFence windowCreated = new DependencyFence(0);

        System.out.println("WindowManager work submited!");

        // Submits window creation task to the engine configuration queue.
        engine.addConfig(
                () -> {
                    // Obtaining the asset folder for the window.
                    Asset windowAsset = Application.getConfigAssets().getSubAsset("window");

                    // Creating a task that will create the window.
                    frame = new CFrame(windowAsset, false, windowCreated, toDestroy);
                },
                wait);

        System.out.println("CFrame work submited!");

        // Submits rendering task to the engine configuration queue.
        engine.addConfig(
                () -> {
                    // Creating the rendering task.
                    InitializeRendering rendTask =
                            new InitializeRendering(frame.getWindow());
                    toDestroy.add(rendTask);
                    engine.addTask(rendTask);
                },
                windowCreated);

        System.out.println("Window manager done!");
    }
}
