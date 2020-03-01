package core.rendering;

import static core.Util.*;
import static core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import static org.lwjgl.vulkan.VK10.*;

import core.result.VulkanException;
import core.result.VulkanResult;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

/**
 * Class for utility methods for rendering tasks.
 *
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public class RenderUtil {

    public static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    /**
     * Creates image views(swapchain <b>must</b> be created before this call).
     *
     * @param device
     * @param colorAttachmentView
     */
    public static long[] createImageViews(
            VkDevice device, VkImageViewCreateInfo colorAttachmentView, long... images)
            throws VulkanException {

        long[] imageViews = new long[images.length];

        LongBuffer pView = memAllocLong(1);
        for (int i = 0; i < images.length; i++) {
            colorAttachmentView.image(images[i]);
            int err = vkCreateImageView(device, colorAttachmentView, null, pView);
            validate(err, "Failed to create image view.");

            imageViews[i] = pView.get(0);
        }

        memFree(pView);

        return imageViews;
    }

    /**
     * Destroys swapchain image views. <b>Note</b> when creating new swapchain image views from the
     * old one are deleted automatically.
     *
     * @param device The Vulkan device.
     * @param imageViews The image views to be destroyed.
     */
    public static void destroyImageViews(VkDevice device, long... imageViews) {
        for (int i = 0; i < imageViews.length; i++) vkDestroyImageView(device, imageViews[i], null);
    }

    /**
     * Obtains queue from logical device.
     *
     * @param device
     * @param queueFamilyIndex index of the current queue family.
     * @param queueIndex if only queue family capabilities are considered queue index should equal
     *     <b>0</b>.
     * @return
     */
    public static VkQueue getDeviceQueue(VkDevice device, int queueFamilyIndex, int queueIndex) {
        PointerBuffer pQueue = memAllocPointer(1);
        vkGetDeviceQueue(device, queueFamilyIndex, queueIndex, pQueue);
        long queueHandle = pQueue.get(0);
        memFree(pQueue);
        return new VkQueue(queueHandle, device);
    }

    /**
     * Creates <code><b><i>LogicalDevice</i></b></code> with given parameters.
     *
     * @param physicalDevice <b>must</b> be a valid <code><b><i>PhysicalDevice</i></b></code>.
     * @param queueFamilyIndex index of the current queue family.
     * @param queuePriorities buffer of queue priorities.
     * @param layers layers to be enabled.
     * @param extensions required extensions.
     * @return
     *     <p>Valid LogicalDevice.
     * @throws VulkanException when device creation process fails.
     */
    public static VkDevice createLogicalDevice(
            VkPhysicalDevice physicalDevice,
            int queueFamilyIndex,
            FloatBuffer queuePriorities,
            int flags,
            @Nullable Collection<String> layers,
            @Nullable Collection<String> extensions)
            throws VulkanException {

        ByteBuffer[] layersBuff = null;
        if (layers != null) layersBuff = makeByteBuffers(layers);

        PointerBuffer pExtensions = null;
        ByteBuffer[] extensionsBuff = null;

        if (extensions != null) {
            extensionsBuff = makeByteBuffers(extensions);
            pExtensions = makePointer(extensionsBuff);
        }

        VkDevice device =
                createLogicalDevice(
                        physicalDevice,
                        queueFamilyIndex,
                        queuePriorities,
                        flags,
                        layersBuff,
                        pExtensions);

        memFree(pExtensions);
        if (extensionsBuff != null)
            for (int i = 0; i < extensionsBuff.length; i++) memFree(extensionsBuff[i]);

        if (layersBuff != null) for (int i = 0; i < layersBuff.length; i++) memFree(layersBuff[i]);

        return device;
    }

    /**
     * Creates <code><b><i>LogicalDevice</i></b></code> with given parameters.
     *
     * @param dev - <b>Must</b> be a valid <code><b><i>PhysicalDevice</i></b></code>.
     * @param queueFamilyIndex index of the current queue family.
     * @param queuePriorities buffer of queue priorities.
     * @param layers - Layers to be enabled.
     * @param extensions - Needed extensions.
     * @return
     *     <p>Valid LogicalDevice.
     * @throws VulkanException when device creation process fails.
     */
    public static VkDevice createLogicalDevice(
            VkPhysicalDevice dev,
            int queueFamilyIndex,
            FloatBuffer queuePriorities,
            int flags,
            @Nullable ByteBuffer[] layers,
            @Nullable PointerBuffer extensions)
            throws VulkanException {
        VkDevice out = null;

        PointerBuffer ppEnabledLayerNames = (layers == null) ? null : makePointer(layers);

        VkDeviceQueueCreateInfo.Buffer deviceQueueCreateInfo =
                VkDeviceQueueCreateInfo.calloc(1)
                        .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                        .pNext(NULL)
                        .queueFamilyIndex(queueFamilyIndex)
                        .pQueuePriorities(queuePriorities);

        VkDeviceCreateInfo deviceCreateInfo =
                VkDeviceCreateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                        .pNext(NULL)
                        .flags(flags)
                        .pQueueCreateInfos(deviceQueueCreateInfo)
                        .ppEnabledLayerNames(ppEnabledLayerNames)
                        .ppEnabledExtensionNames(extensions);

        PointerBuffer pdev = memAllocPointer(1);
        int err = vkCreateDevice(dev, deviceCreateInfo, null, pdev);
        validate(err, "Failed to create logical device!");

        long ldev = pdev.get(0);
        out = new VkDevice(ldev, dev, deviceCreateInfo);

        deviceCreateInfo.free();
        deviceQueueCreateInfo.free();

        memFree(queuePriorities);
        memFree(ppEnabledLayerNames);
        memFree(pdev);

        return out;
    }

    /**
     * Returns memory type that meets requirements.
     *
     * @param device <b>must</b> be a valid physical device.
     * @param bits interesting indices.
     * @param properties properties that memory type should meet.
     * @param typeIndex integer buffer for returned value.
     * @return information about successfulness of the operation(true - success, false - fail).
     */
    public static boolean getMemoryType(
            VkPhysicalDevice device, int bits, int properties, IntBuffer typeIndex) {
        VkPhysicalDeviceMemoryProperties memoryProperties =
                VkPhysicalDeviceMemoryProperties.calloc();
        vkGetPhysicalDeviceMemoryProperties(device, memoryProperties);

        boolean ret = false;

        for (int i = 0; i < 32; i++)
            if ((bits & (1 << i)) > 0)
                if ((memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                    typeIndex.put(0, i);
                    ret = true;
                    break;
                }

        memoryProperties.free();
        return ret;
    }

    /**
     * Creates new command pool.
     *
     * @param device valid vulkan device
     * @param queueFamilyIndex designates a queue family as described in section Queue Family
     *     Properties. All command buffers allocated from this command pool must be submitted on
     *     queues from the same queue family.
     * @param flags must be a valid combination of VkCommandPoolCreateFlagBits values
     * @return Command pool handle.
     * @throws VulkanException When command pool creation fails
     */
    public static long createCommandPool(VkDevice device, int queueFamilyIndex, int flags)
            throws VulkanException {
        VkCommandPoolCreateInfo createInfo =
                VkCommandPoolCreateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                        .pNext(NULL)
                        .flags(flags)
                        .queueFamilyIndex(queueFamilyIndex);

        LongBuffer pCommandPool = memAllocLong(1);
        int err = vkCreateCommandPool(device, createInfo, null, pCommandPool);
        validate(err, "Failed to create command pool!");

        long out = pCommandPool.get(0);

        memFree(pCommandPool);
        createInfo.free();

        return out;
    }

    /**
     * Creeates framebuffers. Has to be invoken on the main thread!
     *
     * @param device vulkan device
     * @param renderPass current render pass
     * @param imageViews image views
     * @param attachments a list of attachments
     * @param width frame buffer width(must be a positive integer)
     * @param height frame buffer height(must be a positive integer)
     * @return a list of frame buffers binded to corresponding images
     * @throws VulkanException when there was a problem with frame buffers creation process
     */
    public static long[] createFramebuffers(
            VkDevice device,
            long renderPass,
            long[] imageViews,
            long[] attachments,
            int width,
            int height)
            throws VulkanException {

        long[] frameBuffers;

        LongBuffer attachmentsBuffer = memAllocLong(attachments.length);
        for (int i = 0; i < attachments.length; i++) attachmentsBuffer.put(i, attachments[i]);

        VkFramebufferCreateInfo frameBufferCreateInfo =
                VkFramebufferCreateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                        .pNext(NULL)
                        .flags(0)
                        .renderPass(renderPass)
                        .pAttachments(attachmentsBuffer)
                        .width(width)
                        .height(height)
                        .layers(1);

        frameBuffers = new long[imageViews.length];
        LongBuffer pFramebuffer = memAllocLong(1);

        for (int i = 0; i < imageViews.length; i++) {
            pFramebuffer.put(0, imageViews[i]);
            int err = vkCreateFramebuffer(device, frameBufferCreateInfo, null, pFramebuffer);
            long framebuffer = pFramebuffer.get(0);
            validate(err, "Failed to create frame buffer!");
            frameBuffers[i] = framebuffer;
        }

        memFree(pFramebuffer);
        memFree(attachmentsBuffer);
        frameBufferCreateInfo.free();

        return frameBuffers;
    }

    /**
     * Creates fence.
     *
     * @param device - Logical device
     * @param ci - Fence create info
     * @param alloc - Allocation callbacks
     * @return - Handle to the fence
     * @throws VulkanException
     */
    public static long createFence(
            VkDevice device, VkFenceCreateInfo ci, VkAllocationCallbacks alloc)
            throws VulkanException {
        LongBuffer longBuffer = memAllocLong(1);
        int err = vkCreateFence(device, ci, null, longBuffer);
        validate(err, "Failed to acquire semaphore!");

        memFree(longBuffer);

        return longBuffer.get(0);
    }

    /**
     * Creates semaphore.
     *
     * @param device - Logical device
     * @param ci - Semaphore create info
     * @param alloc - Allocation callbacks
     * @return - Handle to the semaphore
     * @throws VulkanException
     */
    public static long createSemaphore(
            VkDevice device, VkSemaphoreCreateInfo ci, VkAllocationCallbacks alloc)
            throws VulkanException {
        LongBuffer longBuffer = memAllocLong(1);
        int err = vkCreateSemaphore(device, ci, alloc, longBuffer);
        validate(err, "Failed to acquire semaphore!");

        memFree(longBuffer);

        return longBuffer.get(0);
    }

    /**
     * Obtains available surface extensions.
     *
     * @param physicalDevice - Physical device to obtain properties from.
     * @param surface - Surface handle.
     * @return - Buffer with <b><i><code>VkSurfaceFormatKHR</code></i></b>.
     * @throws VulkanException
     * @see VkSurfaceFormatKHR
     */
    public static VkSurfaceFormatKHR.Buffer listSurfaceFormats(
            VkPhysicalDevice physicalDevice, long surface) throws VulkanException {
        IntBuffer pCount = memAllocInt(1);
        int err = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pCount, null);
        validate(err, "Failed to enumerate surface formats!");
        int count = pCount.get(0);

        VkSurfaceFormatKHR.Buffer out = VkSurfaceFormatKHR.calloc(count);
        err = vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, pCount, out);
        memFree(pCount);
        validate(err, "Failed to obtain surface formats!");

        return out;
    }

    /**
     * Obtains desired color format if possible.
     *
     * @param physicalDevice - Physical device to obtain informations from.
     * @param surface - Surface handle.
     * @param first - Index to start iterating from. Most likely <b>0</b>.
     * @param desiredFormat - Most suitable color format.
     * @param desiredColorSpace - Most suitable color space.
     * @return - Returns <b><i><code>ColorFormatAndSpace</code></i></b> that support
     *     <b>desiredColorFormat</b> if possible. Otherwise <b>null</b>.
     * @throws VulkanException
     */
    public static ColorFormatAndSpace getNextColorFormatAndSpace(
            int first,
            VkPhysicalDevice physicalDevice,
            long surface,
            int desiredFormat,
            int desiredColorSpace)
            throws VulkanException {
        VkSurfaceFormatKHR.Buffer formats = listSurfaceFormats(physicalDevice, surface);
        ColorFormatAndSpace out = null;
        int size = formats.remaining();

        if (size == 1 && formats.get(0).format() == VK_FORMAT_UNDEFINED)
            return new ColorFormatAndSpace(
                    desiredFormat,
                    desiredColorSpace == -1 ? formats.get(0).colorSpace() : desiredColorSpace);

        for (int i = first; i < size; i++)
            if (formats.get(i).format() == desiredFormat || desiredFormat == -1) {
                out = new ColorFormatAndSpace(formats.get(i).format(), formats.get(i).colorSpace());

                if (formats.get(i).colorSpace() == desiredColorSpace || desiredColorSpace == -1)
                    break;
            }

        formats.free();

        return out;
    }

    /**
     * Gets current surface present modes.
     *
     * @param physicalDevice
     * @param surface
     * @return
     * @throws VulkanException
     */
    public static IntBuffer getSurfacePresentModes(VkPhysicalDevice physicalDevice, long surface)
            throws VulkanException {
        IntBuffer pCount = memAllocInt(1);
        int err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pCount, null);
        validate(err, "Failed to obtain present mode count!");
        int count = pCount.get(0);

        IntBuffer modes = memAllocInt(count);
        err = vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, surface, pCount, modes);
        validate(err, "Failed to obtain surface present modes!");

        memFree(pCount);
        return modes;
    }

    /**
     * Obtains <b><i><code>VkSurfaceCapabilitiesKHR</code></i></b>.
     *
     * @param physicalDevice
     * @param surface
     * @return Surface capabilities.
     * @throws VulkanException when failed to obtain surface capabilities.
     * @see org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR
     */
    public static VkSurfaceCapabilitiesKHR getSurfaceCapabilities(
            VkPhysicalDevice physicalDevice, long surface) throws VulkanException {
        VkSurfaceCapabilitiesKHR pSurfaceCapabilities = VkSurfaceCapabilitiesKHR.calloc();
        int err =
                vkGetPhysicalDeviceSurfaceCapabilitiesKHR(
                        physicalDevice, surface, pSurfaceCapabilities);
        validate(err, "Failed to obtain surface capabilities!");

        return pSurfaceCapabilities;
    }

    /**
     * Creates VkInsatnce.
     *
     * @param appInfo Application info.
     * @param validationLayers Names of the validation layers that should be enabled.
     * @param extensionNames Names of the vulkan extensions that should be enabled.
     * @return
     *     <p>VkInstance
     * @throws VulkanException If there was a problem with Instance creation process.
     * @see org.lwjgl.vulkan.VkInstance
     * @see org.lwjgl.vulkan.VkApplicationInfo
     */
    public static VkInstance createInstance(
            VkApplicationInfo appInfo,
            Collection<String> validationLayers,
            Collection<String> extensionNames)
            throws VulkanException {

        // Logger info
        StringBuilder logString = new StringBuilder();
        logString.append("\nCreating an instance with folowing validation layers:\n");

        // Create ByteBuffer array with names of validation layers.
        ByteBuffer[] layers = new ByteBuffer[validationLayers.size()];
        int pos = 0;
        for (String layerName : validationLayers) {
            layers[pos++] = memUTF8(layerName);
            logString.append("\t" + layerName + "\n");
        }

        // Create pointer buffer of the enabled layers.
        PointerBuffer ppEnabledLayerNames = memAllocPointer(layers.length);
        for (int i = 0; i < layers.length; i++) ppEnabledLayerNames.put(layers[i]);
        ppEnabledLayerNames.flip();

        // Logger info about extensions
        logString.append("\nAnd folowing extensions:\n");

        // Create extension name buffers
        ByteBuffer[] extensions = new ByteBuffer[extensionNames.size()];
        pos = 0;
        for (String name : extensionNames) {
            extensions[pos++] = memUTF8(name);
            logString.append("\t" + name + "\n");
        }

        // Create pointer buffer to extension name buffers
        PointerBuffer ppExtensions = memAllocPointer(extensions.length);
        for (int i = 0; i < extensions.length; i++) ppExtensions.put(extensions[i]);
        ppExtensions.flip();

        VkInstanceCreateInfo instanceCreateInfo =
                VkInstanceCreateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                        .pNext(NULL)
                        .pApplicationInfo(appInfo)
                        .ppEnabledExtensionNames(ppExtensions)
                        .ppEnabledLayerNames(ppEnabledLayerNames);

        PointerBuffer inst = memAllocPointer(1);
        int err = vkCreateInstance(instanceCreateInfo, null, inst);
        VulkanResult.validate(err, "Failed to create Vulkan Instance");

        long instanceAdr = inst.get(0);
        VkInstance out = new VkInstance(instanceAdr, instanceCreateInfo);

        for (ByteBuffer buff : layers) memFree(buff);
        for (ByteBuffer buff : extensions) memFree(buff);

        memFree(inst);
        memFree(ppExtensions);
        memFree(ppEnabledLayerNames);

        instanceCreateInfo.free();

        logger.log(Level.CONFIG, logString.toString());

        return out;
    }

    /**
     * Lists available validation layers.
     *
     * @return available vulkan validation layers
     * @throws VulkanException when failed to list validation layers.
     * @see org.lwjgl.vulkan.VkLayerProperties
     */
    public static VkLayerProperties[] listAvailableValidationLayers() throws VulkanException {
        VkLayerProperties.Buffer validationLayers;

        IntBuffer propsCount = memAllocInt(1);
        int err = vkEnumerateInstanceLayerProperties(propsCount, null);
        validate(err, "Failed to obtain the number of vulkan validation layers!");
        int validationLayersCount = propsCount.get(0);

        validationLayers = VkLayerProperties.calloc(validationLayersCount);
        err = vkEnumerateInstanceLayerProperties(propsCount, validationLayers);
        validate(err, "Failed to enumerate vulkan validation layers!");

        memFree(propsCount);

        VkLayerProperties[] props = new VkLayerProperties[validationLayersCount];
        for (int i = 0; i < validationLayersCount; i++) props[i] = validationLayers.get(i);

        validationLayers.free();
        return props;
    }

    /**
     * Lists available instance extensions.
     *
     * @return available instance extensions.
     * @throws VulkanException when extension enumeration fails.
     * @see org.lwjgl.vulkan.VkExtensionProperties
     */
    public static VkExtensionProperties[] listAvailableExtensions() throws VulkanException {
        VkExtensionProperties.Buffer extensions;

        int[] pPropertyCount = new int[1];
        int err = vkEnumerateInstanceExtensionProperties((CharSequence) null, pPropertyCount, null);
        validate(err, "Failed to enumerate instance extension properties!");

        extensions = VkExtensionProperties.calloc(pPropertyCount[0]);
        err =
                vkEnumerateInstanceExtensionProperties(
                        (CharSequence) null, pPropertyCount, extensions);
        validate(err, "Failed to enumerate instance extension properties!");

        VkExtensionProperties[] out = new VkExtensionProperties[pPropertyCount[0]];
        for (int i = 0; i < pPropertyCount[0]; i++) out[i] = extensions.get(i);

        extensions.free();

        return out;
    }

    /**
     * Enumerate available physical devices.
     *
     * <p><b>Note:</b> This method should be invoked only once.
     *
     * @param instance Vulkan instance.
     * @throws VulkanException when the device enumeration process failed.
     */
    public static VkPhysicalDevice[] enumeratePhysicalDevices(VkInstance instance)
            throws VulkanException {
        IntBuffer dev_count = memAllocInt(1);
        int err = vkEnumeratePhysicalDevices(instance, dev_count, null);
        validate(err, "Could not enumerate physical devices!");

        int devCount = dev_count.get(0);
        PointerBuffer pDevices = memAllocPointer(devCount);

        err = vkEnumeratePhysicalDevices(instance, dev_count, pDevices);
        validate(err, "Could not enumerate physical devices!");

        VkPhysicalDevice[] devices = new VkPhysicalDevice[devCount];
        for (int i = 0; i < devCount; i++)
            devices[i] = new VkPhysicalDevice(pDevices.get(i), instance);

        memFree(dev_count);
        memFree(pDevices);

        return devices;
    }
}
