package core.rendering.presets;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import static org.lwjgl.vulkan.VK10.*;
import static core.rendering.RenderUtil.*;
import static core.result.VulkanResult.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;

import core.rendering.Renderer;
import core.rendering.Window;
import core.rendering.factories.CommandBufferFactory;
import core.rendering.factories.SwapchainFactory;
import core.result.VulkanException;

public class StreamLineRenderer extends Renderer{
	
	protected int imageCount;
	protected VkDevice device;
	protected long swapchain;
	
	protected VkSemaphoreCreateInfo imageAcquireSemaphoreCreateInfo;
	protected long[] imageAcquireSemaphores;

	protected VkSemaphoreCreateInfo renderCompleteSemaphoreCreateInfo;
	protected long[] renderCompleteSemaphores;
	
	protected long[] workStatusFences;
	protected boolean[] workSubmited;

	protected VkQueue queue;
	protected VkSubmitInfo submitInfo;
	protected VkPresentInfoKHR presentInfo;
	
	private LongBuffer pWaitSemaphores;
	private LongBuffer pSignalSemaphores;
	
	public StreamLineRenderer(Window window, VkDevice device, CommandBufferFactory cmdFactory,
			SwapchainFactory swapchainFactory, int a) {
		super(window, device, cmdFactory, swapchainFactory);
		
		this.device = device;
//		this.queue = queue;
//		this.submitInfo = submitInfo;
//		this.imageCount = swapchain.images.length;
//		this.swapchain = swapchain.swapchainHandle;
		
		try {
			init();
		} catch (VulkanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void init() throws VulkanException {
		imageAcquireSemaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
				.pNext(NULL)
				.flags(0);
		renderCompleteSemaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
				.pNext(NULL)
				.flags(0);
		
		imageAcquireSemaphores = new long[imageCount];
		renderCompleteSemaphores = new long[imageCount];
		
		for(int i = imageCount-1; i != -1; i--) {
			imageAcquireSemaphores[i] = createSemaphore(device, imageAcquireSemaphoreCreateInfo, null);
			renderCompleteSemaphores[i] = createSemaphore(device, renderCompleteSemaphoreCreateInfo, null);
		}
		
		workStatusFences = new long[imageCount];
		workSubmited = new boolean[imageCount];
		
		VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
				.pNext(NULL)
				.flags(0);
		
		for(int i = imageCount-1; i != -1; i--) {
			workStatusFences[i] = createFence(device, fenceInfo, null);
			workSubmited[i] =  false;
		}
		
		LongBuffer pSwapchains = memAllocLong(1);
		pSwapchains.put(swapchain).flip();
		
		IntBuffer pImageIndex = memAllocInt(1);
		pImageIndex.put(0).flip();
		
		presentInfo = VkPresentInfoKHR.calloc()
				.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
				.pNext(NULL)
				.swapchainCount(1)
				.pSwapchains(pSwapchains)
				.pImageIndices(pImageIndex)
//				.pWaitSemaphores(pRenderCompleteSemaphore)
				.pResults(null);
		
		pWaitSemaphores = memAllocLong(1);
		pSignalSemaphores = memAllocLong(1);
	}
	
	int next = -1;

	@Override
	protected void render(VkCommandBuffer[] commandBuffers) throws VulkanException {
		glfwPollEvents();
		update();
		next++;
		next %= imageCount;
		
		int[] pImageIndex = new int[1];
		int err = vkAcquireNextImageKHR(device, swapchain, 0xFFFFFFFFFFFFFFFFL, imageAcquireSemaphores[next], VK_NULL_HANDLE, pImageIndex);
		validate(err, "Failed to acquire next image(KHR)!");
		int imageIndex = pImageIndex[0];
		
		if(workSubmited[imageIndex] == true) {
//			do {
////				System.out.println("Hello: " + workStatusFences[imageIndex]);
//				err = vkWaitForFences(device, workStatusFences[imageIndex], true, 8000000);
//			}
//			while(err != VK_SUCCESS);
			while(vkGetFenceStatus(device, workStatusFences[imageIndex]) != VK_SUCCESS) {
//				System.out.println("hello");
				glfwPollEvents();//TODO: Check if correct
				update();
			}

			err = vkResetFences(device, workStatusFences[imageIndex]);
			validate(err, "Failed to reset fence!");
			workSubmited[imageIndex] = false;
		}
		
	pWaitSemaphores.put(0, imageAcquireSemaphores[next]);//*
	pSignalSemaphores.put(0, renderCompleteSemaphores[imageIndex]);
		
		submitInfo.waitSemaphoreCount(pWaitSemaphores.remaining());
		submitInfo.pWaitSemaphores(pWaitSemaphores);
		submitInfo.pSignalSemaphores(pSignalSemaphores);
		PointerBuffer pCmdBuff = memAllocPointer(1);
		pCmdBuff.put(commandBuffers[imageIndex]);
		submitInfo.pCommandBuffers(pCmdBuff);
		
		
		err = vkQueueSubmit(queue, submitInfo, workStatusFences[imageIndex]);
		workSubmited[imageIndex] = true;
		validate(err, "Failed to submit queue!");

		presentInfo.pImageIndices().put(0, imageIndex);
		presentInfo.pWaitSemaphores(pSignalSemaphores);
		
		err = vkQueuePresentKHR(queue, presentInfo);
		validate(err, "Failed to present KHR!");
		
		memFree(pCmdBuff);
	}

}
