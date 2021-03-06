package com.sfengine.core.rendering.recording;

import org.lwjgl.vulkan.VkCommandBuffer;

/**
 * Class for command buffer recordable tasks.
 *
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public interface Recordable {

    /**
     * Records a set of instructions to the given command buffer.
     *
     * @param buffer
     */
    void record(VkCommandBuffer buffer, long framebuffer);
}
