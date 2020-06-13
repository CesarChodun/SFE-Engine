package com.sfengine.core.context.framebufferfactory;

import com.sfengine.core.context.Context;

public interface FrameBufferFactoryContext extends Context {

    long[] createFrameBuffers(long[] imageViews);

    void destroyFrameBuffers(long[] frameBuffers);

    @Override
    default String getFactoryIdentifier() {
        return FrameBufferFactoryContextFactory.CONTEXT_IDENTIFIER;
    }
}
