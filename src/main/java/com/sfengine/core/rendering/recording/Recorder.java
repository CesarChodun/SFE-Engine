package com.sfengine.core.rendering.recording;

import com.sfengine.core.synchronization.Dependable;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.Map;

public interface Recorder extends Dependable {

    void record(long... frameBuffers);

    Map<Long, VkCommandBuffer> getCMDs();

}
