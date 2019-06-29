package core;

import static org.lwjgl.vulkan.VK10.*;
import static core.result.VulkanResult.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkLayerProperties;

import core.result.VulkanException;

public class HardwareManager {
	
	private static final String CLASS_NAME = HardwareManager.class.getName();
	protected static final Logger logger = Logger.getLogger(CLASS_NAME);
	
	private int validationLayersCount;
	private VkLayerProperties.Buffer validationLayers;
	private VkInstance instance;

	/**
	 * 
	 * 
	 * @throws VulkanException
	 */
	public HardwareManager() throws VulkanException {
		if (Application.DEBUG)
			logger.entering(CLASS_NAME, "HardwareManager");
		
		logger.setLevel(Level.ALL);
		logger.setUseParentHandlers(true);
		
		listValidationLayers();
		

		if (Application.DEBUG)
			logger.exiting(CLASS_NAME, "HardwareManager");
	}
	
	
	/**
	 * Lists available Vulkan validation layers.
	 */
	private void listValidationLayers() throws VulkanException {
		IntBuffer propsCount = memAllocInt(1);
		int err = vkEnumerateInstanceLayerProperties(propsCount, null);
		validate(err, "Failed to obtain the number of vulkan validation layers!");
		
		validationLayersCount = propsCount.get(0);
		
		validationLayers = VkLayerProperties.calloc(validationLayersCount);
		err = vkEnumerateInstanceLayerProperties(propsCount, validationLayers);
		validate(err, "Failed to enumerate vulkan validation layers!");
		
		// Logging information
		if (Application.DEBUG) {
			StringBuilder sb = new StringBuilder();
			
			sb.append("Available vulkan validation layers(" + validationLayersCount + "): \n");
			
			for (int i = 0; i < validationLayersCount; i++) {
				sb.append(validationLayers.get(i).layerNameString());
				if(i != validationLayersCount - 1)
					sb.append(", ");
			}

			logger.log(Level.CONFIG, sb.toString());
		}
		
		memFree(propsCount);
	}
	
	
	
	private void listDevices() throws VulkanException {
		
		IntBuffer deviceCount = memAllocInt(1);
		int err = vkEnumeratePhysicalDevices(instance, deviceCount, null);
		validate(err, "Failed to enumerate Vulkan devices!");
		int devices = deviceCount.get(0);
		
		
		
		
	}
}
