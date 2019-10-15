package rendering.recording;

import static org.lwjgl.vulkan.VK10.*;

import java.nio.LongBuffer;

import org.eclipse.jdt.annotation.Nullable;

import static core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

import core.rendering.Recordable;
import core.resources.Destroyable;
import core.result.VulkanException;
import rendering.config.Attachments;

public class RenderPass implements Destroyable{

	private VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc();
	private VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc();
	private Attachments attachments;
	
	private Recordable work;
	
	private int contents = VK_SUBPASS_CONTENTS_INLINE; //TODO: let the implementation change this value
	
	private long renderPass;
	
	/**
	 * 
	 * 
	 * @param logicalDevice
	 * @param attachments		Must be freed after the render pass was destroyed.
	 * @param subpasses			may be freed afterwards
	 * @param dependecies		may be freed afterwards
	 * @throws VulkanException 
	 */
	public RenderPass(VkDevice logicalDevice, Attachments attachments, VkSubpassDescription.Buffer subpasses, VkSubpassDependency.@Nullable Buffer dependecies) throws VulkanException {
		VkAttachmentDescription.Buffer buf = attachments.getBuffer();
		
		renderPassInfo
			.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
			.pNext(NULL)
			.pAttachments(buf)
			.pSubpasses(subpasses)
			.pDependencies(dependecies);
		
		this.attachments = attachments;
		
		LongBuffer pRenderPass = memAllocLong(1);
		int err = vkCreateRenderPass(logicalDevice, renderPassInfo, null, pRenderPass);
		validate(err, "Failed to create render pass!");
		
		renderPass = pRenderPass.get(0);
		memFree(pRenderPass);
		
		renderPassBeginInfo
			.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
			.pNext(NULL)
			.renderPass(renderPass)
			.pClearValues(this.attachments.getClearValues());
	}
	
	public void record(VkCommandBuffer cmd, long frameBuffer) {
		renderPassBeginInfo.framebuffer(frameBuffer);
		
		vkCmdBeginRenderPass(cmd, renderPassBeginInfo, contents);
		
		work.record(cmd);
		
		vkCmdEndRenderPass(cmd);
	}
	
	public long handle() {
		return renderPass;
	}

	@Override
	public void destroy() {
		renderPassInfo.free();
		renderPassBeginInfo.free();
	}

	public Recordable getWork() {
		return work;
	}
	public void setWork(Recordable work) {
		this.work = work;
	}
}
