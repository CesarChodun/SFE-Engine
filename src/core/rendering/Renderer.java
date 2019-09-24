package core.rendering;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static core.rendering.RenderUtil.*;
import static core.result.VulkanResult.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubresourceLayout;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import core.rendering.factories.CommandBufferFactory;
import core.rendering.factories.FrameBufferFactory;
import core.rendering.factories.SwapchainFactory;
import core.result.VulkanException;

/**
 * Externally synchronized
 * @author Cezary Chodun
 *
 */
public class Renderer {
	
	private Window window;
	private VkDevice device;
	
	private CommandBufferFactory cmdFactory;
	private SwapchainFactory swapFactory;
	private FrameBufferFactory fbFactory;
	private VkImageViewCreateInfo imageViewCreateInfo;
	
	private Long swapchain = VK_NULL_HANDLE;
	private long[] images;
	private long[] imageViews;
	private long[] framebuffers;
	private VkCommandBuffer[] commandBuffers;
	
	
	private List<Integer> 
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
	private LongBuffer pSwapchain;
	//Must be freed
	private IntBuffer pImageIndex;
	
	private int width, height;

	public Renderer(
			Window window, 
			VkDevice device, 
			VkQueue queue, 
			VkImageViewCreateInfo imageViewCreateInfo,
			CommandBufferFactory cmdFactory, 
			SwapchainFactory swapchainFactory, 
			FrameBufferFactory frameBufferFactory) {
		
		this.window = window;
		this.device = device;
		this.queue = queue;
		this.imageViewCreateInfo = imageViewCreateInfo;
		
		if (device.getCapabilities().vkCreateSwapchainKHR == NULL)
			throw new AssertionError("The device cannot create the swapchain.");
		
		this.cmdFactory = cmdFactory;
		this.swapFactory = swapchainFactory;
		this.fbFactory = frameBufferFactory;
		
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

		pSwapchain = memAllocLong(1);
		
		pImageIndex = memAllocInt(1);
		pImageIndex.put(0).flip();
		
		presentInfo = VkPresentInfoKHR.calloc()
				.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
				.pNext(NULL)
				.swapchainCount(1)
				.pSwapchains(pSwapchain)
				.pImageIndices(pImageIndex)
				.pWaitSemaphores(null)
				.pResults(null);
		
		workDoneFenceInfo = VkFenceCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
				.pNext(NULL)
				.flags(0);
		
		IntBuffer pWaitDstStageMask = memAllocInt(1);
        pWaitDstStageMask.put(0, VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
		
		submitInfo = VkSubmitInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pNext(NULL)
                .pWaitDstStageMask(pWaitDstStageMask);
//                .waitSemaphoreCount(pImageAcquiredSemaphore.remaining())
//                .pWaitSemaphores(pImageAcquiredSemaphore)
//                .pWaitDstStageMask(pWaitDstStageMask)
//                .pCommandBuffers(pCommandBuffers)
//                .pSignalSemaphores(pRenderCompleteSemaphore);
	}
	
	public void update() throws VulkanException {
		recreateSwapchain();
		
		if(this.commandBuffers != null) 
			destroyCmdBuffers();
		this.commandBuffers = cmdFactory.createCmdBuffers(width, height, framebuffers);
		
		renderImageIndices = new ArrayList<Integer>(images.length);
		busyFrames = new ArrayList<Integer>(images.length);
		
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
	
	@Deprecated //TODO remove
	/**
     * <h5>Description:</h5>
	 * <p>
	 * 		Creates image views(swapchain <b>must</b> be created before this call).
	 * </p>
     * @param device
     * @param colorAttachmentView
     */
    public long[] createImageViews(VkDevice device, VkImageViewCreateInfo colorAttachmentView, long... images) throws VulkanException {
    	
    	long[] imageViews = new long[images.length];
    	
    	LongBuffer pView = memAllocLong(1);
    	for(int i = 0; i < images.length; i++) {
    		colorAttachmentView.image(images[i]);
    		int err = vkCreateImageView(device, colorAttachmentView, null, pView);
    		validate(err, "Failed to create image view.");
    		
    		imageViews[i] = pView.get(0);
    	}
    	
    	memFree(pView);
    	
    	return imageViews;
    }
    
	@Deprecated //TODO remove
    /**
     * <h5>Description:</h5>
	 * <p>
	 * 		Destroys swapchain image views.
	 * 		<b>Note</b> when creating new swapchain image views from the old one are deleted automatically.
	 * </p>
     * @param device
     */
    public void destroyImageViews(VkDevice device, long...imageViews) {    	
    	for(int i = 0; i < imageViews.length; i++)
    		vkDestroyImageView(device, imageViews[i], null);
    }
	
	private void recreateSwapchain() throws VulkanException {
		
    	VkSwapchainCreateInfoKHR createInfo = swapFactory.getCreateInfo(window, swapchain);
    	width = createInfo.imageExtent().width();
    	height = createInfo.imageExtent().height();
    	
    	
    	//Destroying old swapchain.
    	if(swapchain != VK_NULL_HANDLE)
    		vkDestroySwapchainKHR(device, swapchain, null);
    	
    	//Create swapchain
    	int err = vkCreateSwapchainKHR(device, createInfo, null, pSwapchain);
    	validate(err, "Failed to recreate swapchain!");
    	swapchain = pSwapchain.get(0);
    	
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
    	

    	if(imageViews != null)
    		destroyImageViews(device);
    	imageViews = createImageViews(device, imageViewCreateInfo, images);
    	
    	//Create framebuffers
    	if (framebuffers != null)
    		fbFactory.destroyFramebuffers(framebuffers);
    	framebuffers = fbFactory.createFramebuffers(width, height, imageViews);
    	
    	//Clean up
    	memFree(pSwapchainImageCount);
    	memFree(pSwapchainImages);
    	createInfo.free();
	}

	//TODO
	private int workingImages = 0;
	
	/**
	 * 
	 * Must be synchronized!
	 * 
	 * @throws VulkanException
	 */
	public boolean acquireNextImage() throws VulkanException {
		
		if (images == null)
			update();
		
		// Checks whether there are free images
//		if (renderImageIndices.size() + busyFrames.size() == images.length)
//			return false;
		if (workingImages >= images.length)
			return false;
		workingImages++;
		
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
		if (renderImageIndices.size() == 0)
			return false;
		
		Integer imageIndex = renderImageIndices.remove(0);	
		
		if (renderCompleteSemaphores[imageIndex] != VK_NULL_HANDLE)
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
		if (busyFrames.size() == 0)
			return false;
		Integer imageIndex = busyFrames.get(0);
		

		if (vkGetFenceStatus(device, workDoneFences[imageIndex]) != VK_SUCCESS)
			return false;
		imageIndex = busyFrames.remove(0);
		
		pSignalSemaphores.put(0, renderCompleteSemaphores[imageIndex]);
		
		presentInfo.pImageIndices().put(0, imageIndex);
		presentInfo.pWaitSemaphores(pSignalSemaphores);
		
		int err = vkQueuePresentKHR(queue, presentInfo);
		validate(err, "Failed to present image!");
		
		//TODO remove
		workingImages--;
		
		return true;
	}
	
	public void destroy() {
		//TODO: Free resources
	}
}
