package core.rendering;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;
import static core.result.VulkanResult.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

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
	
	public abstract void render();
	
}
