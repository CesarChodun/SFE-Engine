package com.sfengine.core.context.physicaldevice;

import com.sfengine.core.context.Context;
import org.lwjgl.vulkan.VkPhysicalDevice;

public interface PhysicalDeviceContext extends Context {

    VkPhysicalDevice getPhysicalDevice();
}
