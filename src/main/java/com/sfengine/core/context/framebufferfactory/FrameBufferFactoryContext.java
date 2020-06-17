package com.sfengine.core.context.framebufferfactory;

import com.sfengine.core.context.Context;
import com.sfengine.core.rendering.recording.BasicAttachemntSet;

public interface FrameBufferFactoryContext extends Context {

    long[] createFrameBuffers(BasicAttachemntSet attachemntSet);

    void destroyFrameBuffers(long[] frameBuffers);

    @Override
    default String getFactoryIdentifier() {
        return FrameBufferFactoryContextFactory.CONTEXT_IDENTIFIER;
    }
}
