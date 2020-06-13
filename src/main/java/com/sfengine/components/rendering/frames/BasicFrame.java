package com.sfengine.components.rendering.frames;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.rendering.frames.Frame;
import com.sfengine.core.rendering.recording.RenderJob;
import com.sfengine.core.result.VulkanResult;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.VkFence.VkFenceSupervisor;
import com.sfengine.core.synchronization.VkFence.VkFenceWrapper;
import com.sfengine.core.synchronization.VkFence.VkFenceWrapperFactory;
import com.sfengine.core.synchronization.VkSemaphore.VkSemaphoreFactory;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSubmitInfo;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;

public class BasicFrame implements Frame {

    private volatile State state = State.WAITING;

    private RenderJob job;

    private volatile long renderCompleteSemaphore = VK_NULL_HANDLE;
    private volatile VkFenceWrapper renderCompleteFence;
    private volatile VkFenceSupervisor supervisor;

    private ContextDictionary dict;

    protected volatile VkSubmitInfo submitInfo;
    protected volatile LongBuffer pSignalSemaphores;
    protected volatile PointerBuffer pCommandBuffers;

    private volatile long frameBuffer;

    public BasicFrame(ContextDictionary dict,
                      VkFenceWrapper fence,
                      VkFenceSupervisor supervisor,
                      RenderJob job,
                      VkSubmitInfo submitInfo,
                      LongBuffer pSignalSemaphores,
                      PointerBuffer pCommandBuffers) {
        this.dict = dict;
        this.renderCompleteFence = fence;
        this.supervisor = supervisor;
        this.job = job;
        this.submitInfo = submitInfo;
        this.pSignalSemaphores = pSignalSemaphores;
        this.pCommandBuffers = pCommandBuffers;


    }

    private void submitToQueue(ContextDictionary dict, VkDevice device, VkQueue queue, LongBuffer pImageAcquireSemaphores) {
        job.performUpdate();
        state = State.SUBMITTED;

        if (renderCompleteSemaphore != VK_NULL_HANDLE) {
            VkSemaphoreFactory.destroySemaphore(device, renderCompleteSemaphore);
        }
        renderCompleteSemaphore = VkSemaphoreFactory.createSemaphore(device);

        pSignalSemaphores.put(0, renderCompleteSemaphore);
        pCommandBuffers.put(0, job.getCMD());

        submitInfo.waitSemaphoreCount(pImageAcquireSemaphores.remaining());
        submitInfo.pWaitSemaphores(pImageAcquireSemaphores);
        submitInfo.pSignalSemaphores(pSignalSemaphores);
        submitInfo.pCommandBuffers(pCommandBuffers);

        renderCompleteFence = VkFenceWrapperFactory.recreateWrapper(dict, renderCompleteFence);
        supervisor.addFence(renderCompleteFence);

        int err = vkQueueSubmit(queue, submitInfo, renderCompleteFence.getFence());
        VulkanResult.assertValidate(err, "Failed to submit queue work!");
    }

    @Override
    public void render(long swapchain, long... imageAcquireSemaphores) {
        state = State.ACQUIRED;

        LongBuffer pImageAcquireSemaphores = MemoryUtil.memAllocLong(imageAcquireSemaphores.length);
        pImageAcquireSemaphores.put(imageAcquireSemaphores);

        submitToQueue(dict,
                ContextUtil.getDevice(dict).getDevice(),
                ContextUtil.getQueue(dict).getQueue(),
                pImageAcquireSemaphores);
    }

    @Override
    public long[] getRenderCompleteSemaphores() {
        return new long[] {renderCompleteSemaphore};
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Dependency getDependency() {
        return renderCompleteFence.getDependency();
    }

    @Override
    public void destroy() {
        if (renderCompleteSemaphore != VK_NULL_HANDLE) {
            VkSemaphoreFactory.destroySemaphore(ContextUtil.getDevice(dict).getDevice(), renderCompleteSemaphore);
        }

        VkDevice device = ContextUtil.getDevice(dict).getDevice();
        VkFenceWrapperFactory.destroy(device, renderCompleteFence);
    }
}
