package demos.helloDescriptor.window;

import core.Engine;
import core.EngineTask;
import core.rendering.Window;
import core.resources.Destroyable;

/**
 * Callback that will close all initialized window 
 * resources on the window shutdown.
 * 
 * @author Cezary Chodun
 * @since 10.01.2020
 */
public class WindowShutDown implements WindowTickTask.WindowCloseCallback {

	private Engine engine;
	private Window window;
	private EngineTask windowTickTask;
	private Destroyable toDestroy = null;
	
	public WindowShutDown(Engine engine, Window window, EngineTask windowTickTask) {
		this.engine = engine;
		this.window = window;
		this.windowTickTask = windowTickTask;
	}
	
	public WindowShutDown(Engine engine, Window window, EngineTask windowTickTask, Destroyable toDestroy) {
		this.engine = engine;
		this.window = window;
		this.windowTickTask = windowTickTask;
		this.toDestroy = toDestroy;
	}
	
	@Override
	public void close(long windowID) {
		// Hides the window.
		window.setVisible(false);

		// Destroying other objects.
		if (toDestroy != null)
			toDestroy.destroy();
		
		// Removes the tick task from the engine.
		engine.removeTickTask(windowTickTask);
		// Destroys the window.
		window.destroyWindow();
		
		// Shut down the engine.
		engine.stop();
	}
	
}