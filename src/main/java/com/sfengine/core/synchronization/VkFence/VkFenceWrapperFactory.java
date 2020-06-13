package com.sfengine.core.synchronization.VkFence;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.synchronization.VkFence.VkFenceWrapper;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryUtil.*;
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
        if (fences.length == 0)
            throw new AssertionError("Trying to delete 0 fences.");
        LongBuffer lbuf = memAllocLong(fences.length);
        for (int i = 0; i < fences.length; i++)
            lbuf.put(fences[i]);
        lbuf.flip();
        VK10.vkResetFences(ContextUtil.getDevice(dict).getDevice(), lbuf);
        memFree(lbuf);
    }

    private static VkFenceWrapper[] createMany(ContextDictionary dict, VkFenceCreateInfo ci, int count) throws VulkanException {
        long[] fences = new long[count];
        LongBuffer lbuf = memAllocLong(1);

        int err;

        for (int i = 0; i < count; i++) {
            err = vkCreateFence(ContextUtil.getDevice(dict).getDevice(), ci, null, lbuf);
            validate(err, "Failed to create fences.");
            fences[i] = lbuf.get(0);
        }

//        memFree(lbuf);

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

    public static void destroy(VkDevice device, VkFenceWrapper... wrappers) {
        for (VkFenceWrapper fence : wrappers)
            vkDestroyFence(device, fence.getFence(), null);
    }

}
