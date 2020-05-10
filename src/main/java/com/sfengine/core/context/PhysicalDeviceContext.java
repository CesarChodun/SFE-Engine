package com.sfengine.core.context;

import org.lwjgl.vulkan.VkPhysicalDevice;

public interface PhysicalDeviceContext extends Context {

    VkPhysicalDevice getPhysicalDevice();
}
