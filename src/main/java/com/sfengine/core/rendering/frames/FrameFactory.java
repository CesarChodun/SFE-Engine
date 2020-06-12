package com.sfengine.core.rendering.frames;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.swapchain.SwapchainContext;

public interface FrameFactory {

    void update(SwapchainContext swapchainContext);

    int lazyCount();

    Frame popFrame(int id);

    void releaseFrame(Frame frame);

}
