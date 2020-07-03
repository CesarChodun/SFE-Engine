package com.sfengine.core.context.queue;

import com.sfengine.core.context.Context;
import org.lwjgl.vulkan.VkQueue;

public interface QueueContext extends Context {

    VkQueue getQueue();

    @Override
    default String getFactoryIdentifier() {
        return QueueContextFactory.CONTEXT_IDENTIFIER;
    }

}
