package demos.helloDescriptor.rendering;

import static com.sfengine.core.rendering.RenderUtil.createLogicalDevice;
import static com.sfengine.core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.*;

import com.sfengine.components.contexts.DefaultContexts;
import com.sfengine.components.contexts.framebufferfactory.BasicFrameBufferFactoryContextFactory;
import com.sfengine.components.contexts.renderjob.BasicRenderJobContext;
import com.sfengine.components.contexts.renderjob.BasicRenderJobContextFactory;
import com.sfengine.components.contexts.swapchain.BasicSwapchainContextFactory;
import com.sfengine.components.geometry.unindexed.MeshU2D;
import com.sfengine.components.rendering.RenderPassFactory;
import com.sfengine.components.rendering.pipeline.GraphicsPipeline;
import com.sfengine.components.rendering.pipeline.ImageViewCreateInfo;
import com.sfengine.components.rendering.pipeline.Pipeline;
import com.sfengine.components.rendering.pipeline.PipelineLayout;
import com.sfengine.components.rendering.BasicFrameFactory;
import com.sfengine.components.rendering.RenderPass;
import com.sfengine.components.resources.MemoryBin;
import com.sfengine.components.shaders.descriptor_sets.DescriptorSet;
import com.sfengine.components.shaders.descriptor_sets.DescriptorSetBlueprint;
import com.sfengine.components.shaders.descriptor_sets.DescriptorSetFactory;
import com.sfengine.components.shaders.descriptor_sets.FileDescriptorSetBlueprint;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.Application;
import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.engine.EngineTask;
import com.sfengine.core.rendering.*;
import com.sfengine.core.rendering.recording.Recordable;
import com.sfengine.core.rendering.CommandBufferFactory;
import com.sfengine.core.resources.Asset;
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.synchronization.VkFence.VkFenceSupervisor;
import com.sfengine.core.synchronization.VkFence.VkFenceSupervisorTask;
import demos.helloDescriptor.rendering.environment.EnvironmentDescriptorSet;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import org.joml.Vector2f;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkViewport;

public class InitializeRendering implements EngineTask, Destroyable {

    private final Engine engine = EngineFactory.getEngine();
    private CFrame frame;

    private MemoryBin destroy = new MemoryBin();
    private List<EngineTask> tickTasks = new ArrayList<EngineTask>();

    private VkViewport.Buffer viewport;
    private VkRect2D.Buffer scissor;
    LongBuffer pDesc;
    Timer timer;

    private ContextDictionary dict;

    public InitializeRendering(CFrame frame) {
        this.frame = frame;
        this.dict = DefaultContexts.getDictionary();
    }

    @Override
    public void run() throws AssertionError {
        Window window = frame.getWindow();

        // Creating required vulkan objects.
        VkPhysicalDevice physicalDevice = ContextUtil.getPhysicalDevice(dict).getPhysicalDevice();
        ColorFormatAndSpace colorFormat = getColorFormat(window, physicalDevice);
        int renderQueueFamilyIndex = ContextUtil.getQueueFamily(dict).getQueueFamilyIndex();
        VkDevice device = ContextUtil.getDevice(dict).getDevice();
        final VkQueue renderQueue = ContextUtil.getQueue(dict).getQueue();

        // Checking the window support
        checkSupport(window, physicalDevice, renderQueueFamilyIndex);

        RenderPass renderPass = RenderPassFactory.createRenderPass(dict, "RenderPass1");
        destroy.add(renderPass);

        DescriptorSetBlueprint[] dscBlueprint =
                createDscBlueprints(
                        device, Application.getConfigAssets().getSubAsset("descriptors"), destroy);
        final Pipeline pipeline = createPipeline(physicalDevice, device, renderPass, dscBlueprint);

        DescriptorSet[] descriptorSets = createDescriptorSets(physicalDevice, device, dscBlueprint);
        destroy.add(descriptorSets);
        pDesc = memAllocLong(descriptorSets.length);
        for (DescriptorSet dsc : descriptorSets) {
            pDesc.put(dsc.getDescriptorSet());
        }
        pDesc.flip();

        Recordable cmdPreset = makePreset(window);
        Recordable cmdWork = makeWorkRecordable(physicalDevice, device, pipeline, pDesc);
        renderPass.setPreset(cmdPreset);
        renderPass.setWork(cmdWork);

        CommandBufferFactory basicCMD =
                new CommandBufferFactory(dict, renderPass, renderQueueFamilyIndex, 0);

        dict.put(BasicFrameBufferFactoryContextFactory.createFrameBufferFactoryContext("BasicFBFactory", dict, renderPass.handle()));
        dict.put(BasicSwapchainContextFactory.createSwapchainContext("BasicSwapchain", dict, frame, renderPass.getAttachmentBlueprints(), colorFormat));

        BasicRenderJobContext renderJobContext =
                BasicRenderJobContextFactory.createContext("helloCube", basicCMD, dict);
        dict.put(renderJobContext);

        VkFenceSupervisor vksupervisor = new VkFenceSupervisor();
        VkFenceSupervisorTask supTask = new VkFenceSupervisorTask(vksupervisor);
        engine.addTickTask(supTask);
        tickTasks.add(supTask);

        engine.addTask(()-> {
            Presenter presenter = new Presenter(dict, new BasicFrameFactory(dict, vksupervisor));
            engine.addTickTask(presenter);
            tickTasks.add(presenter);
            destroy.add(presenter);
        });

        window.setVisible(true);
    }

    @Override
    public void destroy() {
        timer.cancel();

        for (EngineTask task : tickTasks) {
            engine.removeTickTask(task);
        }

        viewport.free();
        scissor.free();
        memFree(pDesc);

        destroy.destroy();
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

    private static DescriptorSetBlueprint[] createDscBlueprints(
            VkDevice device, Asset asset, MemoryBin destroy) {
        DescriptorSetBlueprint[] out = new FileDescriptorSetBlueprint[1];

        try {
            FileDescriptorSetBlueprint dscb =
                    new FileDescriptorSetBlueprint(device, asset.getConfigFile("general.cfg"));
            destroy.add(dscb);
            out[0] = dscb;
        } catch (Exception e) {
            throw new AssertionError("Failed to create shader ");
        }

        return out;
    }

    /**
     * Creates a basic pipeline.
     *
     * @param physicalDevice
     * @param device
     * @param renderPass
     * @return
     */
    private static Pipeline createPipeline(
            VkPhysicalDevice physicalDevice,
            VkDevice device,
            RenderPass renderPass,
            DescriptorSetBlueprint... descriptorBlueprints) {
        try {
            PipelineLayout layout = new PipelineLayout(device, descriptorBlueprints);

            return new GraphicsPipeline(
                    Application.getConfigAssets().getSubAsset("pipeline"),
                    "pipeline.cfg",
                    device,
                    renderPass.handle(),
                    layout.getPipelineLayout());
        } catch (VulkanException | IOException | AssertionError e) {
            e.printStackTrace();
            throw new AssertionError("Failed to create pipeline.");
        }
    }

    /**
     * Creates a descriptor set for "time" uniform from the //TODO
     *
     * @param device
     * @param dscBlueprint
     * @return
     */
    private DescriptorSet[] createDescriptorSets(
            VkPhysicalDevice physicalDevice,
            VkDevice device,
            DescriptorSetBlueprint... dscBlueprint) {
        long[] dscs = new long[0];
        try {
            dscs = DescriptorSetFactory.createDescriptorSets(device, dscBlueprint);
        } catch (VulkanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DescriptorSet[] out = new DescriptorSet[dscs.length];
        EnvironmentDescriptorSet env =
                new EnvironmentDescriptorSet(physicalDevice, device, dscs[0]);
        TimeDescriptorUpdater timeUp = new TimeDescriptorUpdater(env);
        timer = new Timer("Time timer");
        timer.schedule(timeUp, 30, 10);

        out[0] = env;

        return out;
    }

    /**
     * Creates a basic Recordable that will initialize the command recording process.
     *
     * @param window
     * @return
     */
    private Recordable makePreset(Window window) {
        // Update dynamic viewport state
        viewport =
                VkViewport.calloc(1)
                        .width(window.getWidth())
                        .height(window.getHeight())
                        .minDepth(0.0f)
                        .maxDepth(1.0f)
                        .x(0f)
                        .y(0f);

        // Update dynamic scissor state
        scissor = VkRect2D.calloc(1);
        scissor.extent().set(window.getWidth(), window.getHeight());
        scissor.offset().set(0, 0);

        Recordable out =
                (buffer, framebuffer) -> {

                    vkCmdSetScissor(buffer, 0, scissor);
                    vkCmdSetViewport(buffer, 0, viewport);
                };

        return out;
    }

    /**
     * Creates a Recordable that will render a triangle to the screen.
     *
     * @param physicalDevice
     * @param device
     * @param pipeline
     * @return
     */
    private static Recordable makeWorkRecordable(
            VkPhysicalDevice physicalDevice, VkDevice device, Pipeline pipeline, LongBuffer pDesc) {

        List<Vector2f> vert = new ArrayList<Vector2f>();
        vert.add(new Vector2f(-0.5f, -0.5f));
        vert.add(new Vector2f(0.5f, -0.5f));
        vert.add(new Vector2f(0.0f, 0.5f));

        MeshU2D mesh2;
        try {
            mesh2 = new MeshU2D(physicalDevice, device, vert);
        } catch (VulkanException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to make work recordable.");
        }

        Recordable recCmd =
                (buffer, framebuffer) -> {

                    // Bind the rendering pipeline (including the shaders)
                    vkCmdBindPipeline(
                            buffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.handle());

                    // Bind descriptor sets
                    vkCmdBindDescriptorSets(
                            buffer,
                            VK_PIPELINE_BIND_POINT_GRAPHICS,
                            pipeline.layout(),
                            0,
                            pDesc,
                            null);

                    // Bind triangle vertices
                    LongBuffer offsets = memAllocLong(1);
                    offsets.put(0, 0L);
                    LongBuffer pBuffers = memAllocLong(1);
                    pBuffers.put(0, mesh2.getVerticesHandle());
                    vkCmdBindVertexBuffers(buffer, 0, pBuffers, offsets);

                    memFree(pBuffers);
                    memFree(offsets);

                    vkCmdDraw(buffer, mesh2.verticesCount(), 1, 0, 0);
                };

        return recCmd;
    }

    /**
     * Creates an ImageViewCreateInfo with it's basic configuration.
     *
     * @return
     */
    private static ImageViewCreateInfo getImageViewCreateInfo() {
        ImageViewCreateInfo info;
        try {
            info = new ImageViewCreateInfo(Application.getConfigAssets().getConfigFile("rendererImageVieCI.cfg"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to create image view create info.");
        }

        return info;
    }
}
