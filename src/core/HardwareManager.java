package core;

import static org.lwjgl.vulkan.VK10.*;
import static core.result.VulkanResult.*;
import static org.lwjgl.system.MemoryUtil.*;
import static core.rendering.RenderUtil.*;
import static core.rendering.RenderAssetUtil.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;

import core.hardware.Monitor;
import core.resources.Asset;
import core.result.GLFWError;
import core.result.VulkanException;

public class HardwareManager {
	
	private static final String CLASS_NAME = HardwareManager.class.getName();
	protected static final Logger logger = Logger.getLogger(CLASS_NAME);
	
	private static String[] requiredExtensions;
	
	private static VkInstance instance;
	private static VkPhysicalDevice[] physicalDevices;
	
	private static Monitor[] monitors;

	/**
	 * 
	 * 
	 * @throws VulkanException
	 * @throws IOException 
	 */
	public static void init(VkApplicationInfo appInfo, Asset config) throws VulkanException, IOException {

		initGLFW();
		instance = createInstanceFromAsset(appInfo, config, requiredExtensions);
		physicalDevices = enumeratePhysicalDevices(instance);
	}
	
	private static void initGLFW() {
		if (!glfwInit())
			throw new GLFWError("Failed to initialize GLFW!");
		
		PointerBuffer pRequiredExtensions = glfwGetRequiredInstanceExtensions();
		int requiredInstanceExtensionsCount = pRequiredExtensions.capacity();
		requiredExtensions = new String[requiredInstanceExtensionsCount];
		for (int i = 0; i < requiredInstanceExtensionsCount; i++) {
			requiredExtensions[i] = memUTF8(pRequiredExtensions.get(i));
			System.err.println(requiredExtensions[i]);
		}
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		
		PointerBuffer pMonitors = glfwGetMonitors();
		int monitorCount = pMonitors.capacity();
		monitors = new Monitor[monitorCount];
		for (int i = 0; i < monitorCount; i++) 
			monitors[i] = new Monitor(pMonitors.get(i));
	}
	
	public static VkPhysicalDevice[] getPhysicalDevices() {
		return physicalDevices;
	}
	
	public Monitor getPrimaryMonitor() {
		return monitors[0];
	}

	public static Monitor[] getMonitors() {
		return monitors;
	}

	public static VkInstance getInstance() {
		return instance;
	}
	
}
