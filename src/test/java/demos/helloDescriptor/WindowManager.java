package demos.helloDescriptor;

import com.sfengine.components.resources.MemoryBin;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.Application;
import com.sfengine.core.Engine;
import com.sfengine.core.resources.Asset;
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.synchronization.DependencyFence;

import demos.helloDescriptor.rendering.*;

/**
 * Creates a window and initializes the rendering layer for it.
 *
 * @author Cezary Chodun
 * @since 10.01.2020
 */
public class WindowManager implements Runnable {

    private Engine engine;
    private DependencyFence wait;
    private CFrame frame;

    public WindowManager(Engine engine, DependencyFence wait) {
        this.engine = engine;
        this.wait = wait;
    }

    @Override
    public void run() {
        final MemoryBin toDestroy = new MemoryBin();
        DependencyFence windowCreated = new DependencyFence(0);

        System.err.println("WindowManager work submited!");

        engine.addConfigSMQ(
                () -> {
                    // Obtaining the asset folder for the window.
                    Asset windowAsset = Application.getConfigAssets().getSubAsset("window");

                    // Creating a task that will create the window.
                    frame = new CFrame(engine, windowAsset, false, windowCreated, toDestroy);
                },
                wait);

        System.err.println("CFrame work submited!");

        engine.addConfigSMQ(
                () -> {
                    // Creating the rendering task.
                    InitializeRendering rendTask =
                            new InitializeRendering(engine, frame.getWindow());
                    engine.addTask(rendTask);
                    toDestroy.add((Destroyable) rendTask);
                },
                windowCreated);

        System.err.println("Window manager done!");
    }
}
