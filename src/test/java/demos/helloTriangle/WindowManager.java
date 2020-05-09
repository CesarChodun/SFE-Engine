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

        Semaphore windowCreated = new Semaphore(0);
        try {
            wait.acquire();
        } catch (InterruptedException e) {
            System.out.println("Window wait for semaphore interupted.");
            e.printStackTrace();
        }

        // Obtaining the asset folder for the window.
        Asset windowAsset = Application.getConfigAssets().getSubAsset("window");

        // Creating the frame(window without graphics in it).
        CFrame frame = new CFrame("MyFrame");

        try {
            // Waiting for the window to be created.
            windowCreated.acquire();
        } catch (InterruptedException e) {
            System.out.println("Wait for window creation interupted.");
            e.printStackTrace();
        }

        // Creating the rendering task.
        InitializeRendering rendTask = new InitializeRendering(engine, frame.getWindow());
        toDestroy.add(rendTask);
        engine.addTask(rendTask);
    }
}
