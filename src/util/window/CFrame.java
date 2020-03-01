package util.window;

import core.Engine;
import core.HardwareManager;
import core.rendering.Window;
import core.resources.Asset;
import core.resources.Destroyable;
import core.result.VulkanException;
import core.synchronization.DependencyFence;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.vulkan.VK10;

/**
 * Generalized(JFrame like) window.
 *
 * @author Cezary Chodun
 * @since 23.01.2020
 */
public class CFrame {

    private static final Logger logger = Logger.getLogger(CFrame.class.getName());

    public static final String WINDOW_CFG_FILE = "window.cfg";
    private volatile Window window;
    private volatile long handle = VK10.VK_NULL_HANDLE;

    private synchronized void setWindow(Window window) {
        this.window = window;
        this.handle = window.getWindowID();
    }

    private class CreateCFrameTask implements Runnable {
        private Asset asset;
        private Engine engine;
        private Semaphore workDone;
        private boolean showWindow = true;
        private Destroyable destroyAtShutDown = null;

        public CreateCFrameTask(Engine engine, Asset asset) {
            this.asset = asset;
            this.engine = engine;
        }

        public CreateCFrameTask(
                Engine engine,
                Asset asset,
                boolean showWindow,
                Semaphore workDone,
                Destroyable destroyAtShutDown) {
            this.asset = asset;
            this.engine = engine;
            this.workDone = workDone;
            this.showWindow = showWindow;
            this.destroyAtShutDown = destroyAtShutDown;
        }

        private DependencyFence created;
        private DependencyFence loaded;
        private volatile Window window;

        @Override
        public void run() throws AssertionError {
            //            Window window = null;

            created = new DependencyFence(0);
            loaded = new DependencyFence(0);

            // Creating a window object.
            engine.addTask(
                    () -> {
                        try {
                            window = new Window(HardwareManager.getInstance());

                            System.err.println("Window created!");

                            created.release();
                        } catch (VulkanException e) {
                            logger.log(Level.FINE, "Failed to create window.", e);
                            e.printStackTrace();
                        }
                    });

            engine.addFastSMQ(
                    () -> {
                        // Setting the window data to the values from the file.
                        try {
                            WindowFactory.loadFromFileC(
                                    engine, window, asset.getConfigFile(WINDOW_CFG_FILE), loaded);

                            System.err.println("Window loaded!");
                        } catch (IOException | AssertionError e) {
                            logger.log(Level.FINE, "Failed to create window.", e);
                            e.printStackTrace();
                        }
                    },
                    created);

            engine.addConfigSMQ(
                    () -> {
                        if (window == null) throw new AssertionError("Window is null!");
                        // Creating a window task that will monitor the window events.
                        WindowTickTask wt = new WindowTickTask(window.getWindowID());

                        // Setting window close callback.
                        if (destroyAtShutDown != null)
                            wt.setCloseCall(
                                    new WindowShutDown(engine, window, wt, destroyAtShutDown));
                        else wt.setCloseCall(new WindowShutDown(engine, window, wt));

                        if (window != null) setWindow(window);

                        // Adding window tick task to the engine
                        engine.addTickTask(wt);

                        // Releasing the work done semaphore
                        if (workDone != null) workDone.release();

                        // Showing the window.
                        if (showWindow == true) window.setVisible(true);
                    },
                    loaded);
        }
    }

    /**
     * Creates a window using the given thread engine.getConfigPool(). It is not required to invoke
     * the constructor from the first thread, however the window won't be created until the engine
     * starts running.
     *
     * @param engine
     * @param asset The asset containing the window configuration file.
     * @param engine.getConfigPool() A thread engine.getConfigPool() that will handle IO work.
     */
    public CFrame(Engine engine, Asset asset) {
        engine.getConfigPool().execute(new CreateCFrameTask(engine, asset));
    }

    /**
     * Creates a window using the given thread engine.getConfigPool(). It is not required to invoke
     * the constructor from the first thread, however the window won't be created until the engine
     * starts running.
     *
     * @param engine
     * @param asset The asset containing the window configuration file.
     * @param engine.getConfigPool() A thread engine.getConfigPool() that will handle IO work.
     * @param workDone A semaphore that will indicate that the window creation process have
     *     finished.
     */
    public CFrame(Engine engine, Asset asset, Semaphore workDone) {
        engine.getConfigPool().execute(new CreateCFrameTask(engine, asset, false, workDone, null));
    }

    /**
     * Creates a window using the given thread engine.getConfigPool(). It is not required to invoke
     * the constructor from the first thread, however the window won't be created until the engine
     * starts running.
     *
     * @param engine
     * @param asset The asset containing the window configuration file.
     * @param engine.getConfigPool() A thread engine.getConfigPool() that will handle IO work.
     * @param showWindow Set to true if you want the window to be displayed as soon as created.
     * @param workDone A semaphore that will indicate that the window creation process have
     *     finished.
     */
    public CFrame(Engine engine, Asset asset, boolean showWindow, Semaphore workDone) {
        engine.getConfigPool()
                .execute(new CreateCFrameTask(engine, asset, showWindow, workDone, null));
    }

    /**
     * Creates a window using the given thread engine.getConfigPool(). It is not required to invoke
     * the constructor from the first thread, however the window won't be created until the engine
     * starts running.
     *
     * @param engine
     * @param asset The asset containing the window configuration file.
     * @param engine.getConfigPool() A thread engine.getConfigPool() that will handle IO work.
     * @param workDone A semaphore that will indicate that the window creation process have
     *     finished.
     * @param destroyAtShutDown A destroyable object that will be invoked after the window was shut
     *     down.
     */
    public CFrame(Engine engine, Asset asset, Semaphore workDone, Destroyable destroyAtShutDown) {
        engine.getConfigPool()
                .execute(new CreateCFrameTask(engine, asset, false, workDone, destroyAtShutDown));
    }

    /**
     * Creates a window using the given thread engine.getConfigPool(). It is not required to invoke
     * the constructor from the first thread, however the window won't be created until the engine
     * starts running.
     *
     * @param engine
     * @param asset The asset containing the window configuration file.
     * @param engine.getConfigPool() A thread engine.getConfigPool() that will handle IO work.
     * @param showWindow Set to true if you want the window to be displayed as soon as created.
     * @param workDone A semaphore that will indicate that the window creation process have
     *     finished.
     * @param destroyAtShutDown A destroyable object that will be invoked after the window was shut
     *     down.
     */
    public CFrame(
            Engine engine,
            Asset asset,
            boolean showWindow,
            Semaphore workDone,
            Destroyable destroyAtShutDown) {
        engine.getConfigPool()
                .execute(
                        new CreateCFrameTask(
                                engine, asset, showWindow, workDone, destroyAtShutDown));
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
