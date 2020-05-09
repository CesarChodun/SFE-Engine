package demos.helloDescriptor;

import com.sfengine.components.resources.MemoryBin;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.Application;
import com.sfengine.core.engine.Engine;
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

        frame = new CFrame("MyWindow");
        frame.setCloseCallback(toDestroy);

        System.err.println("CFrame work submited!");

        engine.addConfig(
                () -> {
                    // Creating the rendering task.
                    InitializeRendering rendTask =
                            new InitializeRendering(engine, frame.getWindow());
                    engine.addTask(rendTask);
                    toDestroy.add((Destroyable) rendTask);
                },
                frame.getDependency());

        System.err.println("Window manager done!");
    }
}
