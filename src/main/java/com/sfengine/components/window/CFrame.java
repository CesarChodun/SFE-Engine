package com.sfengine.components.window;

import com.sfengine.core.Application;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.HardwareManager;
import com.sfengine.core.rendering.Window;
import com.sfengine.core.resources.Asset;
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.synchronization.DependencyFence;
import com.sfengine.core.synchronization.Dependable;
import com.sfengine.core.synchronization.Dependency;
import com.sun.xml.internal.bind.v2.TODO;
import org.lwjgl.vulkan.VK10;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generalized(JFrame like) window.
 *
 * @author Cezary Chodun
 * @since 23.01.2020
 */
public class CFrame implements Dependable {

    private static final Logger logger = Logger.getLogger(CFrame.class.getName());

    private final Engine engine = EngineFactory.getEngine();

    public static final String WINDOW_CFG_FILE = "window.cfg";
    public static final String WINDOWS_CONFIG_DIRECTORY = "windows";

    private volatile Window window;
    private volatile WindowTickTask tickTask;
    private final CFrameDestroy destroyCallback = new CFrameDestroy();
    private volatile long handle = VK10.VK_NULL_HANDLE;
    private DependencyFence windowCreated = new DependencyFence();

    @Override
    public Dependency getDependency() {
        return windowCreated;
    }

    private class CFrameDestroy implements Destroyable {

        private volatile Destroyable call;

        @Override
        public void destroy() {
            call.destroy();
        }

        public void setDestroyable(Destroyable dest) {
            call = dest;
        }
    }

    private static Asset getCframeAsset(String name) {
        return Application.getConfigAssets().getSubAsset(WINDOWS_CONFIG_DIRECTORY).getSubAsset(name);
    }

    private void createWindow(String name) {

        DependencyFence created = new DependencyFence();
        DependencyFence loaded = new DependencyFence();

        engine.addTask(
                () -> {
                    try {
                        window = new Window(HardwareManager.getInstance());
                        handle = window.getWindowID();

                        logger.log(Level.INFO, "Window created.");

                        created.release();
                    } catch (VulkanException e) {
                        logger.log(Level.FINE, "Failed to create window.", e);
                        e.printStackTrace();
                    }
                }, HardwareManager.getDependency());

        engine.addFast(
                () -> {
                    // Setting the window data to the values from the file.
                    try {
                        WindowFactory.loadFromFileC(
                                engine, window, getCframeAsset(name).getConfigFile(WINDOW_CFG_FILE), loaded);

                        logger.log(Level.INFO, "Window data loaded.");
                    } catch (IOException | AssertionError e) {
                        logger.log(Level.FINE, "Failed to create window.", e);
                        e.printStackTrace();
                    }
                },
                created);

        engine.addConfig(
                () -> {
                    if (window == null) {
                        throw new AssertionError("Window is null!");
                    }
                    // Creating a window task that will monitor the window events.
                    tickTask = new WindowTickTask(window.getWindowID());

                    // Setting window close callback.
                    tickTask.setCloseCall(new WindowShutDown(engine, window, tickTask, destroyCallback));

                    // Adding window tick task to the engine
                    engine.addTickTask(tickTask);

                    // Releasing the work done semaphore
                    windowCreated.release();

                    //TODO: remove if possible
                    window.setVisible(true);
                },
                loaded);
    }

    /**
     * Creates a new frame with a given name(the name can be different than the window's name).
     * It's thread save and doesn't need any synchronization.
     *
     * @param name  the name of the frame(not window).
     */
    public CFrame(String name) {
        createWindow(name);
    }

    /**
     * Sets the callback for closing the window.
     *
     * @param callback the callback.
     */
    public void setCloseCallback(Destroyable callback) {
        destroyCallback.setDestroyable(callback);
    }

    /**
     * Returns the window handle. Can be invoked on multiple threads.
     *
     * @return the window handle or VK_NULL_HANDLE if it wasn't created yet.
     */
    public long handle() {
        return handle;
    }

    /**
     * <b>Note:</b> access to the window instance must be externally synchronized.
     *
     * @return the window corresponding to this CFrame.
     */
    public Window getWindow() {
        return window;
    }
}
