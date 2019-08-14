package core.rendering.factories;

import static core.result.VulkanResult.*;
import static core.rendering.RenderUtil.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;

import core.rendering.recording.Recordable;
import core.resources.Asset;
import core.resources.ConfigFile;
import core.result.VulkanError;
import core.result.VulkanException;


public class CommandBufferFactory{
	
	public static final String 
		CONFIG_FILE_NAME = "cmdFactory.cfg",
		CV_KEY_0 = "red",
		CV_KEY_1 = "green",
		CV_KEY_2 = "blue",
		CV_KEY_3 = "alfa";

	private static final float 
		CV_VALUE_0 = 1f, 
		CV_VALUE_1 = 1f, 
		CV_VALUE_2 = 1f, 
		CV_VALUE_3 = 0.5f ;
	
//	 /**
//     * This is just -1L, but it is nicer as a symbolic constant.
//     */
//    private static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;
	
	private long renderPass;
	private float[] clearValues;
	private VkDevice device;
	private Recordable cmdWork;
	private int queueFamilyIndex, flags;
	
	private HashMap<VkCommandBuffer, Long> cmdPools = new HashMap<VkCommandBuffer, Long>();
	
	public CommandBufferFactory(VkDevice device, Recordable cmdWork, long renderPass, int queueFamilyIndex, int flags, float[] clearValues) {
		this.device = device;
		this.cmdWork = cmdWork;
		this.renderPass = renderPass;
		this.clearValues = clearValues;
		this.queueFamilyIndex = queueFamilyIndex;
		this.flags = flags;
	}
	
	public CommandBufferFactory(VkDevice device, Recordable cmdWork, int queueFamilyIndex, int flags, long renderPass, Asset config) throws IOException, AssertionError {
		this.device = device;
		this.cmdWork = cmdWork;
		this.renderPass = renderPass;
		this.queueFamilyIndex = queueFamilyIndex;
		this.flags = flags;
		
		ConfigFile cfg = new ConfigFile(config, CONFIG_FILE_NAME);
		this.clearValues = new float[4];
		this.clearValues[0] = cfg.getFloat(CV_KEY_0, CV_VALUE_0);
		this.clearValues[1] = cfg.getFloat(CV_KEY_1, CV_VALUE_1);
		this.clearValues[2] = cfg.getFloat(CV_KEY_2, CV_VALUE_2);
		this.clearValues[3] = cfg.getFloat(CV_KEY_3, CV_VALUE_3);
		cfg.close();
	}

	public VkCommandBuffer[] createCmdBuffers(long[] framebuffers) {
		long commandPool;
		try {
			commandPool = createCommandPool(device, queueFamilyIndex, flags);
		} catch (VulkanException e) {
			e.printStackTrace();
			throw new AssertionError(e.getMessage());
		}
		
		VkCommandBuffer[] commandBuffers;
		
		VkClearValue.Buffer cv = VkClearValue.calloc(2);
		cv.get(0).color()
			.float32(0, clearValues[0])
            .float32(1, clearValues[1])
            .float32(2, clearValues[2])
            .float32(3, clearValues[3]);
		cv.get(1).depthStencil()
			.depth(1f);
		
		VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
				.pNext(NULL)
				.renderPass(renderPass)
				.pClearValues(cv);
		
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
		
		// Update dynamic viewport state
//		VkViewport.Buffer viewport = VkViewport.calloc(1)
//				.height(height)
//				.width(width)
//				.minDepth(0.0f)
//				.maxDepth(1.0f)
//				.x(0)
//				.y(0);

		//Update dynamic scissor state
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
			renderPassBeginInfo.framebuffer(framebuffers[i]);
			
			
			err = vkBeginCommandBuffer(commandBuffers[i], cbbi);
			try {
				validate(err, "Failed to begin command buffer!");
			} catch (VulkanException e) {
				e.printStackTrace();
				throw new AssertionError(e.getMessage());
			}
			
			vkCmdBeginRenderPass(commandBuffers[i], renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

			cmdWork.record(commandBuffers[i]);
			
//			vkCmdSetViewport(commandBuffers[i], 0, viewport);
//			vkCmdSetScissor(commandBuffers[i], 0, scissor);
////			
//			vkCmdBindPipeline(commandBuffers[i], VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
			
//			descriptor.record(commandBuffers[i]);
//			vkCmdBindDescriptorSets(commandBuffers[i], VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, pDesc, null);
//			
//			mesh.draw(commandBuffers[i]);
//			
			vkCmdEndRenderPass(commandBuffers[i]);
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

	public void destroyCmdBuffers(VkCommandBuffer[] buffers) {
		//Nothing to do
	}
	
	
	
}
