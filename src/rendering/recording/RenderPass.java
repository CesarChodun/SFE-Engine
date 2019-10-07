package rendering.recording;


import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import core.rendering.Recordable;
import core.resources.Destroyable;
import rendering.config.Attachments;

public class RenderPass implements Recordable, Destroyable{

	VkRenderPassBeginInfo beginInfo = VkRenderPassBeginInfo.calloc();
	
	public RenderPass(VkDevice logicalDevice, Attachments attachments) {
		beginInfo.
	}
	
	@Override
	public void record(VkCommandBuffer buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		beginInfo.free();
	}
	
	

}
