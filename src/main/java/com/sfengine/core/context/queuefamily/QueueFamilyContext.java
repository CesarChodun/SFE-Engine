package com.sfengine.core.context.queuefamily;

import com.sfengine.core.context.Context;
import com.sfengine.core.context.queue.QueueContextFactory;

public interface QueueFamilyContext extends Context {

    int getQueueFamilyIndex();

    @Override
    default String getFactoryIdentifier() {
        return QueueContextFactory.CONTEXT_IDENTIFIER;
    }
}
