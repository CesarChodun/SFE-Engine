//package game.factories;
//
//import static core.result.VulkanResult.*;
//import static org.lwjgl.system.MemoryUtil.*;
//import static org.lwjgl.vulkan.VK10.*;
//
//import java.nio.ByteBuffer;
//import java.nio.FloatBuffer;
//import java.nio.IntBuffer;
//import java.nio.LongBuffer;
//
//import org.lwjgl.PointerBuffer;
//import org.lwjgl.system.MemoryUtil;
//import org.lwjgl.vulkan.VkBufferCreateInfo;
//import org.lwjgl.vulkan.VkClearValue;
//import org.lwjgl.vulkan.VkCommandBuffer;
//import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
//import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
//import org.lwjgl.vulkan.VkDevice;
//import org.lwjgl.vulkan.VkFramebufferCreateInfo;
//import org.lwjgl.vulkan.VkMemoryAllocateInfo;
//import org.lwjgl.vulkan.VkMemoryRequirements;
//import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
//import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
//import org.lwjgl.vulkan.VkRect2D;
//import org.lwjgl.vulkan.VkRenderPassBeginInfo;
//import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
//import org.lwjgl.vulkan.VkVertexInputBindingDescription;
//import org.lwjgl.vulkan.VkViewport;
//
//import core.rendering.factories.CommandBufferFactory;
//import core.rendering.recording.Recordable;
//import core.result.VulkanException;
//
//
//public class BasicCommandBufferFactory implements CommandBufferFactory{
//
////	 /**
////     * This is just -1L, but it is nicer as a symbolic constant.
////     */
////    private static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;
//	
//	private long renderPass;
//	private long commandPool;
//	private float[] clearValues;
//	private VkDevice device;
//	private Recordable cmdWork;
//	
//	
//
//	@Override
//	public VkCommandBuffer[] createCmdBuffers(long[] framebuffers) {
//		VkCommandBuffer[] commandBuffers;
//		
//		VkClearValue.Buffer cv = VkClearValue.calloc(2);
//		cv.get(0).color()
//			.float32(0, clearValues[0])
//            .float32(1, clearValues[1])
//            .float32(2, clearValues[2])
//            .float32(3, clearValues[3]);
//		cv.get(1).depthStencil()
//			.depth(1f);
//		
//		VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc()
//				.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
//				.pNext(NULL)
//				.renderPass(renderPass)
//				.pClearValues(cv);
//		
////		renderPassBeginInfo.renderArea().offset().set(0, 0);
////		renderPassBeginInfo.renderArea().extent().set(width, height);
//		
//		VkCommandBufferAllocateInfo cbai = VkCommandBufferAllocateInfo.calloc()
//				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
//				.pNext(NULL)
//				.commandPool(commandPool)
//				.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
//				.commandBufferCount(framebuffers.length);
//
//		VkCommandBufferBeginInfo cbbi = VkCommandBufferBeginInfo.calloc()
//				.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
//				.pNext(NULL);
//		
//		// Update dynamic viewport state
////		VkViewport.Buffer viewport = VkViewport.calloc(1)
////				.height(height)
////				.width(width)
////				.minDepth(0.0f)
////				.maxDepth(1.0f)
////				.x(0)
////				.y(0);
//
//		//Update dynamic scissor state
////		VkRect2D.Buffer scissor = VkRect2D.calloc(1);
////		scissor.extent().set(width, height);
////		scissor.offset().set(0, 0);
//		
//		PointerBuffer pCommandBuffer = memAllocPointer(framebuffers.length);
//		int err = vkAllocateCommandBuffers(device, cbai, pCommandBuffer);
//		try {
//			validate(err, "Failed to allocate command buffers!");
//		} catch (VulkanException e) {
//			e.printStackTrace();
//			throw new AssertionError(e.getMessage());
//		}
//		
//		commandBuffers = new VkCommandBuffer[framebuffers.length];
//		
////		IntBuffer descOffsets = memAllocInt(1);
////		descOffsets.put(0);
////		descOffsets.flip();
//		
//		for(int i = 0; i < framebuffers.length; i++) {
////			LongBuffer pDesc = memAllocLong(2);
////			pDesc.put(descriptorA[i].getDescriptorSet()).put(descriptorB[i].getDescriptorSet());
////			pDesc.flip();
//			
//			commandBuffers[i] = new VkCommandBuffer(pCommandBuffer.get(i), device);
//			renderPassBeginInfo.framebuffer(framebuffers[i]);
//			
//			
//			err = vkBeginCommandBuffer(commandBuffers[i], cbbi);
//			try {
//				validate(err, "Failed to begin command buffer!");
//			} catch (VulkanException e) {
//				e.printStackTrace();
//				throw new AssertionError(e.getMessage());
//			}
//			
//			vkCmdBeginRenderPass(commandBuffers[i], renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);
//
//			cmdWork.record(commandBuffers[i]);
//			
////			vkCmdSetViewport(commandBuffers[i], 0, viewport);
////			vkCmdSetScissor(commandBuffers[i], 0, scissor);
//////			
////			vkCmdBindPipeline(commandBuffers[i], VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
//			
////			descriptor.record(commandBuffers[i]);
////			vkCmdBindDescriptorSets(commandBuffers[i], VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, pDesc, null);
////			
////			mesh.draw(commandBuffers[i]);
////			
//			vkCmdEndRenderPass(commandBuffers[i]);
//			err = vkEndCommandBuffer(commandBuffers[i]);
//			try {
//				validate(err, "Failed to end command buffer!");
//			} catch (VulkanException e) {
//				e.printStackTrace();
//				throw new AssertionError(e.getMessage());
//			}
//		}
////
////		scissor.free();
////		viewport.free();
//		
//		return commandBuffers;
//	}
//
//	@Override
//	public void destroyCmdBuffers(VkCommandBuffer[] buffers) {
//		// TODO Auto-generated method stub
//		
//	}
//	
//	
//	
//}
