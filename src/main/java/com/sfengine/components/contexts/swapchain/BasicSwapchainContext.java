package com.sfengine.components.contexts.swapchain;

import com.sfengine.components.pipeline.ImageViewCreateInfo;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.Application;
import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextFactoryProvider;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.context.framebufferfactory.FrameBufferFactoryContext;
import com.sfengine.core.context.swapchain.SwapchainContext;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.properties.PropertyDictionary;
import com.sfengine.core.rendering.ColorFormatAndSpace;
import com.sfengine.core.rendering.RenderUtil;
import com.sfengine.core.rendering.Window;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.result.VulkanResult;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private final Logger logger = Logger.getLogger(BasicSwapchainContext.class.getSimpleName());

    private final Engine engine = EngineFactory.getEngine();

    private final VkSwapchainCreateInfoKHR info = VkSwapchainCreateInfoKHR.calloc();
    private volatile long handle;

    private volatile String name;
    private volatile CFrame frame;

    private volatile long[] images;
    private volatile long[] imageViews;
    private volatile long[] frameBuffers;

    private volatile ContextDictionary dict;
    private volatile PropertyDictionary pdict;

    private final DependencyFence created = new DependencyFence();

    protected BasicSwapchainContext(String name, ContextDictionary dict, PropertyDictionary pdict, CFrame frame) {
        this.name = name;
        this.dict = dict;
        this.pdict = pdict;
        this.frame = frame;


        engine.addTask(() -> {
            init();
        },
                frame.getDependency(),
                ContextUtil.getDevice(dict).getDependency(),
                ContextUtil.getPhysicalDevice(dict).getDependency(),
                ContextUtil.getFrameBufferFactory(dict).getDependency());
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

    private void init() {

        recreate();
        created.release();

    }

    private void getColorAndSpace(VkDevice device, VkPhysicalDevice physicalDevice, PropertyDictionary pdict) {
        ColorFormatAndSpace colorFormat = new ColorFormatAndSpace(0, 0);
        try {
            colorFormat =
                    RenderUtil.getNextColorFormatAndSpace(
                            0,
                            physicalDevice,
                            frame.getWindow().getSurface(),
                            VK_FORMAT_B8G8R8A8_UNORM,
                            VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
        } catch (VulkanException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to choose color format and space.");
        }

        pdict.put("colorFormat", colorFormat.colorFormat);
        pdict.put("colorSpace", colorFormat.colorSpace);
    }

    @Override
    public void recreate() {
        final VkDevice device = ContextUtil.getDevice(dict).getDevice();
        final VkPhysicalDevice physicalDevice = ContextUtil.getPhysicalDevice(dict).getPhysicalDevice();
        final Window window = frame.getWindow();

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
        int imageCount = 2;
        imageCount = Math.min(imageCount, caps.maxImageCount());
        imageCount = Math.max(imageCount, caps.minImageCount());

        logger.log(Level.INFO, "Swapchain min images count:" + imageCount);

        // Transform
        int transform = VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR;
        if ((caps.supportedTransforms() & transform) == 0) {
            transform = caps.currentTransform();
        }

//        getColorAndSpace(device, physicalDevice, pdict);

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

        if (handle != VK_NULL_HANDLE) {
            info.oldSwapchain(handle);
        }

        LongBuffer pSwapchain = memAllocLong(1);
        int err = vkCreateSwapchainKHR(device, info, null, pSwapchain);
        try {
            validate(err, "Failed to create swapchain KHR.");
        } catch (VulkanException e) {
            throw new AssertionError("Swpachain context creation failed.", e);
        }

        if (pdict.get("oldSwapchain", Long.class) != VK_NULL_HANDLE)
            vkDestroySwapchainKHR(device, pdict.get("oldSwapchain", Long.class), null);
        if (handle != VK_NULL_HANDLE)
            vkDestroySwapchainKHR(device, handle, null);

        handle = pSwapchain.get(0);
        memFree(pSwapchain);

        createImageViews();

        created.release();
    }

    private void createImageViews() {
        final VkDevice device = ContextUtil.getDevice(dict).getDevice();
        final SwapchainContext swapchainContext = ContextUtil.getSwapchain(dict);
        FrameBufferFactoryContext fbFactoryContext = ContextUtil.getFrameBufferFactory(dict);

        // Extracting image handles from swapchain.
        IntBuffer pSwapchainImageCount = memAllocInt(1);
        int err = vkGetSwapchainImagesKHR(device, swapchainContext.getHandle(), pSwapchainImageCount, null);
        VulkanResult.assertValidate(err, "Failed to enumerate swapchain images!");

        int swapchainImageCount = pSwapchainImageCount.get(0);
        logger.log(Level.INFO, "Swapchain images count:" + swapchainImageCount);

        LongBuffer pSwapchainImages = memAllocLong(swapchainImageCount);
        err = vkGetSwapchainImagesKHR(device, swapchainContext.getHandle(), pSwapchainImageCount, pSwapchainImages);
        VulkanResult.assertValidate(err, "Failed to obtain swapchain images!");

        images = new long[swapchainImageCount];
        for (int i = 0; i < swapchainImageCount; i++) {
            images[i] = pSwapchainImages.get(i);
        }

        ImageViewCreateInfo ci = null;

        try {
            ci = new ImageViewCreateInfo(ContextFactoryProvider.getConfig(this, Application.getConfigAssets()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ci.getInfo().format(pdict.get("colorFormat", Integer.class));

        try {
            imageViews = RenderUtil.createImageViews(device, ci.getInfo(), images);
        } catch (VulkanException e) {
            throw new AssertionError("Failed to create image view create info.", e);
        }

        frameBuffers = fbFactoryContext.createFrameBuffers(imageViews);
    }

    @Override
    public long getHandle() {
        return handle;
    }

    @Override
    public VkSwapchainCreateInfoKHR info() {
        return info;
    }

    @Override
    public long[] getImages() {
        return images;
    }

    @Override
    public long[] getImageViews() {
        return imageViews;
    }

    @Override
    public long[] getFrameBuffers() {
        return frameBuffers;
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
