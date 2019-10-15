package core.rendering.factories;

import static core.result.VulkanResult.*;
import static core.rendering.RenderUtil.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashMap;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkViewport;

import core.rendering.Recordable;
import core.resources.Asset;
import core.resources.ConfigFile;
import core.result.VulkanException;
import rendering.recording.RenderPass;

/**
 * Class for command buffer creation.
 * 
 * @author Cezary Chodun
 * @since 26.09.2019//edit
 */
public class CommandBufferFactory{
	
	
	
	/** The render pass for command buffers to use. */
	private RenderPass renderPass;
	/** Clear values. */
	private float[] clearValues;
	/** The Vulkan logical device. */
	private VkDevice device;
	/***/
	private int queueFamilyIndex, flags;
	
	/** A map of command buffers pools. */
	private HashMap<VkCommandBuffer, Long> cmdPools = new HashMap<VkCommandBuffer, Long>();
	
	/**
	 * Creates a new Command Buffer factory.
	 * 
	 * @param device			Vulkan device.
	 * @param cmdWork			Work that will be encoded by command buffer.
	 * @param renderPass		The current render pass.
	 * @param queueFamilyIndex	Index of the render queue family.
	 * @param flags				Command buffer flags(for command pool creation).
	 * @param clearValues		Clear values for the rendered image.
	 */
	public CommandBufferFactory(VkDevice device, RenderPass renderPass, int queueFamilyIndex, int flags, float[] clearValues) {
		this.device = device;
		this.renderPass = renderPass;
		this.clearValues = clearValues;
		this.queueFamilyIndex = queueFamilyIndex;
		this.flags = flags;
	}

	/**
	 * Creates command buffers(one for each frame buffer).
	 * 
	 * @param width			Image width.
	 * @param height		Image height.
	 * @param framebuffers	The frame buffers.
	 * @return		The command buffers.
	 */
	public VkCommandBuffer[] createCmdBuffers(int width, int height, long... framebuffers) {
		long commandPool;
		try {
			commandPool = createCommandPool(device, queueFamilyIndex, flags);
		} catch (VulkanException e) {
			e.printStackTrace();
			throw new AssertionError(e.getMessage());
		}
		
		VkCommandBuffer[] commandBuffers;
		
		
//		renderPassBeginInfo.renderArea().offset().set(0, 0);
//		renderPassBeginInfo.renderArea().extent().set(width, height);
		
		VkCommandBufferAllocateInfo cbai = VkCommandBufferAllocateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
				.pNext(NULL)
				.commandPool(commandPool)
				.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
				.commandBufferCount(framebuffers.length);

		VkCommandBufferBeginInfo cbbi = VkCommandBufferBeginInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
				.pNext(NULL);
		
//		//TODO remove viewport and scisors.
//		// Update dynamic viewport state
//		VkViewport.Buffer viewport = VkViewport.calloc(1)
//				.height(height)
//				.width(width)
//				.minDepth(0.0f)
//				.maxDepth(1.0f)
//				.x(0)
//				.y(0);

//		Update dynamic scissor state
//		VkRect2D.Buffer scissor = VkRect2D.calloc(1);
//		scissor.extent().set(width, height);
//		scissor.offset().set(0, 0);
		
		PointerBuffer pCommandBuffer = memAllocPointer(framebuffers.length);
		int err = vkAllocateCommandBuffers(device, cbai, pCommandBuffer);
		try {
			validate(err, "Failed to allocate command buffers!");
		} catch (VulkanException e) {
			e.printStackTrace();
			throw new AssertionError(e.getMessage());
		}
		
		commandBuffers = new VkCommandBuffer[framebuffers.length];
		
//		IntBuffer descOffsets = memAllocInt(1);
//		descOffsets.put(0);
//		descOffsets.flip();
		
		for(int i = 0; i < framebuffers.length; i++) {
//			LongBuffer pDesc = memAllocLong(2);
//			pDesc.put(descriptorA[i].getDescriptorSet()).put(descriptorB[i].getDescriptorSet());
//			pDesc.flip();
			
			commandBuffers[i] = new VkCommandBuffer(pCommandBuffer.get(i), device);
			
			
			err = vkBeginCommandBuffer(commandBuffers[i], cbbi);
			try {
				validate(err, "Failed to begin command buffer!");
			} catch (VulkanException e) {
				e.printStackTrace();
				throw new AssertionError(e.getMessage());
			}
			
			renderPass.record(commandBuffers[i], framebuffers[i]);

 
//			vkCmdSetViewport(commandBuffers[i], 0, viewport);
//			vkCmdSetScissor(commandBuffers[i], 0, scissor);
//			
//			cmdWork.record(commandBuffers[i]);
			
////			
//			vkCmdBindPipeline(commandBuffers[i], VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
			
//			descriptor.record(commandBuffers[i]);
//			vkCmdBindDescriptorSets(commandBuffers[i], VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, pDesc, null);
//			
//			mesh.draw(commandBuffers[i]);
//			
//			vkCmdEndRenderPass(commandBuffers[i]);
			err = vkEndCommandBuffer(commandBuffers[i]);
			try {
				validate(err, "Failed to end command buffer!");
			} catch (VulkanException e) {
				e.printStackTrace();
				throw new AssertionError(e.getMessage());
			}
		}
//
//		scissor.free();
//		viewport.free();
		
		cmdPools.put(commandBuffers[0], commandPool);
		
		return commandBuffers;
	}

	/**
	 * Destroys the command buffers allocated from this factory.
	 * It is required that the command buffer list
	 * contains the same command buffer set. 
	 * As returned in the createCmdBuffers().
	 * 
	 * Cannot be invoked if any of the command buffers
	 * is in the pending state!
	 * 
	 * @param buffers		The list of buffers returned by 
	 * createCmdBuffers().
	 */
	public void destroyCmdBuffers(VkCommandBuffer[] buffers) {
		
		for (VkCommandBuffer cmd : buffers) 
			if (cmdPools.containsKey(cmd)){
				Long pool = cmdPools.get(cmd);
				vkDestroyCommandPool(device, pool, null);
			}
		
	}
	
	
	
}
