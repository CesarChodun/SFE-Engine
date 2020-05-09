package com.sfengine.core.context;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;

public interface DeviceContext extends Context {

    VkPhysicalDevice getPhysicalDevice();

    VkDevice getDevice();
}
