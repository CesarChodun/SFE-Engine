package core;

import static org.lwjgl.vulkan.VK10.*;
import static core.result.VulkanResult.*;
import static org.lwjgl.system.MemoryUtil.*;
import static core.rendering.RenderUtil.*;
import static core.rendering.RenderAssetUtil.*;

import java.io.FileNotFoundException;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;

import core.resources.Asset;
import core.result.VulkanException;

public class HardwareManager {
	
	private static final String CLASS_NAME = HardwareManager.class.getName();
	protected static final Logger logger = Logger.getLogger(CLASS_NAME);
	
	private static int validationLayersCount;
	private static VkInstance instance;
	private static VkPhysicalDevice[] physicalDevices;

	/**
	 * 
	 * 
	 * @throws VulkanException
	 * @throws FileNotFoundException 
	 */
	public static void init(VkApplicationInfo appInfo, Asset config) throws VulkanException, FileNotFoundException {
		instance = createInstanceFromAsset(appInfo, config);
		physicalDevices = enumeratePhysicalDevices(instance);
	}
	
}
