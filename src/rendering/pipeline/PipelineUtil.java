package rendering.pipeline;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static core.result.VulkanResult.*;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import core.result.VulkanException;


/**
 * Package with utility methods for pipeline management.
 * 
 * @author Cezary Chodun
 *
 */
public class PipelineUtil {

	/**
	 * 		reates descriptor set layout.
	 * 
	 * @param device		- Logical device
	 * @param layoutBinding	- Layout binding
	 * @return				- Handle to descriptor set layout
	 * @throws VulkanException 
	 */
	public static long createDescriptorSetLayout(VkDevice device, VkDescriptorSetLayoutBinding.Buffer layoutBindings) throws VulkanException {
		VkDescriptorSetLayoutCreateInfo layoutCreateInfo = VkDescriptorSetLayoutCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
				.pNext(NULL)
				.pBindings(layoutBindings);
		
		LongBuffer pSetLayout = memAllocLong(1);
		int err = vkCreateDescriptorSetLayout(device, layoutCreateInfo, null, pSetLayout);
		validate(err, "Filed to create descriptor set layout.");
		
		long ans = pSetLayout.get(0);
		
		memFree(pSetLayout);
		layoutCreateInfo.free();
		
		return ans;
	}
	
}
