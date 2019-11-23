package demos.helloDescriptor.window;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import core.EngineTask;

public class WindowTickTask implements EngineTask{
	
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
	
	public WindowTickTask(long windowID) {
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
