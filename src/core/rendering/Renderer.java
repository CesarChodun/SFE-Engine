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
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import core.dataStructures.FixedSizeQueue;
import core.rendering.factories.CommandBufferFactory;
import core.rendering.factories.SwapchainFactory;
import core.result.VulkanException;

/**
 * Externally synchronized
 * @author Cezary Chodun
 *
 */
public abstract class Renderer {
	
	private Window window;
	private VkDevice device;
	
	private CommandBufferFactory cmdFactory;
	private SwapchainFactory swapFactory;
	
	private Long swapchain = VK_NULL_HANDLE;
	private long[] images;
	private VkCommandBuffer[] commandBuffers;
	
	
	private FixedSizeQueue<Integer> 
	renderImageIndices, 
	busyFrames;

	private long[] imageAcquireSemaphores = new long[0];
	protected long[] renderCompleteSemaphores = new long[0];
	
	protected VkSemaphoreCreateInfo imageAcquireSemaphoreCreateInfo;
	protected VkSemaphoreCreateInfo renderCompleteSemaphoreCreateInfo;
	protected VkFenceCreateInfo workDoneFenceInfo;
	
	protected VkSubmitInfo submitInfo;
	private LongBuffer pWaitSemaphores;
	private LongBuffer pSignalSemaphores;
	private PointerBuffer pCommandBuffers;
	
	protected VkPresentInfoKHR presentInfo;
	
	private long[] workDoneFences = new long[0];
	
	private VkQueue queue;
	
	//Must be freed
	private LongBuffer pSwapchains;
	//Must be freed
	private IntBuffer pImageIndex;
	

	public Renderer(Window window, VkDevice device, VkQueue queue, CommandBufferFactory cmdFactory, SwapchainFactory swapchainFactory) {
		this.window = window;
		this.device = device;
		
		this.cmdFactory = cmdFactory;
		this.swapFactory = swapchainFactory;
		
		initRenderingResources();
	}
	
	private void initRenderingResources() {
		imageAcquireSemaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
				.pNext(NULL)
				.flags(0);
		renderCompleteSemaphoreCreateInfo = VkSemaphoreCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
				.pNext(NULL)
				.flags(0);
		
		pWaitSemaphores = memAllocLong(1);
		pSignalSemaphores = memAllocLong(1);
		pCommandBuffers = memAllocPointer(1);

		pSwapchains = memAllocLong(1);
		pSwapchains.put(swapchain).flip();
		
		pImageIndex = memAllocInt(1);
		pImageIndex.put(0).flip();
		
		presentInfo = VkPresentInfoKHR.calloc()
				.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
				.pNext(NULL)
				.swapchainCount(1)
				.pSwapchains(pSwapchains)
				.pImageIndices(pImageIndex)
				.pWaitSemaphores(null)
				.pResults(null);
		
		workDoneFenceInfo = VkFenceCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
				.pNext(NULL)
				.flags(0);
	}
	
	public void update() throws VulkanException {
		recreateSwapchain();
		
		if(this.commandBuffers != null)
			destroyCmdBuffers();
		this.commandBuffers = cmdFactory.createCmdBuffers(images);
		
		renderImageIndices = new FixedSizeQueue<Integer>(images.length);
		busyFrames = new FixedSizeQueue<Integer>(images.length);
		
		//Clean up
		for (int i = 0; i < imageAcquireSemaphores.length; i++) {
			vkDestroySemaphore(device, imageAcquireSemaphores[i], null);
			imageAcquireSemaphores[i] = VK_NULL_HANDLE;
		}
		for (int i = 0; i < renderCompleteSemaphores.length; i++) {
			vkDestroySemaphore(device, renderCompleteSemaphores[i], null);
			renderCompleteSemaphores[i] = VK_NULL_HANDLE;
		}
		for (int i = 0; i < workDoneFences.length; i++) {
			vkDestroyFence(device, workDoneFences[i], null);
			workDoneFences[i] = VK_NULL_HANDLE;
		}
		
		//Create
		imageAcquireSemaphores = new long[images.length];
		renderCompleteSemaphores = new long[images.length];
		workDoneFences = new long[images.length];

		for (int i = 0; i < images.length; i++)
			workDoneFences[i] = createFence(device, workDoneFenceInfo, null);
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
		validate(err, "Failed to acquire imageIndex image KHR!");
		int nextImage = pImageIndex[0];
		
		renderImageIndices.add(nextImage);
		vkDestroySemaphore(device, imageAcquireSemaphores[nextImage], null);
		imageAcquireSemaphores[nextImage] = semaphore;
		
		return true;
	}
	
	public boolean submitToQueue() throws VulkanException {
		
		Integer imageIndex = renderImageIndices.top();
		if (imageIndex == null)
			return false;
		
		if (vkGetFenceStatus(device, workDoneFences[imageIndex]) != VK_SUCCESS)
			return false;
		renderImageIndices.pop();
		
		vkDestroySemaphore(device, renderCompleteSemaphores[imageIndex], null);
		renderCompleteSemaphores[imageIndex] = createSemaphore(device, renderCompleteSemaphoreCreateInfo, null);
		
		pWaitSemaphores.put(0, imageAcquireSemaphores[imageIndex]);
		pSignalSemaphores.put(0, renderCompleteSemaphores[imageIndex]);
		pCommandBuffers.put(0, commandBuffers[imageIndex]);
			
		submitInfo.waitSemaphoreCount(pWaitSemaphores.remaining());
		submitInfo.pWaitSemaphores(pWaitSemaphores);
		submitInfo.pSignalSemaphores(pSignalSemaphores);
		submitInfo.pCommandBuffers(pCommandBuffers);
		
		vkResetFences(device, workDoneFences[imageIndex]);
		
		int err = vkQueueSubmit(queue, submitInfo, workDoneFences[imageIndex]);
		validate(err, "Failed to submit queue work!");
		
		busyFrames.add(imageIndex);
		
		return true;
	}
	
	public boolean presentKHR() throws VulkanException {
		Integer imageIndex = busyFrames.pop();
		if (imageIndex == null)
			return false;
		
		pSignalSemaphores.put(0, renderCompleteSemaphores[imageIndex]);
		
		presentInfo.pImageIndices().put(0, imageIndex);
		presentInfo.pWaitSemaphores(pSignalSemaphores);
		
		int err = vkQueuePresentKHR(queue, presentInfo);
		validate(err, "Failed to present image!");
		
		return true;
	}
	
	public void destroy() {
		//TODO: Free resources
	}
}
