package core.hardware;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static java.lang.Math.*;
import static core.result.VulkanResult.*;

import java.nio.IntBuffer;

import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import core.result.VulkanException;

import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkInstance;

@Deprecated
/**
 *	<h5>Description: </h5>
 *		<p>This class provide simplified access to children of <code>VkPhysicalDevice</code>.</p>
 *
 * 	@author Cezary Choduń
 *	@see {@link org.lwjgl.vulkan.VkPhysicalDevice}
 */
public class PhysicalDevice extends VkPhysicalDevice{
	
	public VkPhysicalDeviceProperties properties;
	public VkQueueFamilyProperties.Buffer queueFamilyProperties;
	public VkPhysicalDeviceMemoryProperties memoryProperties;
	public int queueFamilyCount;

	/**
	 * <h5>Description</h5>
	 * 
	 * <p>This constructor creates new <code>VkPhysicalDevice</code> and acquire informations about it.
	 * I.e <code>VkPhysicalDeviceProperties</code>, <code>VkQueueFamilyProperties</code></p>
	 * 
	 * @param handle 	- As specified in <code><b>VkPhysicalDevice</b></code>
	 * @param instance	- As specified in <code><b>VkPhysicalDevice</b></code>
	 * 
	 * @see {@link org.lwjgl.vulkan.VkPhysicalDevice}
	 */
	public PhysicalDevice(long handle, VkInstance instance) {
		super(handle, instance);
	}
	
	/**
	 * <h5>Description</h5>
	 * 
	 * <p>Function for acquiring queue family index which meets requirements(<code><i><b>VkQueueFlagBits</b></i></code>).</p>
	 * <h4>Note:</h4>
	 * <p>						If <code><b><i>requiredSupport</i></b></code> equals to <b>0</b> and <code><b><i>first</i></b></code> 
	 * 							is less than <code><b><i>queueFamilyCount</i></b></code> 
	 * 							next queue family will be returned.
	 * </p>	
	 * @param first 			- First interesting queue index. By default <b>0</b>.
	 * @param requiredSupport 	- Required queue flag bits(<code><i><b>VkQueueFlagBits</b></i></code>). For example <code>VK_QUEUE_GRAPHICS_BIT</code>.<br>
	 * 
	 * @return	If queue family has been found, the index of such family is returned. Otherwise <b>-1</b> is returned.
	 * @see {@link org.lwjgl.vulkan.VK10#VK_QUEUE_GRAPHICS_BIT}
	 */
	public int getNextQueueFamilyIndex(int first, int requiredSupport) {
		for(int i = max(0, first); i < queueFamilyCount; i++)
			if((queueFamilyProperties.get(i).queueFlags() & requiredSupport) == requiredSupport)
				return i;
		return -1;
	}
	
	/**
	 * <h5>Description:</h5>
	 * 
	 * <p>Returns the next queue family that supports given surface.</p>
	 * 
	 * @param first				- First interesting queue index.
	 * @param requiredSupport	- Required queue flag bits(<code><i><b>VkQueueFlagBits</b></i></code>).
	 * @param surface			- Surface handle for compatibility check.
	 * @return					First queue family index that meets all requirements or -1 if no such queue exist.
	 * @throws VulkanException	when surface support query fails.
	 */
	public int getNextPresentQueueFamilyIndex(int first, int requiredFlags, long surface) throws VulkanException {
		IntBuffer supportPresent = memAllocInt(1);
		
		for(int i = first; i < queueFamilyCount; i++) {
			supportPresent.position(0);
			
			int err = vkGetPhysicalDeviceSurfaceSupportKHR(this, i, surface, supportPresent);
			validate(err, "Failed to check physical device surface support!");
			
			int at = supportPresent.get(0);
			if((queueFamilyProperties.get(i).queueFlags() & requiredFlags) == requiredFlags && at == VK_TRUE)
				return i;
		}
		
		return -1;
	}
	
	/**
	 * <h5>Description</h5>
	 * <p>Acquire device and queue properties.</p>
	 * @param instance - <b>Must</b> be a valid <code>VkInstance</code>.
	 * @see {@link org.lwjgl.vulkan.VkInstance}
	 */
	public void acquireProperties(VkInstance instance) {
		freeProperties();
		properties = VkPhysicalDeviceProperties.calloc();
		vkGetPhysicalDeviceProperties(this, properties);
		
		IntBuffer queue_family_count_buffer = memAllocInt(1);
		vkGetPhysicalDeviceQueueFamilyProperties(this, queue_family_count_buffer, null);
		queueFamilyCount = queue_family_count_buffer.get(0);
		
		queueFamilyProperties = VkQueueFamilyProperties.calloc(queueFamilyCount);
		vkGetPhysicalDeviceQueueFamilyProperties(this, queue_family_count_buffer, queueFamilyProperties);
		memFree(queue_family_count_buffer);
		
		memoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
		vkGetPhysicalDeviceMemoryProperties(this, memoryProperties);
	}
	
	/**
	 * <h5>Description</h5>
	 * <p>Frees information acquired by <code>PhysicalDevice</code> but leaves <code>VkPhysicalDevice</code> intact.</p>
	 * @see {@link org.lwjgl.vulkan.VkPhysicalDevice}
	 */
	public void freeProperties() {
		if(queueFamilyProperties != null) 
			queueFamilyProperties.free();
		if(properties != null)
			properties.free();
		if(memoryProperties != null)
			memoryProperties.free();
		
		queueFamilyProperties = null;
		properties = null;
		memoryProperties = null;
	}
}
