package core.rendering;

import static core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import static org.lwjgl.vulkan.VK10.*;

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import core.result.VulkanException;
import core.result.VulkanResult;

public class RenderUtil {
	
	public static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	/**
	  *	<h5>Description:</h5>
	  * <p>
	  * 	Obtains available surface extensions.
	  * </p>
	  * @param physicalDevice	- Physical device to obtain properties from.
	  * @param surface			- Surface handle.
	  * @return					- Buffer with <b><i><code>VkSurfaceFormatKHR</code></i></b>.
	 * @throws VulkanException 
	  * @see 					{@link VkSurfaceFormatKHR}
	  */
	public static VkSurfaceFormatKHR.Buffer listSurfaceFormats(VkPhysicalDevice physicalDevice, long surface) throws VulkanException{
		 IntBuffer pCount = memAllocInt(1);
		 int err = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pCount, null);
		 validate(err, "Failed to enumerate surface formats!");
		 int count = pCount.get(0);
		 
		 VkSurfaceFormatKHR.Buffer out = VkSurfaceFormatKHR.calloc(count);
		 err = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pCount, out);
		 memFree(pCount);
		 validate(err, "Failed to obtain surface formats!");
		 
		 return out;
	 }
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 	Obtains desired color format if possible.
	 * </p>
	 * 
	 * @param physicalDevice		- Physical device to obtain informations from.
	 * @param surface				- Surface handle.
	 * @param first					- Index to start iterating from. Most likely <b>0</b>.
	 * @param desiredFormat			- Most suitable color format.
	 * @param desiredColorSpace		- Most suitable color space.
	 * @return						- Returns <b><i><code>ColorFormatAndSpace</code></i></b> that support <b>desiredColorFormat</b>
	 * 								if possible. Otherwise <b>null</b>.
	 * @throws VulkanException 
	 */
	public static ColorFormatAndSpace getNextColorFormatAndSpace(int first, VkPhysicalDevice physicalDevice, long surface, int desiredFormat, int desiredColorSpace) throws VulkanException {
		VkSurfaceFormatKHR.Buffer formats = listSurfaceFormats(physicalDevice, surface);
		ColorFormatAndSpace out = null;
		int size = formats.remaining();
		
		if(size == 1 && formats.get(0).format() == VK_FORMAT_UNDEFINED)
			return new ColorFormatAndSpace(desiredFormat, desiredColorSpace == -1 ? formats.get(0).colorSpace() : desiredColorSpace);
		
		for(int i = first; i < size; i++)
			if(formats.get(i).format() == desiredFormat || desiredFormat == -1) {
				out = new ColorFormatAndSpace(formats.get(i).format(), formats.get(i).colorSpace());
				
				if(formats.get(i).colorSpace() == desiredColorSpace || desiredColorSpace == -1)
					break;
			}
		
		formats.free();
		
		return out;
	}
	
	/**
     * <h5>Description:</h5>
	 * <p>
	 * 		Gets current surface present modes.
	 * </p>
     * @param physicalDevice
     * @param surface
     * @return
	 * @throws VulkanException 
     */
    public static IntBuffer getSurfacePresentModes(VkPhysicalDevice physicalDevice, long surface) throws VulkanException {
    	IntBuffer pCount = memAllocInt(1);
    	int err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pCount, null);
    	validate(err, "Failed to obtain present mode count!");
    	int count = pCount.get(0);
    	
    	IntBuffer modes = memAllocInt(count);
    	err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pCount, modes);
    	validate(err, "Failed to obtain surface present modes!");

    	memFree(pCount);
    	return modes;
    }
	
	/**
	 * Obtains <b><i><code>VkSurfaceCapabilitiesKHR</code></i></b>.
	 * 
     * @param physicalDevice 	
     * @param surface			
     * @return					Surface capabilities.
	 * @throws VulkanException 	when failed to obtain surface capabilities.
     * @see {@link org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR}
     */
    public static VkSurfaceCapabilitiesKHR getSurfaceCapabilities(VkPhysicalDevice physicalDevice, long surface) throws VulkanException {
    	VkSurfaceCapabilitiesKHR pSurfaceCapabilities = VkSurfaceCapabilitiesKHR.calloc();
    	int err = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, pSurfaceCapabilities);
    	validate(err, "Failed to obtain surface capabilities!");
    	
    	return pSurfaceCapabilities;
    }
	
	/**
	 * Creates VkInsatnce.
	 * 
	 * @param appInfo				Application info.
	 * @param validationLayers		Names of the validation layers that should be enabled.
	 * @param extensionNames	 	Names of the vulkan extensions that should be enabled.
	 * @return		<p>VkInstance</p>
	 * @throws VulkanException 		If there was a problem with Instance creation process.
	 * @see {@link org.lwjgl.vulkan.VkInstance}
	 * @see {@link org.lwjgl.vulkan.VkApplicationInfo}
	 */
	public static VkInstance createInstance(VkApplicationInfo appInfo, Collection<String> validationLayers, Collection<String> extensionNames) throws VulkanException {		
		
		//Logger info
		StringBuilder logString = new StringBuilder();		
		logString.append("\nCreating an instance with folowing validation layers:\n");
		
		// Create ByteBuffer array with names of validation layers.
		ByteBuffer[] layers = new ByteBuffer[validationLayers.size()];
		int pos = 0;
		for (String layerName : validationLayers) {
			layers[pos++] = memUTF8(layerName);
			logString.append("\t" + layerName + "\n");
		}
		
		// Create pointer buffer of the enabled layers.
		PointerBuffer ppEnabledLayerNames = memAllocPointer(layers.length);
		for (int i = 0; i < layers.length; i++)
			ppEnabledLayerNames.put(layers[i]);
		ppEnabledLayerNames.flip();
		
		//Logger info about extensions
		logString.append("\nAnd folowing extensions:\n");
		
		// Create extension name buffers
		ByteBuffer[] extensions = new ByteBuffer[extensionNames.size()];
		pos = 0;
		for (String name : extensionNames) {
			extensions[pos++] = memUTF8(name);
			logString.append("\t" + name + "\n");
		}
		
		// Create pointer buffer to extension name buffers
		PointerBuffer ppExtensions = memAllocPointer(extensions.length);
		for (int i = 0; i < extensions.length; i++)
			ppExtensions.put(extensions[i]);
		ppExtensions.flip();
		
		VkInstanceCreateInfo instanceCreateInfo = VkInstanceCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
				.pNext(NULL)
				.pApplicationInfo(appInfo)
				.ppEnabledExtensionNames(ppExtensions)
				.ppEnabledLayerNames(ppEnabledLayerNames);
		
		PointerBuffer inst = memAllocPointer(1);
		int err = vkCreateInstance(instanceCreateInfo, null, inst);
		VulkanResult.validate(err, "Failed to create Vulkan Instance");
		
		long instanceAdr = inst.get(0);
		VkInstance out = new VkInstance(instanceAdr, instanceCreateInfo);
		
		for (ByteBuffer buff : layers)
			memFree(buff);
		for (ByteBuffer buff : extensions)
			memFree(buff);
		
		memFree(inst);
		memFree(ppExtensions);
		memFree(ppEnabledLayerNames);
		
		instanceCreateInfo.free();
		
		logger.log(Level.CONFIG, logString.toString());
		
		return out;
	}
	

	/**
	 * Lists available validation layers.
	 * 
	 * @return						available vulkan validation layers
	 * @throws VulkanException		when failed to list validation layers.
	 * @see {@link org.lwjgl.vulkan.VkLayerProperties}
	 */
	public static VkLayerProperties[] listAvailableValidationLayers() throws VulkanException {
		VkLayerProperties.Buffer validationLayers;
		
		IntBuffer propsCount = memAllocInt(1);
		int err = vkEnumerateInstanceLayerProperties(propsCount, null);
		validate(err, "Failed to obtain the number of vulkan validation layers!");
		int validationLayersCount = propsCount.get(0);
		
		validationLayers = VkLayerProperties.calloc(validationLayersCount);
		err = vkEnumerateInstanceLayerProperties(propsCount, validationLayers);
		validate(err, "Failed to enumerate vulkan validation layers!");
		
		memFree(propsCount);
		
		VkLayerProperties[] props = new VkLayerProperties[validationLayersCount];
		for (int i = 0; i < validationLayersCount; i++)
			props[i] = validationLayers.get(i);
		
		validationLayers.free();
		return props;
	}
	
	/**
	 * Lists available instance extensions.
	 * 
	 * @return						available instance extensions.
	 * @throws VulkanException		when extension enumeration fails.
	 * @see {@link org.lwjgl.vulkan.VkExtensionProperties}
	 */
	public static VkExtensionProperties[] listAvailableExtensions() throws VulkanException {
		VkExtensionProperties.Buffer extensions;
		
		int[] pPropertyCount = new int[1];
		int err = vkEnumerateInstanceExtensionProperties((CharSequence) null, pPropertyCount, null);
		validate(err, "Failed to enumerate instance extension properties!");
		
		extensions = VkExtensionProperties.calloc(pPropertyCount[0]);
		err = vkEnumerateInstanceExtensionProperties((CharSequence) null, pPropertyCount, extensions);
		validate(err, "Failed to enumerate instance extension properties!");
		
		VkExtensionProperties[] out = new VkExtensionProperties[pPropertyCount[0]];
		for (int i = 0; i < pPropertyCount[0]; i++)
			out[i] = extensions.get(i);
		
		extensions.free();
		
		return out;
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>Enumerate available physical devices.</p>
	 * <p><b>Note:</b> This method should be invoked only once.</p>
	 * 
	 * @param instance				Vulkan instance.
	 * @throws VulkanException 		when the device enumeration process failed.
	 */
	public static VkPhysicalDevice[] enumeratePhysicalDevices(VkInstance instance) throws VulkanException {
		IntBuffer dev_count = memAllocInt(1);
		int err = vkEnumeratePhysicalDevices(instance, dev_count, null);
		validate(err, "Could not enumerate physical devices!");
		
		int devCount = dev_count.get(0);
		PointerBuffer pDevices = memAllocPointer(devCount);
		
		err = vkEnumeratePhysicalDevices(instance, dev_count, pDevices);
		validate(err, "Could not enumerate physical devices!");
		
		VkPhysicalDevice[] devices = new VkPhysicalDevice[devCount];
		for (int i = 0; i < devCount; i++)
			devices[i] = new VkPhysicalDevice(pDevices.get(i), instance);
		
		memFree(dev_count);
		memFree(pDevices);
		
		return devices;
	}
}
