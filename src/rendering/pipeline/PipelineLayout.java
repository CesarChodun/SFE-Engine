package rendering.pipeline;

import static core.result.VulkanResult.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;

import core.result.VulkanException;

public class PipelineLayout {
	
	private long[] layouts;
	private long pipelineLayout;
	
	private VkDevice device;
	
	public PipelineLayout(VkDevice device, long... layouts) {
		this.layouts = layouts;
		this.device = device;
		
		try {
			createPipelineLayout();
		} catch (VulkanException e) {
			e.printStackTrace();
		}
	}

	private void createPipelineLayout() throws VulkanException {
		
		VkPipelineLayoutCreateInfo pPipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
			.pNext(NULL)
			.flags(0)
			.pPushConstantRanges(null)
			.pSetLayouts(null);
		
		LongBuffer pSetLayouts = null;
		if (layouts.length != 0) {
			pSetLayouts = memAllocLong(layouts.length);
			for (int i = 0; i < layouts.length; i++)
				pSetLayouts.put(i, layouts[i]);
			
			pPipelineLayoutCreateInfo.pSetLayouts(pSetLayouts);
		}
		
		LongBuffer pPipelineLayout = memAllocLong(1);
		int err = vkCreatePipelineLayout(device, pPipelineLayoutCreateInfo, null, pPipelineLayout);
		validate(err, "Failed to create pipeline layout!");
		
		pipelineLayout = pPipelineLayout.get(0);
		
		if (pSetLayouts != null)
			memFree(pSetLayouts);
		memFree(pPipelineLayout);
		pPipelineLayoutCreateInfo.free();
	}

	public long getPipelineLayout() {
		return pipelineLayout;
	}
}
