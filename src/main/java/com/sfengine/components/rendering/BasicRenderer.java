package com.sfengine.components.rendering;

import com.sfengine.core.rendering.Renderer;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

public class BasicRenderer implements Renderer {

    private final DependencyFence created = new DependencyFence();
    private volatile boolean shouldStop = false;

    /** Queue submit info. */
    protected VkSubmitInfo submitInfo;
    /** KHR present info. */
    protected VkPresentInfoKHR presentInfo;

    /** Queue transferring the rendering work. */
    private VkQueue queue;

    @Override
    public void update() {

    }

    @Override
    public long getSwapchain() {
        return 0;
    }

    @Override
    public void destroy() {
        //TODO: free resources
    }

    @Override
    public Dependency getDependency() {
        return created;
    }

    @Override
    public void run() {
        while(!shouldStop) {

        }
    }

    public void stop() {
        shouldStop = true;
    }
}
