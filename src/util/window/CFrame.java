package util.window;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.vulkan.VK10;

import core.Engine;
import core.HardwareManager;
import core.rendering.Window;
import core.resources.Asset;
import core.resources.Destroyable;
import core.result.VulkanException;

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
		
		public CreateCFrameTask(Engine engine, Asset asset, boolean showWindow, Semaphore workDone, Destroyable destroyAtShutDown) {
			this.asset = asset;
			this.engine = engine;
			this.workDone = workDone;
			this.showWindow = showWindow;
			this.destroyAtShutDown = destroyAtShutDown;
		}
		
		
		@Override
		public void run() throws AssertionError {		
			Window window = null;
			
			try {
				// Creating a window object.
				window = new Window(HardwareManager.getInstance());
				// Setting the window data to the values from the file.
				WindowFactory.loadFromFile(window, asset.getConfigFile(WINDOW_CFG_FILE));
			} catch (VulkanException | IOException e) {
				logger.log(Level.FINE, "Failed to create window.");
				e.printStackTrace();
			}
			
			// Creating a window task that will monitor the window events.
			WindowTickTask wt = new WindowTickTask(window.getWindowID());
			
			// Setting window close callback.
			if (destroyAtShutDown != null)
				wt.setCloseCall(new WindowShutDown(engine, window, wt, destroyAtShutDown));
			else
				wt.setCloseCall(new WindowShutDown(engine, window, wt));
			
			// Adding window tick task to the engine
			engine.addTickTask(wt);
			
			// Releasing the work done semaphore
			if (workDone != null)
				workDone.release();
			
			// Showing the window.
			if (showWindow == true)
				window.setVisible(true);
			
			if (window != null)
				setWindow(window);
		}
	}
	
	/**
	 * In contrary to other constructors this one
	 * creates the window using the current thread.
	 * And it shouldn't be invoked from busy threads.
	 * 
	 * @param engine
	 * @param asset		The asset containing the window
	 * 		configuration file.
	 */
	public CFrame(Engine engine, Asset asset) {
		(new CreateCFrameTask(engine, asset)).run();
	}
	
	/**
	 * Creates a window using the given thread pool.
	 * It is not required to invoke the constructor
	 * from the first thread, however the window
	 * won't be created until the engine starts running.
	 * 
	 * @param engine
	 * @param asset		The asset containing the window
	 * 		configuration file.
	 * @param pool		A thread pool that will handle IO work.
	 */
	public CFrame(Engine engine, Asset asset, ThreadPoolExecutor pool) {
		pool.execute(new CreateCFrameTask(engine, asset));
	}
	
	/**
	 * Creates a window using the given thread pool.
	 * It is not required to invoke the constructor
	 * from the first thread, however the window
	 * won't be created until the engine starts running.
	 * 
	 * @param engine
	 * @param asset		The asset containing the window
	 * 		configuration file.
	 * @param pool		A thread pool that will handle IO work.
	 * @param workDone	A semaphore that will indicate that the window
	 * 		creation process have finished.
	 */
	public CFrame(Engine engine, Asset asset, ThreadPoolExecutor pool, Semaphore workDone) {
		pool.execute(new CreateCFrameTask(engine, asset, false, workDone, null));
	}
	
	/**
	 * Creates a window using the given thread pool.
	 * It is not required to invoke the constructor
	 * from the first thread, however the window
	 * won't be created until the engine starts running.
	 * 
	 * @param engine
	 * @param asset		The asset containing the window
	 * 		configuration file.
	 * @param pool		A thread pool that will handle IO work.
	 * @param showWindow	Set to true if you want the window to
	 * 		be displayed as soon as created.
	 * @param workDone	A semaphore that will indicate that the window
	 * 		creation process have finished.
	 */
	public CFrame(Engine engine, Asset asset, ThreadPoolExecutor pool, boolean showWindow, Semaphore workDone) {
		pool.execute(new CreateCFrameTask(engine, asset, showWindow, workDone, null));
	}
	
	/**
	 * Creates a window using the given thread pool.
	 * It is not required to invoke the constructor
	 * from the first thread, however the window
	 * won't be created until the engine starts running.
	 * 
	 * @param engine
	 * @param asset		The asset containing the window
	 * 		configuration file.
	 * @param pool		A thread pool that will handle IO work.
	 * @param workDone	A semaphore that will indicate that the window
	 * 		creation process have finished.
	 * @param destroyAtShutDown	A destroyable object that will be invoked 
	 * 		after the window was shut down.
	 */
	public CFrame(Engine engine, Asset asset, ThreadPoolExecutor pool, Semaphore workDone, Destroyable destroyAtShutDown) {
		pool.execute(new CreateCFrameTask(engine, asset, false, workDone, destroyAtShutDown));
	}
	
	/**
	 * Creates a window using the given thread pool.
	 * It is not required to invoke the constructor
	 * from the first thread, however the window
	 * won't be created until the engine starts running.
	 * 
	 * @param engine
	 * @param asset		The asset containing the window
	 * 		configuration file.
	 * @param pool		A thread pool that will handle IO work.
	 * @param showWindow	Set to true if you want the window to
	 * 		be displayed as soon as created.
	 * @param workDone	A semaphore that will indicate that the window
	 * 		creation process have finished.
	 * @param destroyAtShutDown	A destroyable object that will be invoked 
	 * 		after the window was shut down.
	 */
	public CFrame(Engine engine, Asset asset, ThreadPoolExecutor pool, boolean showWindow, Semaphore workDone, Destroyable destroyAtShutDown) {
		pool.execute(new CreateCFrameTask(engine, asset, showWindow, workDone, destroyAtShutDown));
	}
	
	/**
	 * Returns the window handle.
	 * Can be invoked on multiple threads.
	 * 
	 * @return the window handle or VK_NULL_HANDLE
	 * 		if it wasn't created yet.
	 */
	public long handle() {
		return handle;
	}
	
	/**
	 * <b>Note:</b> access to the window instance
	 * 	must be externally synchronized.
	 * 
	 * @return	the window corresponding to this CFrame.
	 */
	public Window getWindow() {
		return window;
	}
}
