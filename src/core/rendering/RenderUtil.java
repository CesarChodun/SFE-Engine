package core.rendering;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;

import java.nio.ByteBuffer;
import java.util.Collection;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import core.result.VulkanException;
import core.result.VulkanResult;

public class RenderUtil {

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
		
		// Create ByteBuffer array with names of validation layers.
		ByteBuffer[] layers = new ByteBuffer[validationLayers.size()];
		int pos = 0;
		for(String layerName : validationLayers)
			layers[pos++] = memUTF8(layerName);
		
		// Create pointer buffer of the enabled layers.
		PointerBuffer ppEnabledLayerNames = memAllocPointer(layers.length);
		for(int i = 0; i < layers.length; i++)
			ppEnabledLayerNames.put(layers[i]);
		ppEnabledLayerNames.flip();
		
		// Create extension name buffers
		ByteBuffer[] extensions = new ByteBuffer[extensionNames.size()];
		pos = 0;
		for(String name : extensionNames)
			extensions[pos++] = memUTF8(name);
		
		// Create pointer buffer to extension name buffers
		PointerBuffer ppExtensions = memAllocPointer(extensions.length);
		for(int i = 0; i < extensions.length; i++)
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
		VulkanResult.validate(err, "Failed to create Vulkan Instane");
		
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
		
		return out;
	}
	
}
