package demos.helloCube.rendering;

import static com.sfengine.core.rendering.RenderUtil.createLogicalDevice;
import static com.sfengine.core.rendering.RenderUtil.getDeviceQueue;
import static com.sfengine.core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import com.sfengine.components.geometry.indexed.MeshI3D;
import com.sfengine.components.pipeline.Attachments;
import com.sfengine.components.pipeline.GraphicsPipeline;
import com.sfengine.components.pipeline.ImageViewCreateInfo;
import com.sfengine.components.pipeline.Pipeline;
import com.sfengine.components.pipeline.PipelineLayout;
import com.sfengine.components.recording.RenderPass;
import com.sfengine.components.resources.MemoryBin;
import com.sfengine.components.shaders.descriptor_sets.DescriptorSet;
import com.sfengine.components.shaders.descriptor_sets.DescriptorSetBlueprint;
import com.sfengine.components.shaders.descriptor_sets.DescriptorSetFactory;
import com.sfengine.components.shaders.descriptor_sets.FileDescriptorSetBlueprint;
import com.sfengine.components.transform.CameraTransform;
import com.sfengine.components.transform.ModelTransform3D;
import com.sfengine.core.*;
import com.sfengine.core.rendering.ColorFormatAndSpace;
import com.sfengine.core.rendering.Recordable;
import com.sfengine.core.rendering.RenderUtil;
import com.sfengine.core.rendering.Renderer;
import com.sfengine.core.rendering.Window;
import com.sfengine.core.rendering.factories.CommandBufferFactory;
import com.sfengine.core.resources.Asset;
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.result.VulkanException;
import demos.util.BasicFramebufferFactory;
import demos.util.BasicSwapchainFactory;
import demos.util.RenderingTask;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import org.joml.Vector3f;
import org.lwjgl.vulkan.EXTDescriptorIndexing;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkViewport;

/**
 * Sets up data needed for the rendering.
 *
 * @author Cezary Chodun
 * @since 12.03.2020
 */
public class InitializeRendering implements EngineTask, Destroyable {

    private final Engine engine = EngineFactory.getEngine();
    private Window window;

    private MemoryBin destroy = new MemoryBin();
    private List<EngineTask> tickTasks = new ArrayList<EngineTask>();

    private VkViewport.Buffer viewport;
    private VkRect2D.Buffer scissor;
    LongBuffer pDesc;
    Timer timer;

    private ModelTransform3D cubeTransform = new ModelTransform3D();
    private CameraTransform camera;

    public InitializeRendering(Window window) {
        this.window = window;
    }

    @Override
    public void run() throws AssertionError {
        // Creating required vulkan objects.
        VkPhysicalDevice physicalDevice = getPhysicalDevice();
        ColorFormatAndSpace colorFormat = getColorFormat(window, physicalDevice);
        int renderQueueFamilyIndex = getRenderQueueFamilyIndex(window, physicalDevice);
        VkDevice device = getLogicalDevice(physicalDevice, renderQueueFamilyIndex);
        final VkQueue renderQueue = getDeviceQueue(device, renderQueueFamilyIndex, 0);

        // Checking the window support
        checkSupport(window, physicalDevice, renderQueueFamilyIndex);

        RenderPass renderPass = createRenderPass(device, colorFormat.colorFormat);
        destroy.add(renderPass);
        DescriptorSetBlueprint[] dscBlueprint =
                createDscBlueprints(
                        device, Application.getConfigAssets().getSubAsset("descriptors"), destroy);
        final Pipeline pipeline = createPipeline(physicalDevice, device, renderPass, dscBlueprint);

        camera = new CameraTransform(window.getWidth(), window.getHeight());

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
                new CommandBufferFactory(device, renderPass, renderQueueFamilyIndex, 0);
        BasicSwapchainFactory swapchainFactory =
                new BasicSwapchainFactory(physicalDevice, colorFormat);
        BasicFramebufferFactory fbFactory =
                new BasicFramebufferFactory(device, renderPass.handle());
        destroy.add(fbFactory);

        ImageViewCreateInfo imageInfo = getImageViewCreateInfo();
        Renderer winRenderer =
                new Renderer(
                        window,
                        device,
                        renderQueue,
                        imageInfo.getInfo(),
                        basicCMD,
                        swapchainFactory,
                        fbFactory);

        // Creating rendering task
        RenderingTask renderingTask = new RenderingTask(winRenderer);
        engine.addTickTask(renderingTask);
        tickTasks.add(renderingTask);

        window.setVisible(true);

        destroy.add(winRenderer);
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
     * Chooses the most appropriate physical device(GPU). Discrete GPUs are preferred and if it is
     * available it will be returned.
     *
     * @return the physical device.
     */
    private static VkPhysicalDevice getPhysicalDevice() {
        return  HardwareManager.getBestPhysicalDevice(
                device -> {

                    VkPhysicalDeviceProperties props =
                            VkPhysicalDeviceProperties.calloc();
                    vkGetPhysicalDeviceProperties(device, props);

                    int out = 0;

                    if (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                        out = 1000;
                    } else if (props.deviceType()
                            == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU) {
                        out = 100;
                    }

                    props.free();
                    return out;
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
     * Returns an index to the most suitable queue family. According to the hardware config file.
     *
     * @param window
     * @param physicalDevice
     * @return
     */
    private static int getRenderQueueFamilyIndex(Window window, VkPhysicalDevice physicalDevice) {
        int renderQueueFamilyIndex = 0;

        try {
            renderQueueFamilyIndex = HardwareManager.getMostSuitableQueueFamily(physicalDevice);
        } catch (NoSuchFieldException
                | SecurityException
                | IllegalArgumentException
                | IllegalAccessException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to obtain render queue family index");
        }

        return renderQueueFamilyIndex;
    }

    /**
     * Obtains a basic logical device.
     *
     * @param physicalDevice
     * @param renderQueueFamilyIndex
     * @return
     */
    private static VkDevice getLogicalDevice(
            VkPhysicalDevice physicalDevice, int renderQueueFamilyIndex) {

        FloatBuffer queuePriorities = memAllocFloat(1);
        queuePriorities.put(0.0f);
        queuePriorities.flip();

        List<String> extensions = new ArrayList<String>();
        extensions.add(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
        extensions.add(EXTDescriptorIndexing.VK_EXT_DESCRIPTOR_INDEXING_EXTENSION_NAME);

        VkDevice device = null;

        try {
            device =
                    createLogicalDevice(
                            physicalDevice,
                            renderQueueFamilyIndex,
                            queuePriorities,
                            0,
                            null,
                            extensions);
        } catch (VulkanException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to create logical device.");
        }

        return device;
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
    private static RenderPass createRenderPass(VkDevice logicalDevice, int colorFormat) {
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

        Attachments attachments;
        RenderPass renderPass;
        try {
            attachments =
                    new Attachments(
                            Application.getConfigAssets().getSubAsset("RenderPass1"),
                            "attachment01.cfg");
            renderPass = new RenderPass(logicalDevice, attachments, subpass, null);
        } catch (AssertionError | IOException | VulkanException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to create render pass.");
        }

        return renderPass;
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
            e.printStackTrace();
            throw new AssertionError("Failed to create descriptor set, while creating descriptor sets.", e);
        }

        // Sets the cube position
        cubeTransform.getPosition().set(0.0f, 0.0f, 2.0f);

        DescriptorSet[] out = new DescriptorSet[dscs.length];
        TransformationDescriptorSet env =
                new TransformationDescriptorSet(physicalDevice, device, dscs[0]);
        Transform4fPeriodicalDescriptorUpdater timeUp =
                new Transform4fPeriodicalDescriptorUpdater(env, cubeTransform, camera, "transform");
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
                buffer -> {

                    vkCmdSetScissor(buffer, 0, scissor);
                    vkCmdSetViewport(buffer, 0, viewport);
                };

        return out;
    }

    /**
     * Makes a square face from the vertices. The first two vertices must not share a edge of the
     * square.
     */
    private static List<Integer> makeSquere(int a, int b, int c, int d) {
        List<Integer> ind = new ArrayList<Integer>();
        ind.add(a);
        ind.add(b);
        ind.add(c);

        ind.add(d);
        ind.add(b);
        ind.add(a);

        return ind;
    }
    /**
     *
     Creates a Recordable that will render a triangle to the screen.
     *
     * @param physicalDevice
     * @param device
     * @param pipeline
     * @return
     */
    private static Recordable makeWorkRecordable(
            VkPhysicalDevice physicalDevice, VkDevice device, Pipeline pipeline, LongBuffer pDesc) {

        // Cube
        List<Vector3f> vert = new ArrayList<Vector3f>();
        vert.add(new Vector3f(0.5f, 0.5f, 0.5f)); // 0
        vert.add(new Vector3f(0.5f, 0.5f, -0.5f)); // 1
        vert.add(new Vector3f(0.5f, -0.5f, 0.5f)); // 2
        vert.add(new Vector3f(0.5f, -0.5f, -0.5f)); // 3
        vert.add(new Vector3f(-0.5f, 0.5f, 0.5f)); // 4
        vert.add(new Vector3f(-0.5f, 0.5f, -0.5f)); // 5
        vert.add(new Vector3f(-0.5f, -0.5f, 0.5f)); // 6
        vert.add(new Vector3f(-0.5f, -0.5f, -0.5f)); // 7

        List<Integer> ind = new ArrayList<Integer>();
        ind.addAll(makeSquere(0, 3, 2, 1)); // x
        ind.addAll(makeSquere(4, 7, 5, 6));

        ind.addAll(makeSquere(0, 5, 1, 4)); // y
        ind.addAll(makeSquere(2, 7, 6, 3));

        ind.addAll(makeSquere(0, 6, 4, 2)); // z
        ind.addAll(makeSquere(1, 7, 3, 5));

        // Creating mesh
        MeshI3D meshI3D;
        try {
            meshI3D = new MeshI3D(physicalDevice, device, vert, ind);
        } catch (VulkanException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to make work recordable.");
        }

        // Making the recordable object for command buffer creation
        Recordable recCmd =
                buffer -> {

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
                    pBuffers.put(0, meshI3D.getVerticesHandle());
                    vkCmdBindVertexBuffers(buffer, 0, pBuffers, offsets);

                    vkCmdBindIndexBuffer(
                            buffer, meshI3D.getIndicesHandle(), (long) 0, VK_INDEX_TYPE_UINT32);

                    vkCmdDrawIndexed(buffer, ind.size(), 1, 0, 0, 0);

                    memFree(pBuffers);
                    memFree(offsets);
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
            info = new ImageViewCreateInfo(Application.getConfigAssets(), "rendererImageVieCI.cfg");
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to create image view create info.");
        }

        return info;
    }
}
