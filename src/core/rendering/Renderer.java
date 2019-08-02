package core.rendering;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static core.rendering.RenderUtil.*;
import static core.result.VulkanResult.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Set;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import core.dataStructures.FixedSizeQueue;
import core.rendering.factories.CommandBufferFactory;
import core.rendering.factories.SwapchainFactory;
import core.result.VulkanException;

public abstract class Renderer {
	
	private Window window;
	private VkDevice device;
	
	private CommandBufferFactory cmdFactory;
	private SwapchainFactory swapFactory;
	
	private Long swapchain = VK_NULL_HANDLE;
	private long[] images;
	private VkCommandBuffer[] commandBuffers;
	
//	private 

	public Renderer(Window window, VkDevice device, CommandBufferFactory cmdFactory, SwapchainFactory swapchainFactory) {
		this.window = window;
		this.device = device;
		
		this.cmdFactory = cmdFactory;
		this.swapFactory = swapchainFactory;
	}
	
	public void update() throws VulkanException {
		recreateSwapchain();
		
		if(this.commandBuffers != null)
			destroyCmdBuffers();
		this.commandBuffers = cmdFactory.createCmdBuffers(images);
	}
	
	private void destroyCmdBuffers() {
		cmdFactory.destroyCmdBuffers(commandBuffers);
		commandBuffers = null;
	}
	
	private void recreateSwapchain() throws VulkanException {
		
    	VkSwapchainCreateInfoKHR createInfo = swapFactory.getCreateInfo(window, swapchain);
		
    	//Create swapchain
    	LongBuffer pSwapchain = memAllocLong(1);
    	int err = vkCreateSwapchainKHR(device, createInfo, null, pSwapchain);
    	validate(err, "Failed to recreate swapchain!");
    	swapchain = pSwapchain.get(0);
    	
    	//Destroying old swapchain.
    	if(swapchain != VK_NULL_HANDLE)
    		vkDestroySwapchainKHR(device, swapchain, null);
    	
    	//Extracting image handles from swapchain.
    	IntBuffer pSwapchainImageCount = memAllocInt(1);
    	err = vkGetSwapchainImagesKHR(device, swapchain, pSwapchainImageCount, null);
    	validate(err, "Failed to enumerate swapchain images!");
    	
    	int swapchainImageCount = pSwapchainImageCount.get(0);
    	
    	LongBuffer pSwapchainImages = memAllocLong(swapchainImageCount);
    	err = vkGetSwapchainImagesKHR(device, swapchain, pSwapchainImageCount, pSwapchainImages);
    	validate(err, "Failed to obtain swapchain images!");
    	
    	images = new long[swapchainImageCount];
    	for(int i = 0; i < swapchainImageCount; i++)
    		images[i] = pSwapchainImages.get(i);
    	
    	//Clean up
    	memFree(pSwapchain);
    	memFree(pSwapchainImageCount);
    	memFree(pSwapchainImages);
    	createInfo.free();
	}
//
//	private Set<Integer> 
//			suspendedImageIndices;
	private FixedSizeQueue<Integer> 
			renderImageIndices, 
			busyFrames;

	private long[] imageAcquireSemaphores;
	protected long[] renderCompleteSemaphores;

	protected VkSemaphoreCreateInfo imageAcquireSemaphoreCreateInfo;
	protected VkSemaphoreCreateInfo renderCompleteSemaphoreCreateInfo;
	
	protected VkSubmitInfo submitInfo;
	private LongBuffer pWaitSemaphores;
	private LongBuffer pSignalSemaphores;
	private PointerBuffer pCommandBuffers;
	
	private long[] workDoneFences;
	
	private VkQueue queue;
	/**
	 * 
	 * Must be synchronized!
	 * 
	 * @throws VulkanException
	 */
	public boolean acquireNextImage() throws VulkanException {
		
		// Checks whether there are free images
		if (renderImageIndices.size() == images.length)
			return false;
		
		long semaphore = createSemaphore(device, imageAcquireSemaphoreCreateInfo, null);
		
		int[] pImageIndex = new int[1];//TODO: Utilize fences
		int err = vkAcquireNextImageKHR(device, swapchain, 0xFFFFFFFFFFFFFFFFL, semaphore, VK_NULL_HANDLE, pImageIndex);
		validate(err, "Failed to acquire next image KHR!");
		int nextImage = pImageIndex[0];
		
		renderImageIndices.add(nextImage);
		vkDestroySemaphore(device, imageAcquireSemaphores[nextImage], null);
		imageAcquireSemaphores[nextImage] = semaphore;
		
		return true;
	}
	
	public boolean submitToQueue() throws VulkanException {
		
		Integer next = renderImageIndices.pop();
		if (next == null)
			return false;
		
		vkDestroySemaphore(device, renderCompleteSemaphores[next], null);
		renderCompleteSemaphores[next] = createSemaphore(device, renderCompleteSemaphoreCreateInfo, null);
		
		pWaitSemaphores.put(0, imageAcquireSemaphores[next]);
		pSignalSemaphores.put(0, renderCompleteSemaphores[next]);
		pCommandBuffers.put(0, commandBuffers[next]);
			
		submitInfo.waitSemaphoreCount(pWaitSemaphores.remaining());
		submitInfo.pWaitSemaphores(pWaitSemaphores);
		submitInfo.pSignalSemaphores(pSignalSemaphores);
		submitInfo.pCommandBuffers(pCommandBuffers);
		
		vkResetFences(device, workDoneFences[next]);
		
		int err = vkQueueSubmit(queue, submitInfo, workDoneFences[next]);
		validate(err, "Failed to submit queue work!");
		
		busyFrames.add(next);
		
		return true;
	}
	
	public void presentKHR() {
		
	}
	
//	public void render() {
//		render(commandBuffers);
//	}
//	
//	protected abstract void render(VkCommandBuffer[] commandBuffers);
	
}
