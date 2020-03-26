package demos.util;

import static core.rendering.RenderUtil.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import core.rendering.ColorFormatAndSpace;
import core.rendering.Window;
import core.rendering.factories.SwapchainFactory;
import core.result.VulkanException;
import java.nio.IntBuffer;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

public class BasicSwapchainFactory implements SwapchainFactory {

    private static final int[] PRESENT_MODE_HIERARCHY = {
        VK_PRESENT_MODE_MAILBOX_KHR, VK_PRESENT_MODE_FIFO_KHR
    };

    private VkPhysicalDevice physicalDevice;
    private ColorFormatAndSpace colorFormatAndSpace;

    private int graphicsQueueFamilyIndex, presentQueueFamilyIndex;

    public BasicSwapchainFactory(
            VkPhysicalDevice physicalDevice, ColorFormatAndSpace colorFormatAndSpace) {
        this.physicalDevice = physicalDevice;
        this.colorFormatAndSpace = colorFormatAndSpace;
    }

    /**
     * Gets resolution range.
     *
     * @param capabilities - Surface capabilities.
     * @return
     */
    protected static int[] getSwapchainResolutionRange(VkSurfaceCapabilitiesKHR capabilities) {
        int[] out = new int[4];

        out[0] = capabilities.minImageExtent().width();
        out[1] = capabilities.minImageExtent().height();
        out[2] = capabilities.maxImageExtent().width();
        out[3] = capabilities.maxImageExtent().height();

        return out;
    }

    /**
     * Outputs best supported surface present mode.
     *
     * @param physicalDevice
     * @param surface
     * @param presentModeHierarchy
     * @return present mode identifier.
     * @throws VulkanException
     */
    private static Integer getBestPresentMode(
            VkPhysicalDevice physicalDevice, long surface, int[] presentModeHierarchy)
            throws VulkanException {
        IntBuffer modes = getSurfacePresentModes(physicalDevice, surface);
        int bsize = modes.remaining();
        int hsize = presentModeHierarchy.length;

        for (int i = 0; i < hsize; i++) {
            for (int j = 0; j < bsize; j++) {
                if (modes.get(j) == presentModeHierarchy[i]) {
                    Integer out = modes.get(j);
                    memFree(modes);
                    return out;
                }
            }
        }

        memFree(modes);

        return null;
    }

    @Override
    public VkSwapchainCreateInfoKHR getCreateInfo(Window window, long oldSwapchain) {
        final int width = (int) (window.getWidth());
        final int height = (int) (window.getHeight());

        VkSurfaceCapabilitiesKHR caps;
        try {
            caps = getSurfaceCapabilities(physicalDevice, window.getSurface());
        } catch (VulkanException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to obtain surface capabilities!");
        }

        //        if(!checkResolution(this.width, this.height, getSwapchainResolutionRange(caps))) {
        //            int[] range = getSwapchainResolutionRange(caps);
        //            throw new IllegalStateException("Swapchain width and/or height are outside
        // surface specific range. Current size: width = "
        //                    + this.width + ", height = " + this.height + ". While the range is:
        // width e <" + range[0] + "; " + range[2] + ">,"
        //                    + "height e <" + range[1] + "; " + range[3] +  ">.");
        //        }

        // Swapchain presentation mode:
        int swapchainPresentMode;
        try {
            swapchainPresentMode =
                    getBestPresentMode(physicalDevice, window.getSurface(), PRESENT_MODE_HIERARCHY);
        } catch (VulkanException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to obtain adequate present mode!");
        }
        if (swapchainPresentMode == -1) {
            throw new AssertionError("Failed to locate any suitable mode!");
        }

        // Triple buffering:
        int imageCount = caps.minImageCount();
        if (imageCount <= 1) {
            imageCount++;
        }
        if (caps.maxImageCount() > 0 && imageCount > caps.maxImageCount()) {
            imageCount = caps.maxImageCount();
        }

        // Transform
        int transform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
        if ((caps.supportedTransforms() & transform) == 0) {
            transform = caps.currentTransform();
        }

        // Create info for new swapchain
        VkSwapchainCreateInfoKHR createInfo =
                VkSwapchainCreateInfoKHR.calloc()
                        .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                        .pNext(NULL)
                        .surface(window.getSurface())
                        .minImageCount(imageCount)
                        .imageFormat(colorFormatAndSpace.colorFormat)
                        .imageColorSpace(colorFormatAndSpace.colorSpace)
                        .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                        .preTransform(transform)
                        .imageArrayLayers(1)
                        .imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
                        .pQueueFamilyIndices(null)
                        .presentMode(swapchainPresentMode)
                        .oldSwapchain(oldSwapchain)
                        .clipped(true)
                        .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);

        createInfo.imageExtent().width(width).height(height);

        // Share images between two queues.
        if (graphicsQueueFamilyIndex != presentQueueFamilyIndex) { // TODO: Check if valid
            IntBuffer buf = memAllocInt(2);
            buf.put(graphicsQueueFamilyIndex);
            buf.put(presentQueueFamilyIndex);
            buf.flip();
            createInfo.pQueueFamilyIndices(buf);
            createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
            memFree(buf);
        }

        caps.free();

        return createInfo;
    }

    @Override
    public void destroyInfo(VkSwapchainCreateInfoKHR info) {
        info.free();
    }
}
