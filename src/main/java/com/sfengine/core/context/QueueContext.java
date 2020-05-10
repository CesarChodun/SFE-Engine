package com.sfengine.core.context;

import org.lwjgl.vulkan.VkQueue;

public interface QueueContext extends Context {

    VkQueue getQueue();

}
