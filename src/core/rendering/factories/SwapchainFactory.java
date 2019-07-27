package core.rendering.factories;

import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import core.rendering.Window;

public interface SwapchainFactory {

	public VkSwapchainCreateInfoKHR getCreateInfo(Window window, long oldSwapchain);
	
}
