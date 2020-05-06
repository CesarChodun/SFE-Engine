package com.sfengine.core.rendering.factories;

import com.sfengine.core.rendering.Window;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

/**
 * Class for swapchain creation.
 *
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public interface SwapchainFactory {

    /**
     * Provides the create info for the swapchain.
     *
     * @param window The targeted window.
     * @param oldSwapchain The old swapchain(VK_NULL_HANDLE if no swapchains were created).
     * @return Create info for the swapchain.
     */
    public VkSwapchainCreateInfoKHR getCreateInfo(Window window, long oldSwapchain);

    /**
     * Destroys the create info created by this class.
     *
     * @param info The create info.
     */
    public void destroyInfo(VkSwapchainCreateInfoKHR info);
}
