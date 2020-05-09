package demos.helloTriangle;

import com.sfengine.components.resources.MemoryBin;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.Application;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.resources.Asset;
import java.util.concurrent.Semaphore;

public class WindowManager implements Runnable {

    private Engine engine;
    private Semaphore wait;

    public WindowManager(Engine engine, Semaphore wait) {
        this.engine = engine;
        this.wait = wait;
    }

    @Override
    public void run() {
        MemoryBin toDestroy = new MemoryBin();

        // Creating the frame(window without graphics in it).
        CFrame frame = new CFrame("MyFrame");

        engine.addConfig(
                () -> {
                    // Creating the rendering task.
                    InitializeRendering rendTask = new InitializeRendering(engine, frame.getWindow());
                    toDestroy.add(rendTask);
                    engine.addTask(rendTask);
                    frame.setCloseCallback(toDestroy);
                }, frame.getDependency());
    }
}
