package demos.helloTriangle;

import static com.sfengine.core.rendering.RenderUtil.createLogicalDevice;
import static com.sfengine.core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;

import com.sfengine.components.contexts.DefaultContexts;
import com.sfengine.components.contexts.framebufferfactory.BasicFrameBufferFactoryContextFactory;
import com.sfengine.components.contexts.renderjob.BasicRenderJobContext;
import com.sfengine.components.contexts.renderjob.BasicRenderJobContextFactory;
import com.sfengine.components.contexts.swapchain.BasicSwapchainContextFactory;
import com.sfengine.components.geometry.unindexed.MeshU2D;
import com.sfengine.components.rendering.pipeline.GraphicsPipeline;
import com.sfengine.components.rendering.BasicFrameFactory;
import com.sfengine.components.rendering.RenderPass;
import com.sfengine.components.resources.MemoryBin;
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
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.result.VulkanException;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import com.sfengine.core.synchronization.VkFence.VkFenceSupervisor;
import com.sfengine.core.synchronization.VkFence.VkFenceSupervisorTask;
import org.joml.Vector2f;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkViewport;

public class InitializeRendering implements EngineTask, Destroyable {

    private final Engine engine = EngineFactory.getEngine();
    private CFrame frame;
    private ContextDictionary dict;

    private MemoryBin destroy = new MemoryBin();
    private List<EngineTask> tickTasks = new ArrayList<>();

    private VkViewport.Buffer viewport;
    private VkRect2D.Buffer scissor;
    private AttachmentBlueprint[] atts;

    private VkPhysicalDevice physicalDevice;
    private int renderQueueFamilyIndex;
    private VkDevice device;
    private VkQueue renderQueue;

    public InitializeRendering(CFrame frame) {
        this.frame = frame;
        this.dict = DefaultContexts.getDictionary();
    }

    @Override
    public void run() throws AssertionError {
        Window window = frame.getWindow();

        physicalDevice = ContextUtil.getPhysicalDevice(dict).getPhysicalDevice();
        renderQueueFamilyIndex = ContextUtil.getQueueFamily(dict).getQueueFamilyIndex();
        device = ContextUtil.getDevice(dict).getDevice();
        renderQueue = ContextUtil.getQueue(dict).getQueue();

        // Creating required vulkan objects.
        ColorFormatAndSpace colorFormat = getColorFormat(window, physicalDevice);

        // Checking the window support
        checkSupport(window, physicalDevice, renderQueueFamilyIndex);

        RenderPass renderPass = createRenderPass(dict, device, colorFormat.colorFormat);
        destroy.add(renderPass);
        long pipeline = createPipeline(physicalDevice, device, renderPass);

        Recordable cmdPreset = makePreset(window);
        Recordable cmdWork = makeWorkRecordable(physicalDevice, device, pipeline);
        renderPass.setPreset(cmdPreset);
        renderPass.setWork(cmdWork);

        CommandBufferFactory basicCMD =
                new CommandBufferFactory(device, renderPass, renderQueueFamilyIndex, 0);

        dict.put(BasicFrameBufferFactoryContextFactory.createFrameBufferFactoryContext("BasicFBFactory", dict, renderPass.handle()));
        dict.put(BasicSwapchainContextFactory.createSwapchainContext("BasicSwapchain", dict, frame, atts, colorFormat));

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
        for (EngineTask task : tickTasks) {
            engine.removeTickTask(task);
        }

        viewport.free();
        scissor.free();

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

    /**
     * Creates a basic render pass.
     *
     * @param logicalDevice
     * @param colorFormat
     * @return
     */
    private RenderPass createRenderPass(ContextDictionary dict, VkDevice logicalDevice, int colorFormat) {
        VkAttachmentReference.Buffer colorReference =
                VkAttachmentReference.calloc(1)
                        .attachment(0)
                        .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        VkSubpassDescription.Buffer subpass =
                VkSubpassDescription.calloc(1)
                        .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                        .flags(0)
                        .pInputAttachments(null)
                        .colorAttachmentCount(colorReference.remaining())
                        .pColorAttachments(colorReference)
                        .pResolveAttachments(null)
                        .pDepthStencilAttachment(null)
                        .pPreserveAttachments(null);

        atts = new AttachmentBlueprint[1];
        try {
            atts[0] = new FileAttachmentBlueprint(dict, Application.getConfigAssets().getSubAsset("RenderPass1").getConfigFile("attachment01.cfg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final RenderPass renderPass = new RenderPass(logicalDevice, subpass, null, atts);

        return renderPass;
    }

    /**
     * Creates a basic pipeline.
     *
     * @param physicalDevice
     * @param device
     * @param renderPass
     * @return
     */
    private static long createPipeline(
            VkPhysicalDevice physicalDevice, VkDevice device, RenderPass renderPass) {
        long pipeline = VK_NULL_HANDLE;

        try {
            final long rpHandle = renderPass.handle();

            // renderingPipeline.getGraphicsPipeline();
            // Create the pipeline layout that is used to generate the rendering pipelines that
            // are based on this descriptor set layout
            VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo =
                    VkPipelineLayoutCreateInfo.calloc()
                            .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                            .pNext(NULL)
                            .pSetLayouts(null)
                            .pPushConstantRanges(null);

            LongBuffer pLayout = memAllocLong(1);
            int err = vkCreatePipelineLayout(device, pipelineLayoutCreateInfo, null, pLayout);
            validate(err, "Failed to create pipeline layout.");

            long layout = pLayout.get(0);

            memFree(pLayout);
            pipelineLayoutCreateInfo.free();

            GraphicsPipeline gp =
                    new GraphicsPipeline(
                            Application.getConfigAssets().getSubAsset("pipeline"),
                            "pipeline.cfg",
                            device,
                            rpHandle,
                            layout);

            pipeline = gp.getPipelineHandle();
        } catch (VulkanException | IOException | AssertionError e) {
            e.printStackTrace();
            throw new AssertionError("Failed to create pipeline.");
        }

        return pipeline;
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
                new Recordable() {

                    @Override
                    public void record(VkCommandBuffer buffer) {

                        vkCmdSetScissor(buffer, 0, scissor);
                        vkCmdSetViewport(buffer, 0, viewport);
                    }
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
            VkPhysicalDevice physicalDevice, VkDevice device, Long pipeline) {

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
                new Recordable() {

                    @Override
                    public void record(VkCommandBuffer buffer) {

                        // Bind the rendering pipeline (including the shaders)
                        vkCmdBindPipeline(buffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);

                        // Bind triangle vertices
                        LongBuffer offsets = memAllocLong(1);
                        offsets.put(0, 0L);
                        LongBuffer pBuffers = memAllocLong(1);
                        pBuffers.put(0, mesh2.getVerticesHandle());
                        vkCmdBindVertexBuffers(buffer, 0, pBuffers, offsets);

                        memFree(pBuffers);
                        memFree(offsets);

                        vkCmdDraw(buffer, mesh2.verticesCount(), 1, 0, 0);
                    }
                };

        return recCmd;
    }
}
