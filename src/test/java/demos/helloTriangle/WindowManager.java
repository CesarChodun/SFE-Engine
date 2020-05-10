package demos.helloTriangle;

import com.sfengine.components.contexts.DefaultContexts;
import com.sfengine.components.resources.MemoryBin;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.Application;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.resources.Asset;
import com.sfengine.core.synchronization.Dependency;

import java.util.List;
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

        List<Dependency> deps = DefaultContexts.getDependencies();
        deps.add(frame.getDependency());

        Dependency[] depsArr = new Dependency[deps.size()];
        for (int i = 0; i < deps.size(); i++)
            depsArr[i] = deps.get(i);

        engine.addConfig(
                () -> {
                    // Creating the rendering task.
                    InitializeRendering rendTask = new InitializeRendering(frame.getWindow());
                    toDestroy.add(rendTask);
                    engine.addTask(rendTask);
                    frame.setCloseCallback(toDestroy);
                }, depsArr);

    }
}
