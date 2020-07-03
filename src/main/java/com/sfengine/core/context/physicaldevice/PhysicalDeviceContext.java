package com.sfengine.core.context.physicaldevice;

import com.sfengine.core.context.Context;
import org.lwjgl.vulkan.VkPhysicalDevice;

public interface PhysicalDeviceContext extends Context {

    /**
     * Returns the most up to date physical device available.
     *
     * @apiNote There is no guarantee that the physical device
     * will be available at the time of executing.
     * Please wait on the dependency fence object.
     * (<code>this.getDependency();</code>)
     *
     * @return The most up to date physical device or null
     * if there is no VkPhysicalDevice available at the time.
     */
    VkPhysicalDevice getPhysicalDevice();

    @Override
    default String getFactoryIdentifier() {
        return PhysicalDeviceContextFactory.CONTEXT_IDENTIFIER;
    }
}
