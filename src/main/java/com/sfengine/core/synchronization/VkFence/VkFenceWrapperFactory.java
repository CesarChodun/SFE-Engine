package com.sfengine.core.synchronization.VkFence;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.synchronization.VkFence.VkFenceWrapper;
import org.lwjgl.vulkan.VkFenceCreateInfo;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.VK10.*;
import static com.sfengine.core.result.VulkanResult.*;


public class VkFenceWrapperFactory {

    private static final VkFenceCreateInfo CREATE_INFO = VkFenceCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
            .pNext(NULL)
            .flags(0);

    private static VkFenceWrapper[] create(ContextDictionary dict, long... fences) {
        VkFenceWrapper[] out = new VkFenceWrapper[fences.length];

        int i = 0;
        for(long fence : fences)
            out[i++] = new VkFenceWrapper(ContextUtil.getDevice(dict).getDevice(), fence);

        return out;
    }

    private static void reset(ContextDictionary dict, long...fences) {
        vkResetFences(ContextUtil.getDevice(dict).getDevice(), fences);
    }

    private static VkFenceWrapper[] createMany(ContextDictionary dict, VkFenceCreateInfo ci, int count) throws VulkanException {
        long[] fences = new long[count];
        int err = vkCreateFence(ContextUtil.getDevice(dict).getDevice(), ci, null, fences);
        validate(err, "Failed to create fences.");

        return create(dict, fences);
    }

    public static VkFenceWrapper createWrapper(ContextDictionary dict, long fence) {
        return create(dict, fence)[0];
    }

    public static VkFenceWrapper recreateWrapper(ContextDictionary dict, VkFenceWrapper old) {
        reset(dict, old.getFence());
        return create(dict, old.getFence())[0];
    }

    public static VkFenceWrapper[] createWrapper(ContextDictionary dict, int count) {
        try {
            return createMany(dict, CREATE_INFO, count);
        } catch (VulkanException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to create fence wrappers.", e);
        }
    }

    public static VkFenceWrapper[] recreateWrapper(ContextDictionary dict, VkFenceWrapper... wrappers) {
        long[] handles = new long[wrappers.length];

        for(int i = 0; i < wrappers.length; i++)
            handles[i] = wrappers[i].getFence();

        reset(dict, handles);
        return create(dict, handles);
    }

}
