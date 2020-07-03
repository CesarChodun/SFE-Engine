package com.sfengine.components.contexts.framebufferfactory;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextFactory;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.context.framebufferfactory.FrameBufferFactoryContext;
import com.sfengine.core.context.swapchain.SwapchainContext;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.rendering.recording.BasicAttachemntSet;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import java.nio.LongBuffer;
import java.util.concurrent.locks.Lock;

import static com.sfengine.core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

public class BasicFrameBufferFactoryContext implements FrameBufferFactoryContext {

    private final Engine engine = EngineFactory.getEngine();

    private volatile String name;

    private final VkFramebufferCreateInfo frameBufferCreateInfo = VkFramebufferCreateInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
            .pNext(NULL)
            .flags(0)
            .layers(1);

    private final DependencyFence created = new DependencyFence();
    private volatile ContextDictionary dict;

    BasicFrameBufferFactoryContext(String name, ContextDictionary dict, long renderPass) {
        this.name = name;
        this.dict = dict;
        this.frameBufferCreateInfo.renderPass(renderPass);

        engine.addTask(() -> {init();},
                ContextUtil.getDevice(dict).getDependency());
    }

    private void init() {
        created.release();
    }

    @Override
    public long[] createFrameBuffers(BasicAttachemntSet attachemntSet) {
        SwapchainContext swapchainContext = ContextUtil.getSwapchain(dict);

        int width = swapchainContext.info().imageExtent().width();
        int height = swapchainContext.info().imageExtent().height();

        frameBufferCreateInfo.width(width);
        frameBufferCreateInfo.height(height);

        VkDevice device = ContextUtil.getDevice(dict).getDevice();

        int frames = attachemntSet.framesCount();
        long[] frameBuffers = new long[frames];
        LongBuffer pFrameBuffer = memAllocLong(1);

        int attsCount = attachemntSet.getViews(0).length;
        LongBuffer attachments = memAllocLong(attsCount);
        frameBufferCreateInfo.pAttachments(attachments);

        for (int i = 0; i < frames; i++) {
            long[] atts = attachemntSet.getViews(i);
            for (int j = 0; j < atts.length; j++)
                attachments.put(j, atts[j]);

            int err = vkCreateFramebuffer(device, frameBufferCreateInfo, null, pFrameBuffer);
            long frameBuffer = pFrameBuffer.get(0);
            try {
                validate(err, "Failed to create frame buffer!");
            } catch (VulkanException e) {
                throw new AssertionError("Failed to create framebuffers.", e);
            }
            frameBuffers[i] = frameBuffer;
        }

        memFree(attachments);
        memFree(pFrameBuffer);

        return frameBuffers;
    }

    @Override
    public void destroyFrameBuffers(long[] frameBuffers) {
        VkDevice device = ContextUtil.getDevice(dict).getDevice();

        for (long frameBuffer : frameBuffers) {
            vkDestroyFramebuffer(device, frameBuffer, null);
        }
    }

    @Override
    public String getName() {
        return name;
    }




    @Override
    public Dependency getDependency() {
        return created;
    }
}
