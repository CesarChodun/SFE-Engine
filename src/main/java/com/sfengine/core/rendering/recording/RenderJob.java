package com.sfengine.core.rendering.recording;

import org.lwjgl.vulkan.VkCommandBuffer;

public interface RenderJob {

    VkCommandBuffer getCMD();

    void performUpdate();

}
