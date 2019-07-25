package core.hardware;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWGammaRamp;

public class Monitor {

	private long handle;
	
	private String name;
	private int x, y;
	private int width, height;
	private float gamma;
	
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
		
		this.gamma = 1.0f;
		glfwSetGamma(handle, gamma);
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

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}