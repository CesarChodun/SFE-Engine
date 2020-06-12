package com.sfengine.core.synchronization.VkFence;

import com.sfengine.core.synchronization.Dependable;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.lwjgl.vulkan.VkDevice;

import static org.lwjgl.vulkan.VK10.*;

public class VkFenceWrapper implements Dependable {

    private final DependencyFence dep = new DependencyFence();
    private VkDevice device;
    private long vkFence;

    protected VkFenceWrapper(VkDevice device, long vkFence) {
        this.device = device;

        if (vkFence == VK_NULL_HANDLE)
            throw new AssertionError("Wrong fence handle.");
        this.vkFence = vkFence;
    }

    public long getFence() {
        return vkFence;
    }

    public boolean check() {
        if (vkGetFenceStatus(device, vkFence) == VK_SUCCESS) {
            dep.release();
            return true;
        }

        return false;
    }

    @Override
    public Dependency getDependency() {
        return dep;
    }
}
