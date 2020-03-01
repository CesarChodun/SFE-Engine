package core.hardware;

import static core.result.VulkanResult.validate;
import static java.lang.Math.max;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.VK10.*;

import core.result.VulkanException;
import java.nio.IntBuffer;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

/**
 * Class with utilities for hardware managment.
 *
 * @author Cezary Chodun
 * @since 24.09.2019
 */
public class HardwareUtil {

    /**
     * Function for acquiring queue family index which meets requirements(<code>
     * <i><b>VkQueueFlagBits</b></i></code>). <b>Note:</b>
     *
     * <p>If <code><b><i>requiredSupport</i></b></code> equals to <b>0</b> and <code>
     * <b><i>first</i></b></code> is less than <code><b><i>queueFamilyCount</i></b></code> next
     * queue family will be returned.
     *
     * @param first First interesting queue index. By default <b>0</b>.
     * @param requiredSupport Required queue flag bits(<code><i><b>VkQueueFlagBits</b></i></code>).
     *     For example <code>VK_QUEUE_GRAPHICS_BIT</code>.<br>
     * @see org.lwjgl.vulkan.VK10#VK_QUEUE_GRAPHICS_BIT
     * @return If queue family has been found, the index of such family is returned. Otherwise
     *     <b>-1</b> is returned.
     */
    public static int getNextQueueFamilyIndex(
            int first, int requiredSupport, VkQueueFamilyProperties.Buffer queueFamilyProperties) {
        for (int i = max(0, first); i < queueFamilyProperties.remaining(); i++)
            if ((queueFamilyProperties.get(i).queueFlags() & requiredSupport) == requiredSupport)
                return i;
        return -1;
    }

    /**
     * Returns the next queue family that supports given surface.
     *
     * @param first First interesting queue index.
     * @param requiredFlags Required queue flag bits(<code><i><b>VkQueueFlagBits</b></i></code>).
     * @param surface Surface handle for compatibility check.
     * @param physicalDevice <b>Must</b> be a valid VkPhysicalDevice handle.
     * @param queueFamilyProperties Valid buffer of queue family properties.
     * @return First queue family index that meets all requirements or <b>-1</b> if no such queue
     *     exist.
     * @throws VulkanException when surface support query fails.
     */
    public static int getNextPresentQueueFamilyIndex(
            int first,
            int requiredFlags,
            long surface,
            VkPhysicalDevice physicalDevice,
            VkQueueFamilyProperties.Buffer queueFamilyProperties)
            throws VulkanException {
        IntBuffer supportPresent = memAllocInt(1);

        for (int i = first; i < queueFamilyProperties.remaining(); i++) {
            supportPresent.position(0);

            int err =
                    vkGetPhysicalDeviceSurfaceSupportKHR(
                            physicalDevice, i, surface, supportPresent);
            validate(err, "Failed to check physical device surface support!");

            int at = supportPresent.get(0);
            if ((queueFamilyProperties.get(i).queueFlags() & requiredFlags) == requiredFlags
                    && at == VK_TRUE) return i;
        }

        return -1;
    }

    /**
     * Creates a new buffer of queue family properties.
     *
     * @param physicalDevice <b>Must</b> be a valid pointer to VkPhysicalDevice.
     * @return A buffer of queue family properties. This buffer <b>should</b> be freed by the
     *     implementation.
     */
    public static VkQueueFamilyProperties.Buffer newQueueFamilyProperties(
            VkPhysicalDevice physicalDevice) {
        IntBuffer queue_family_count_buffer = memAllocInt(1);
        vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, queue_family_count_buffer, null);
        int queueFamilyCount = queue_family_count_buffer.get(0);

        VkQueueFamilyProperties.Buffer queueFamilyProperties =
                VkQueueFamilyProperties.calloc(queueFamilyCount);
        vkGetPhysicalDeviceQueueFamilyProperties(
                physicalDevice, queue_family_count_buffer, queueFamilyProperties);
        memFree(queue_family_count_buffer);

        return queueFamilyProperties;
    }

    /**
     * Creates a new VkPhysicalDeviceMemoryProperties object.
     *
     * @param physicalDevice <b>Must</b> be a valid pointer to VkPhysicalDevice.
     * @return A VkPhysicalDeviceMemoryProperties object. This object <b>should</b> be freed by the
     *     implementation.
     */
    public static VkPhysicalDeviceMemoryProperties newPhysicalDeviceMemoryProperties(
            VkPhysicalDevice physicalDevice) {
        VkPhysicalDeviceMemoryProperties memoryProperties =
                VkPhysicalDeviceMemoryProperties.calloc();
        vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties);

        return memoryProperties;
    }
}
