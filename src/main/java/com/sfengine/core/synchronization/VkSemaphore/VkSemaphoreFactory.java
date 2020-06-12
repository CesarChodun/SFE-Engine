package com.sfengine.core.synchronization.VkSemaphore;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.result.VulkanResult;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.*;

/**
 *
 * <b>Important!</b> All VK... factories must be invoked from the first thread!
 */
public class VkSemaphoreFactory {

    private static final VkSemaphoreCreateInfo basicCI = VkSemaphoreCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO)
            .pNext(NULL)
            .flags(0);

    private static LongBuffer handleBuffer = memAllocLong(1);

    public static long createSemaphore(VkDevice device) {
        int err = vkCreateSemaphore(device, basicCI, null, handleBuffer);
        VulkanResult.assertValidate(err, "Failed to acquire semaphore!");

        return handleBuffer.get(0);
    }

    public static long createSemaphore(ContextDictionary dict) {
        return createSemaphore(ContextUtil.getDevice(dict).getDevice());
    }

    public static long[] createSemaphores(VkDevice device, int count) {
        long[] out = new long[count];

        for (int i = 0; i < count; i++)
            out[i] = createSemaphore(device);

        return out;
    }

    public static long[] createSemaphores(ContextDictionary dict, int count) {
        return createSemaphores(ContextUtil.getDevice(dict).getDevice(), count);
    }

    public static void destroySemaphore(VkDevice device, long... semaphores) {
        for (long sem : semaphores)
            vkDestroySemaphore(device, sem, null);
    }

    public static void destroySemaphore(ContextDictionary dict, long... semaphores) {
        destroySemaphore(ContextUtil.getDevice(dict).getDevice(), semaphores);
    }
}
