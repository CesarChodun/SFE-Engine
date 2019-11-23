package demos.helloDescriptor.window;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import core.Engine;
import core.EngineTask;
import core.HardwareManager;
import core.rendering.Window;
import core.resources.Asset;
import core.resources.Destroyable;
import core.result.VulkanException;
import util.window.WindowFactory;

public class TriangleWindow implements EngineTask {

	private Window window;
	private Asset asset;
	private Engine engine;
	private Semaphore workDone;
	private boolean showWindow = true;
	private Destroyable destroyAtShutDown = null;
	
	public TriangleWindow(Engine engine, Asset asset) {
		this.asset = asset;
		this.engine = engine;
	}
	
	public TriangleWindow(Engine engine, Asset asset, boolean showWindow, Semaphore workDone, Destroyable destroyAtShutDown) {
		this.asset = asset;
		this.engine = engine;
		this.workDone = workDone;
		this.showWindow = showWindow;
		this.destroyAtShutDown = destroyAtShutDown;
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
	}

	public Window getWindow() {
		return window;
	}
}