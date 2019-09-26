package game.rendering;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import core.EngineTask;
import core.rendering.Window;

public class WindowTask implements EngineTask{
	
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
