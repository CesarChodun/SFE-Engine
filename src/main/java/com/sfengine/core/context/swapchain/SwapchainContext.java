package com.sfengine.core.context.swapchain;

import com.sfengine.core.context.Context;
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.synchronization.Dependency;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import java.util.concurrent.locks.Lock;

public interface SwapchainContext extends Context, Destroyable {

    void recreate();

    long getHandle();

    VkSwapchainCreateInfoKHR info();

    long[] getFrameBuffers();

    @Override
    default String getFactoryIdentifier() {
        return SwapchainContextFactory.CONTEXT_IDENTIFIER;
    }

}
