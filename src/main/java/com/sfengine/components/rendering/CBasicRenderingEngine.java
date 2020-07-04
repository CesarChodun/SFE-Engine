package com.sfengine.components.rendering;

import com.sfengine.components.contexts.DefaultContexts;
import com.sfengine.components.contexts.framebufferfactory.BasicFrameBufferFactoryContextFactory;
import com.sfengine.components.contexts.renderjob.BasicRenderJobContext;
import com.sfengine.components.contexts.renderjob.BasicRenderJobContextFactory;
import com.sfengine.components.contexts.swapchain.BasicSwapchainContextFactory;
import com.sfengine.components.resources.MemoryBin;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.context.swapchain.SwapchainContext;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.engine.EngineTask;
import com.sfengine.core.rendering.*;
import com.sfengine.core.rendering.frames.FrameFactory;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import com.sfengine.core.synchronization.VkFence.VkFenceSupervisor;
import com.sfengine.core.synchronization.VkFence.VkFenceSupervisorTask;
import junit.framework.Assert;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.IntBuffer;
import java.util.*;

import static com.sfengine.core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.*;

public class CBasicRenderingEngine implements RenderingEngine {

    private final Engine engine = EngineFactory.getEngine();
    private CFrame frame;

    private final Map<Long, PipelineContainer> pipelines = Collections.synchronizedMap(new HashMap<>());
    private final Set<Updatable> upds = Collections.synchronizedSet(new HashSet<>());

    private RenderPass renderPass;

    private final MemoryBin bin = new MemoryBin();
    private List<EngineTask> tickTasks = new ArrayList<EngineTask>();

    private final DependencyFence created = new DependencyFence();

    private volatile ContextDictionary dict;
    private volatile FrameFactory frameFactory;

    public CBasicRenderingEngine(ContextDictionary dict, CFrame frame) {
        this.dict = dict;
        this.frame = frame;

        List<Dependency> deps = DefaultContexts.getDependencies();
        deps.add(frame.getDependency());

        Dependency[] depsArr = new Dependency[deps.size()];
        for (int i = 0; i < deps.size(); i++)
            depsArr[i] = deps.get(i);

        engine.addConfig(() -> {
            initialize(dict);
        }, depsArr);
    }

    private void record() {

    }

    private void initialize(ContextDictionary dict) {
        RenderingEngineFactory.setRenderingEngine(frame, this);

        Window window = frame.getWindow();

        // Creating required vulkan objects.
        VkPhysicalDevice physicalDevice = ContextUtil.getPhysicalDevice(dict).getPhysicalDevice();
        ColorFormatAndSpace colorFormat = getColorFormat(window, physicalDevice);
        int renderQueueFamilyIndex = ContextUtil.getQueueFamily(dict).getQueueFamilyIndex();

        // Checking the window support
        checkSupport(window, physicalDevice, renderQueueFamilyIndex);

        renderPass = RenderPassFactory.createRenderPass(dict, "RenderPass1");
        bin.add(renderPass);

        CommandBufferFactory basicCMD =
                new CommandBufferFactory(dict, (cmd, framebuffer) -> {
                    renderPass.record(cmd, framebuffer);

                    Set<Long> kpipelines = null;
                    synchronized (pipelines) {
                        kpipelines = pipelines.keySet();
                    }
                    for (long p : kpipelines) {
                        vkCmdBindPipeline(cmd, VK_PIPELINE_BIND_POINT_GRAPHICS, p);

                        synchronized (pipelines.get(p)) {
                            for (RenderObject obj : pipelines.get(p))
                                obj.record(cmd, framebuffer);
                        }
                    }

                    vkCmdEndRenderPass(cmd);
                }, renderQueueFamilyIndex, 0);
        dict.put(BasicFrameBufferFactoryContextFactory.createFrameBufferFactoryContext("BasicFBFactory", dict, renderPass.handle()));
        dict.put(BasicSwapchainContextFactory.createSwapchainContext("BasicSwapchain", dict, frame, renderPass.getAttachmentBlueprints(), colorFormat));

        BasicRenderJobContext renderJobContext =
                BasicRenderJobContextFactory.createContext("helloCube", basicCMD, dict, () -> {
                    synchronized (upds) {
                        for (Updatable u : upds)
                            u.update();
                    }
                });
        dict.put(renderJobContext);

        VkFenceSupervisor vksupervisor = new VkFenceSupervisor();
        VkFenceSupervisorTask supTask = new VkFenceSupervisorTask(vksupervisor);
        engine.addTickTask(supTask);
        tickTasks.add(supTask);

        frameFactory = new BasicFrameFactory(dict, vksupervisor);

        engine.addTask(()-> {
            Presenter presenter = new Presenter(dict, frameFactory);
            engine.addTickTask(presenter);
            tickTasks.add(presenter);
            bin.add(presenter);

            created.release();
        });
    }


    /**
     * Obtains suitable color format.
     *
     * @param window
     * @param physicalDevice
     * @return
     */
    private static ColorFormatAndSpace getColorFormat(
            Window window, VkPhysicalDevice physicalDevice) {
        ColorFormatAndSpace colorFormat = new ColorFormatAndSpace(0, 0);
        try {
            colorFormat =
                    RenderUtil.getNextColorFormatAndSpace(
                            0,
                            physicalDevice,
                            window.getSurface(),
                            VK_FORMAT_B8G8R8A8_UNORM,
                            VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
        } catch (VulkanException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to choose color format and space.");
        }

        return colorFormat;
    }

    /**
     * Checks if the physical device supports KHR surface.
     *
     * @param window
     * @param physicalDevice
     * @param renderQueueFamilyIndex
     */
    private static void checkSupport(
            Window window, VkPhysicalDevice physicalDevice, int renderQueueFamilyIndex) {
        IntBuffer pSupported = memAllocInt(1);
        int err =
                vkGetPhysicalDeviceSurfaceSupportKHR(
                        physicalDevice, renderQueueFamilyIndex, window.getSurface(), pSupported);

        try {
            validate(err, "Failed to check device KHR surface suport.");
        } catch (VulkanException e1) {
            e1.printStackTrace();
        }

        if (pSupported.get(0) != VK_TRUE) {
            throw new AssertionError("Device does not support the khr swapchain.");
        }

        memFree(pSupported);
    }

    @Override
    public void add(RenderObject obj) {
        if (!pipelines.containsKey(obj.getPipeline()))
            pipelines.put(obj.getPipeline(), new PipelineContainer(obj.getPipeline()));

        pipelines.get(obj.getPipeline()).add(obj);
    }

    @Override
    public boolean remove(RenderObject obj) {
        if (!pipelines.containsKey(obj.getPipeline()))
            return false;

        return pipelines.get(obj.getPipeline()).remove(obj);
    }

    @Override
    public void requestUpdate() {
        throw new NotImplementedException();
    }

    @Override
    public void forceUpdate() {
        SwapchainContext swc = ContextUtil.getSwapchain(dict);
        frameFactory.update(swc);
    }

    @Override
    public void addUpdate(Updatable upd) {
        if (upd == null)
            throw new AssertionError("Updatable cannot be null.");
        upds.add(upd);
    }

    @Override
    public void removeUpdate(Updatable upd) {
        upds.add(upd);
    }

    @Override
    public RenderPass getRenderPass() {
        return renderPass;
    }

    @Override
    public CFrame getCFrame() {
        return frame;
    }

    @Override
    public void destroy() {

        for (EngineTask task : tickTasks) {
            engine.removeTickTask(task);
        }

        //TODO: free the rest of the resources

        bin.destroy();
    }

    @Override
    public Dependency getDependency() {
        return created;
    }
}
