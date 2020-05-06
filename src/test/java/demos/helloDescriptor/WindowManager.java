package test.java.demos.helloDescriptor;

import main.java.components.resources.MemoryBin;
import main.java.components.window.CFrame;
import main.java.core.Application;
import main.java.core.Engine;
import main.java.core.resources.Asset;
import main.java.core.synchronization.DependencyFence;
import test.java.demos.helloDescriptor.rendering.*;

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
                    toDestroy.add(rendTask);
                    engine.addTask(rendTask);
                },
                windowCreated);

        System.err.println("Window manager done!");
    }
}