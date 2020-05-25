package demos.helloTriangle;

import com.sfengine.components.contexts.DefaultContexts;
import com.sfengine.components.resources.MemoryBin;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.Application;
import com.sfengine.core.HardwareManager;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.components.util.DefaultResourceConverter;

import java.util.List;

public class GameLogic implements Runnable {

    private static final String CONFIG_FILE = "demos/hellotriangle";
    private Engine engine = EngineFactory.getEngine();
    private MemoryBin toDestroy = new MemoryBin();

    @Override
    public void run() throws AssertionError {

        // Asset converter
        DefaultResourceConverter converter = new DefaultResourceConverter();

        // Performing asset conversion
        converter.runConversion();

        // Awaiting for the asset conversion
        converter.await();

        // Application and hardware initialization.
        Application.init(CONFIG_FILE);
        HardwareManager.init();

        // Creating the frame(window without graphics in it).
        CFrame frame = new CFrame("MyFrame");
        frame.setCloseCallback(toDestroy);

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
