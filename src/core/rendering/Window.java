package core.rendering;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkInstance;

import core.hardware.Monitor;
import core.result.VulkanException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static core.result.VulkanResult.*;

/**
 * <h5>Description:</h5>
 * <p>Class for creating window using <b>GLFW</b>.</p>
 * @author Cezary Choduń
 *
 */
public class Window {
	
	public static final int DEFAULT_WIDTH = 800, DEFAULT_HEIGHT = 600, DEFAULT_X = 0, DEFAULT_Y = 0;
	public static final String DEFAULT_NAME = "Window";

	private int width = DEFAULT_WIDTH, height = DEFAULT_HEIGHT, x = DEFAULT_X, y = DEFAULT_Y;
	private String name = DEFAULT_NAME;
	private long windowID;
	private long surface;
	
	/**
	 * 	<h5>Description:</h5>
	 * 	<p>Create window with default name and dimensions.</p>
	 * @throws VulkanException 
	 * 
	 */
	public Window(VkInstance instance) throws VulkanException {
		createWindow(instance, NULL);
	}
	
	/**
	 * 	<h5>Description:</h5>
	 * 	<p>Create window with default dimensions and custom name.</p>
	 * 	@param name	- Name of the window.
	 * @throws VulkanException 
	 */
	public Window(VkInstance instance, String name) throws VulkanException {
		this.name = name;
		createWindow(instance, NULL);
	}
	
	/**
	 * 	<h5>Description:</h5>
	 * 	<p>Create window with custom name and dimensions.</p>
	 * 
	 * 	@param width	- Width of the created window.
	 * 	@param height	- Height of the created window. 
	 * 	@param name		- Name of the window.
	 * @throws VulkanException 
	 */
	public Window(VkInstance instance, String name, int width, int height) throws VulkanException {
		this.height = height;
		this.width = width;
		
		this.name = name;
		createWindow(instance, NULL);
	}
	
	/**
	 * 	<h5>Description:</h5>
	 * 	<p>Create window with custom name and dimensions.</p>
	 * 
	 * 	@param x  		- Horizontal distance between top left monitor corner and top left window corner.
	 * 	@param y 		- Vertical distance between top left monitor corner and top left window corner.
	 * 	@param width	- Width of the created window.
	 * 	@param height	- Height of the created window. 
	 * 	@param name		- Name of the window.
	 * @throws VulkanException 
	 */
	public Window(VkInstance instance, String name, int x, int y, int width, int height) throws VulkanException {		
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
		
		this.name = name;
		createWindow(instance, NULL);
	}
	
	/**
	 * 	<h5>Description:</h5>
	 * 	<p>Create window with custom name and dimensions.</p>
	 * 
	 * 	@param x  		- Horizontal distance between top left monitor corner and top left window corner.
	 * 	@param y 		- Vertical distance between top left monitor corner and top left window corner.
	 * 	@param width	- Width of the created window.
	 * 	@param height	- Height of the created window. 
	 * 	@param name		- Name of the window.
	 *  @param fullScreen - Creates full screen window(if true).
	 * @throws VulkanException 
	 */
	public Window(VkInstance instance, String name, int x, int y, int width, int height, boolean fullScreen) throws VulkanException {		
		this.x = x;
		this.y = y;
		this.height = height;
		this.width = width;
		
		this.name = name;
		
		if(fullScreen == false)
			createWindow(instance, NULL);
		else
			createWindow(instance, glfwGetPrimaryMonitor());
	}
	
	public long getSurface() {
		return surface;
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Creates a window.</p>
	 * @throws VulkanException		when the window creation fails.
	 */
	private void createWindow(VkInstance instance, long monitor) throws VulkanException {
		windowID = glfwCreateWindow(width, height, name, monitor, NULL);
		setPos(x, y);
		
		LongBuffer pSurface = memAllocLong(1);
		int err = glfwCreateWindowSurface(instance, windowID, null, pSurface);
		validate(err, "Failed to create surface!");
		surface = pSurface.get(0);
		memFree(pSurface);
	}
	
	public void destroyWindow() {
		glfwDestroyWindow(windowID);
	}
	
	public void setVisible(boolean val) {
		if(val)
			glfwShowWindow(windowID);
		else
			glfwHideWindow(windowID);
	}
	public boolean isVsible() {
		return ((glfwGetWindowAttrib(windowID, GLFW_VISIBLE) == 0)? true : false);
	}
	public void setPos(int x, int y) {
		this.x = x; this.y = y;
		glfwSetWindowPos(windowID, x, y);
	}
	public void setSize(int width, int height) {
		this.width = width; this.height = height;
		glfwSetWindowSize(windowID, width, height);
	}
	public void setBounds(int x, int y, int width, int height) {
		setPos(x, y);
		setSize(width, height);
	}
	public void setWindowHint(int hint, int val) {
		glfwWindowHint(hint, val);
	}
	public void fullScreen(boolean full) {
		if(full == true)
			glfwSetWindowMonitor(windowID, glfwGetPrimaryMonitor(), x, y, width, height, GLFW_DONT_CARE);
		else
			glfwSetWindowMonitor(windowID, NULL, x, y, width, height, GLFW_DONT_CARE);
	}
	
	public long getWindowID() {
		return windowID;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public String getName() {
		return name;
	}
}
