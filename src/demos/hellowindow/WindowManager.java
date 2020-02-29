package demos.hellowindow;

import static org.lwjgl.glfw.GLFW.*;

import java.io.IOException;

import core.Engine;
import core.EngineTask;
import core.HardwareManager;
import core.rendering.Window;
import core.resources.Asset;
import core.result.VulkanException;
import util.window.WindowFactory;

public class WindowManager implements EngineTask {

    /**
     * A class that will perform the window(and engine)
     * shut down.
     */
    private static class WindowShutDown implements WindowTask.WindowCloseCallback {

        private Engine engine;
        private Window window;
        private EngineTask windowTask;
        
        public WindowShutDown(Engine engine, Window window, WindowTask windowTask) {
            this.engine = engine;
            this.window = window;
            this.windowTask = windowTask;
        }
        
        @Override
        public void close(long windowID) {
            // Hides the window.
            window.setVisible(false);
            // Removes the tick task from the engine.
            engine.removeTickTask(windowTask);
            // Destroys the window.
            window.destroyWindow();
            // Shut down the engine.
            engine.stop();
        }
        
    }
    
    /**
     * A class that will monitor if the window should close,
     * and poll the GLFW events.
     */
    private static class WindowTask implements EngineTask{
        
        public static interface WindowCloseCallback {
            /**
             * Method invoked when the window is closing.
             * 
             * @param windowID        the ID of the closing window.
             */
            public void close(long windowID);
        }

        private long windowID;
        private WindowCloseCallback closeCall;
        
        public WindowTask(long windowID) {
            this.windowID = windowID;
        }
        
        @Override
        public void run() throws AssertionError {
            // Pooling the GLFW events.
            glfwPollEvents();
            
            // Checking if the window should close.
            if (glfwWindowShouldClose(windowID))
                closeWindow();
        }

        private void closeWindow() {
            if (closeCall != null)
                closeCall.close(windowID);
        }

        public void setCloseCall(WindowCloseCallback closeCall) {
            this.closeCall = closeCall;
        }
    }
    
    /**
     * A class that will create the window 
     * and add the window per tick task to the engine.
     */
    private static class CreateWindowTask implements EngineTask {

        private Window window;
        private Asset asset;
        private Engine engine;
        
        public CreateWindowTask(Engine engine, Asset asset) {
            this.asset = asset;
            this.engine = engine;
        }
        
        
        @Override
        public void run() throws AssertionError {            
            try {
                // Creating a window object.
                window = new Window(HardwareManager.getInstance());
                // Setting the window data to the values from the file.
                WindowFactory.loadFromFile(window, asset.getConfigFile("window.cfg"));
            } catch (VulkanException | IOException e) {
                System.err.println("Failed to create window.");
                e.printStackTrace();
            }
            
            // Creating a window task that will monitor the window events.
            WindowTask wt = new WindowTask(window.getWindowID());
            wt.setCloseCall(new WindowShutDown(engine, window, wt));
            engine.addTickTask(wt);
            
            // Showing the window.
            window.setVisible(true);
        }
    }
    
    private Engine engine;
    private Asset asset;
    
    public WindowManager(Engine engine, Asset asset) {
        this.engine = engine;
        this.asset = asset;
    }
    
    @Override
    public void run() {
        // Obtaining the asset folder for the window.
        Asset windowAsset = asset.getSubAsset("window");
        
        // Creating a task that will create the window.
        CreateWindowTask cwt = new CreateWindowTask(engine, windowAsset);
        engine.addTask(cwt);
    }

}
