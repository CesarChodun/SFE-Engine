package game.util;

import static core.result.VulkanResult.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import core.rendering.factories.FrameBufferFactory;
import core.resources.Destroyable;
import core.result.VulkanException;

public class BasicFramebufferFactory implements FrameBufferFactory, Destroyable {
	
	private long renderPass;
	private VkDevice device;
	private LongBuffer attachments;
	private VkFramebufferCreateInfo frameBufferCreateInfo;

	public BasicFramebufferFactory(VkDevice device, long renderPass) {
		this.device = device;
		this.renderPass = renderPass;
		
		attachments = memAllocLong(1);
		
		frameBufferCreateInfo = VkFramebufferCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.renderPass(renderPass)
				.pAttachments(attachments)
//				.width(width)
//				.height(height)
				.layers(1);
	}
	
	@Override
	public long[] createFramebuffers(int width, int height, long... imageViews) {
		
		frameBufferCreateInfo
			.width(width)
			.height(height);
		
		long[] framebuffers = new long[imageViews.length];
		LongBuffer pFramebuffer = memAllocLong(1);
		
		for(int i = 0; i < imageViews.length; i++) {
			attachments.put(0, imageViews[i]);
			int err = vkCreateFramebuffer(device, frameBufferCreateInfo, null, pFramebuffer);
			long framebuffer = pFramebuffer.get(0);
			try {
				validate(err, "Failed to create frame buffer!");
			} catch (VulkanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			framebuffers[i] = framebuffer;
		}
		
		memFree(pFramebuffer);
		
		return framebuffers;
	}

	@Override
	public void destroyFramebuffers(long... framebuffers) {
		for (long framebuffer : framebuffers)
			vkDestroyFramebuffer(device, framebuffer, null);//TODO change allocator
	}

	@Override
	public void destroy() {
		memFree(frameBufferCreateInfo.pAttachments());
		frameBufferCreateInfo.free();
	}
}
