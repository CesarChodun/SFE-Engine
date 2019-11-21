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

public class WindowManager implements EngineTask{

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
			engine.removeTickTask(windowTask);
			window.setVisible(false);
			window.destroyWindow();
			engine.stop();
		}
		
	}
	
	private static class WindowTask implements EngineTask{
		
		public static interface WindowCloseCallback {
			/**
			 * Method invoked when the window is closing.
			 * 
			 * @param windowID		the ID of the closing window.
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
			glfwPollEvents();
			if (glfwWindowShouldClose(windowID))
				closeWindow();
		}

		private void closeWindow() {
			if (closeCall != null)
				closeCall.close(windowID);
		}

		public WindowCloseCallback getCloseCall() {
			return closeCall;
		}

		public void setCloseCall(WindowCloseCallback closeCall) {
			this.closeCall = closeCall;
		}
	}
	
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
				window = new Window(HardwareManager.getInstance());
				WindowFactory.loadFromFile(window, asset.getConfigFile("window.cfg"));
			} catch (VulkanException | IOException e) {
				System.err.println("Failed to create window.");
				e.printStackTrace();
			}
			
			window.setVisible(true);

			WindowTask wt = new WindowTask(window.getWindowID());
			wt.setCloseCall(new WindowShutDown(engine, window, wt));
			engine.addTickTask(wt);
		}


		public Window getWindow() {
			return window;
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
		
		Asset windowAsset = asset.getSubAsset("window");
		CreateWindowTask cwt = new CreateWindowTask(engine, windowAsset);
		engine.addTask(cwt);
	}

}
