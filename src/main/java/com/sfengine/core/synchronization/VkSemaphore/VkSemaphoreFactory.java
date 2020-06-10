package com.sfengine.core.synchronization.VkSemaphore;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.result.VulkanResult;
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

    public static long createSemaphore(ContextDictionary dict) {
        int err = vkCreateSemaphore(ContextUtil.getDevice(dict).getDevice(), basicCI, null, handleBuffer);
        VulkanResult.assertValidate(err, "Failed to acquire semaphore!");

        return handleBuffer.get(0);
    }

    public static void destroySemaphore(ContextDictionary dict, long sem) {
        vkDestroySemaphore(ContextUtil.getDevice(dict).getDevice(), sem, null);
    }
}
