package core.hardware;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWGammaRamp;
import org.lwjgl.glfw.GLFWVidMode;

public class Monitor {

	/** The unique id of the monitor. */
	private long handle;
	
	/** Name of the monitor. */
	private String name;
	/** Work area x and y position. */
	private int x, y;
	/** Work area width and height. */
	private int width, height;
	/** Monitor's gamma value. */
	private float gamma;
	
	/** Monitor's video mode. */
	private GLFWVidMode videoMode;
	
	/** Creates a new monitor object 
	 * from the GLFW monitor handle.
	 * 
	 * @param handle
	 */
	public Monitor(long handle) {
		this.handle = handle;
		obtainMonitorData();
	}
	
	private void obtainMonitorData() {
		this.name = glfwGetMonitorName(handle);
		
		int[] xpos = new int[1], ypos = new int[1], width = new int[1], height = new int[1]; 
		glfwGetMonitorWorkarea(handle, xpos, ypos, width, height);
		this.x = xpos[0];
		this.y = ypos[0];
		this.width = width[0];
		this.height = height[0];
		
		videoMode = glfwGetVideoMode(handle);
		
		this.gamma = 1.0f;
		glfwSetGamma(handle, gamma);
	}
	
	public int getWidth() {
		return videoMode.width();
	}
	
	public int getHeight() {
		return videoMode.height();
	}
	
	public int getRefreshRate() {
		return videoMode.refreshRate();
	}

	public GLFWGammaRamp getGammaRamp() {
		return glfwGetGammaRamp(handle);
	}

	public void setGammaRamp(GLFWGammaRamp gammaRamp) {
		glfwSetGammaRamp(handle, gammaRamp);
	}

	public float getGamma() {
		return gamma;
	}

	public void setGamma(float gamma) {
		this.gamma = gamma;
		glfwSetGamma(handle, gamma);
	}

	public long getHandle() {
		return handle;
	}

	public String getName() {
		return name;
	}

	public int getWorkAreaX() {
		return x;
	}

	public int getWorkAreaY() {
		return y;
	}

	public int getWorkAreaWidth() {
		return width;
	}

	public int getWorkAreaHeight() {
		return height;
	}
}
