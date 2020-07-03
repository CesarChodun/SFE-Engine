package com.sfengine.core.context.device;

import com.sfengine.core.context.Context;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;

public interface DeviceContext extends Context {

    VkDevice getDevice();

    @Override
    default String getFactoryIdentifier() {
        return DeviceContextFactory.CONTEXT_IDENTIFIER;
    }
}
