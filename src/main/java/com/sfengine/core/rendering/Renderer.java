package com.sfengine.core.rendering;

import com.sfengine.core.engine.EngineTask;
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.synchronization.Dependable;

public interface Renderer extends Destroyable, Dependable, Runnable {

    /**
     * Recreates swapchain and related resources.
     */
    void update();

    long getSwapchain();

}
