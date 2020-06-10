package com.sfengine.components.contexts.swapchain;

import com.sfengine.components.window.CFrame;
import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.context.swapchain.SwapchainContext;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.properties.PropertyDictionary;
import com.sfengine.core.rendering.Window;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.locks.Lock;

import static com.sfengine.core.rendering.RenderUtil.getSurfaceCapabilities;
import static com.sfengine.core.rendering.RenderUtil.getSurfacePresentModes;
import static com.sfengine.core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

public class BasicSwapchainContext implements SwapchainContext {

    private static final int[] PRESENT_MODE_HIERARCHY = {
            VK_PRESENT_MODE_MAILBOX_KHR, VK_PRESENT_MODE_FIFO_KHR
    };

    private final Engine engine = EngineFactory.getEngine();

    private final VkSwapchainCreateInfoKHR info = VkSwapchainCreateInfoKHR.calloc();
    private volatile long handle;

    private volatile String name;
    private volatile CFrame frame;

    private volatile ContextDictionary dict;
    private volatile PropertyDictionary pdict;

    private final DependencyFence created = new DependencyFence();

    protected BasicSwapchainContext(String name, ContextDictionary dict, PropertyDictionary pdict, CFrame frame) {
        this.name = name;
        this.dict = dict;
        this.pdict = pdict;
        this.frame = frame;


        engine.addTask(() -> {
            init(frame.getWindow());
        },
                frame.getDependency(),
                ContextUtil.getDevice(dict).getDependency(),
                ContextUtil.getPhysicalDevice(dict).getDependency());
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
        final int hsize = presentModeHierarchy.length;
        final int bsize = modes.remaining();

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

    private void init(Window window) {
        final VkDevice device = ContextUtil.getDevice(dict).getDevice();
        final VkPhysicalDevice physicalDevice = ContextUtil.getPhysicalDevice(dict).getPhysicalDevice();

        int width = (int) (window.getWidth());
        int height = (int) (window.getHeight());

        VkSurfaceCapabilitiesKHR caps;
        try {
            caps = getSurfaceCapabilities(physicalDevice, window.getSurface());
        } catch (VulkanException e) {
            throw new AssertionError("Failed to obtain surface capabilities!", e);
        }

        // Clamp image extent
        width = Math.max(width, caps.minImageExtent().width());
        width = Math.min(width, caps.maxImageExtent().width());
        height = Math.max(height, caps.minImageExtent().height());
        height = Math.min(height, caps.maxImageExtent().height());

        // Swapchain presentation mode:
        Integer swapchainPresentMode;
        try {
            swapchainPresentMode =
                    getBestPresentMode(physicalDevice, window.getSurface(), PRESENT_MODE_HIERARCHY);
        } catch (VulkanException e) {
            throw new AssertionError("Failed to obtain adequate present mode!", e);
        }
        if (swapchainPresentMode == null) {
            throw new AssertionError("Failed to locate any suitable mode!");
        }

        // Triple buffering:
        int imageCount = caps.maxImageCount();

        // Transform
        int transform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
        if ((caps.supportedTransforms() & transform) == 0) {
            transform = caps.currentTransform();
        }

        // Create info for new swapchain
        info
                .sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                .pNext(NULL)
                .surface(window.getSurface())
                .minImageCount(imageCount)
                .imageFormat(pdict.get("colorFormat", Integer.class))
                .imageColorSpace(pdict.get("colorSpace", Integer.class))
                .imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                .preTransform(transform)
                .imageArrayLayers(1)
                .imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
                .pQueueFamilyIndices(null)
                .presentMode(swapchainPresentMode)
                .oldSwapchain(pdict.get("oldSwapchain", Long.class))
                .clipped(true)
                .compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);

        info.imageExtent().width(width).height(height);

        caps.free();

        if (pdict.get("oldSwapchain", Long.class) != VK_NULL_HANDLE)
            vkDestroySwapchainKHR(device, pdict.get("oldSwapchain", Long.class), null);

        LongBuffer pSwapchain = memAllocLong(1);
        int err = vkCreateSwapchainKHR(device, info, null, pSwapchain);
        try {
            validate(err, "Failed to create swapchain KHR.");
        } catch (VulkanException e) {
            throw new AssertionError("Swpachain context creation failed.", e);
        }
        handle = pSwapchain.get(0);
        memFree(pSwapchain);
    }

    @Override
    public long getHandle() {
        return handle;
    }

    @Override
    public int getWidth() {
        return info.imageExtent().width();
    }

    @Override
    public int getHeight() {
        return info.imageExtent().height();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Lock getLock() {
        return null;
    }

    @Override
    public Dependency getDependency() {
        return created;
    }

    @Override
    public void destroy() {
        info.free();
    }
}
