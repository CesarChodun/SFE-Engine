package com.sfengine.components.rendering;

import com.sfengine.components.rendering.BasicFrame;
import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.context.renderjob.RenderJobContext;
import com.sfengine.core.context.swapchain.SwapchainContext;
import com.sfengine.core.rendering.frames.Frame;
import com.sfengine.core.rendering.frames.FrameFactory;
import com.sfengine.core.rendering.recording.RenderJob;
import com.sfengine.core.synchronization.VkFence.VkFenceSupervisor;
import com.sfengine.core.synchronization.VkFence.VkFenceWrapper;
import com.sfengine.core.synchronization.VkFence.VkFenceWrapperFactory;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkSubmitInfo;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

public class BasicFrameFactory implements FrameFactory {

    private List<Frame> frames = Collections.synchronizedList(new ArrayList<>());
    private Set<Frame> oldFrames = Collections.synchronizedSet(new HashSet<>());

    private volatile int activeFrames = 0;

    private final IntBuffer pWaitDstStageMask = memAllocInt(1)
            .put(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
    private final VkSubmitInfo submitInfo = VkSubmitInfo.calloc()
            .sType(VK_STRUCTURE_TYPE_SUBMIT_INFO)
            .pNext(NULL)
            .pWaitDstStageMask(pWaitDstStageMask);

    private final LongBuffer pSignalSemaphores = memAllocLong(1);
    private final PointerBuffer pCommandBuffers = memAllocPointer(1);

    private VkFenceSupervisor supervisor;
    private ContextDictionary dict;

    public BasicFrameFactory(ContextDictionary dict, VkFenceSupervisor supervisor) {
        this.dict = dict;
        this.supervisor = supervisor;
    }

    private Frame create(VkFenceWrapper wrapper, RenderJob job) {
        return new BasicFrame(dict,
                wrapper,
                supervisor,
                job,
                submitInfo,
                pSignalSemaphores,
                pCommandBuffers);
    }

    private void forgetFrame(int num) {
        if (frames.get(num).getState() == Frame.State.WAITING)
            frames.get(num).destroy();
        else
            oldFrames.add(frames.get(num));
    }

    @Override
    public void update(SwapchainContext swapchainContext) {
        int requiredImages = swapchainContext.info().minImageCount();

        if (requiredImages != frames.size()) {
            long[] frameBuffers = swapchainContext.getFrameBuffers();
            RenderJobContext jobContext = ContextUtil.getRenderJob(dict);
            jobContext.recreateJobs(frameBuffers);

            VkFenceWrapper[] wrappers = VkFenceWrapperFactory.createWrapper(dict, requiredImages);

            for (int i = 0; i < frames.size(); i++) {
                forgetFrame(i);
            }

            frames.clear();

            for (int i = 0; i < requiredImages; i++)
                frames.add(create(wrappers[i], jobContext.getJob(frameBuffers[i])));
        }
        else {
            long[] frameBuffers = swapchainContext.getFrameBuffers();
            RenderJobContext jobContext = ContextUtil.getRenderJob(dict);
            jobContext.recreateJobs(frameBuffers);

            VkFenceWrapper[] wrappers = VkFenceWrapperFactory.createWrapper(dict, requiredImages);

            for (int i = 0; i < frames.size(); i++) {
                forgetFrame(i);
            }

            for (int i = 0; i < requiredImages; i++)
                frames.set(i, create(wrappers[i], jobContext.getJob(frameBuffers[i])));
        }


    }

    @Override
    public int lazyCount() {
        return frames.size() - activeFrames;
    }

    @Override
    public Frame popFrame(int id) {
        activeFrames++;
        return frames.get(id);
    }

    @Override
    public void releaseFrame(Frame frame) {
        activeFrames--;

        if (oldFrames.contains(frame)) {
            oldFrames.remove(frame);
            frame.destroy();
        }
    }
}
