package com.sfengine.core.rendering.presenting;

import com.sfengine.core.rendering.frames.Frame;
import com.sfengine.core.rendering.frames.FrameFactory;
import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.context.swapchain.SwapchainContext;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.engine.EngineTask;
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.result.VulkanResult;
import com.sfengine.core.synchronization.VkSemaphore.VkSemaphoreFactory;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;

public class Presenter implements EngineTask, Destroyable {

    private final Engine engine = EngineFactory.getEngine();

    private FrameFactory frames;

    private ContextDictionary dict;
    private volatile long lastSwapchain = VK_NULL_HANDLE;

    private final VkPresentInfoKHR presentInfo;

    public Presenter(ContextDictionary dict, FrameFactory frames) {
        this.dict = dict;
        this.frames =  frames;

        LongBuffer pSwapchain = memAllocLong(1);
        pSwapchain.put(0);
        pSwapchain.flip();

        IntBuffer pImages = memAllocInt(1);
        pImages.put(0);
        pImages.flip();

        LongBuffer pWait = memAllocLong(1);
        pWait.put(0);
        pWait.flip();


        this.presentInfo = VkPresentInfoKHR.calloc()
                .sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                .pNext(NULL)
                .swapchainCount(1)
                .pSwapchains(pSwapchain)
                .pImageIndices(pImages)
                .pWaitSemaphores(pWait)
                .pResults(null);
    }

    @Override
    public void run() throws AssertionError {
        SwapchainContext swapchainContrext = ContextUtil.getSwapchain(dict);
        long newSwapchain = swapchainContrext.getHandle();

        if (lastSwapchain != newSwapchain) {
            frames.update(swapchainContrext);
            lastSwapchain = newSwapchain;
        }


        if (frames.lazyCount() == 0)
            return;

        final long semaphore = VkSemaphoreFactory.createSemaphore(dict);
        VkQueue presentQueue = ContextUtil.getQueue(dict).getQueue();
        VkDevice device = ContextUtil.getDevice(dict).getDevice();

        int[] pImageIndex = new int[1];
        int err = vkAcquireNextImageKHR(
                        device,
                        swapchainContrext.getHandle(),
                        0xFFFFFFFFFFFFFFFFL,
                        semaphore,
                        VK_NULL_HANDLE,
                        pImageIndex); // Fences can be used for synchronization.
        try {
            VulkanResult.validate(err, "Failed to acquire imageIndex image KHR!");
        } catch (VulkanException e) {
            e.printStackTrace();
        }
        int nextImage = pImageIndex[0];

        final Frame frame = frames.popFrame(nextImage);
        frame.render(semaphore);


        engine.addTask(() -> {
            int subErr;

            synchronized (presentInfo) {
                VkSemaphoreFactory.destroySemaphore(device, semaphore);

                long[] sems = frame.getRenderCompleteSemaphores();
                if (sems.length > 1)
                    throw new NotImplementedException();
                presentInfo.pWaitSemaphores().put(0, sems[0]);

                presentInfo.pImageIndices().put(0, nextImage);

                presentInfo.pSwapchains().put(0, swapchainContrext.getHandle());

                subErr = vkQueuePresentKHR(presentQueue, presentInfo);

                frames.releaseFrame(frame);
            }

            VulkanResult.assertValidate(subErr, "Failed to present the image.");
        }, frame.getDependency());
    }

    @Override
    public void destroy() {

        presentInfo.free();
    }
}
