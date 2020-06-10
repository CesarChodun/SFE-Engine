package com.sfengine.core.context.swapchain;

import com.sfengine.core.context.Context;
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.synchronization.Dependency;

import java.util.concurrent.locks.Lock;

public interface SwapchainContext extends Context, Destroyable {

    long getHandle();

    int getWidth();

    int getHeight();

    @Override
    default String getFactoryIdentifier() {
        return SwapchainContextFactory.CONTEXT_IDENTIFIER;
    }

}
