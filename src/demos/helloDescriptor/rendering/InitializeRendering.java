package demos.helloDescriptor.rendering;

import static core.rendering.RenderUtil.createLogicalDevice;
import static core.rendering.RenderUtil.getDeviceQueue;
import static core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU;
import static org.lwjgl.vulkan.VK10.VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdSetScissor;
import static org.lwjgl.vulkan.VK10.vkCmdSetViewport;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceProperties;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkViewport;

import core.Application;
import core.Engine;
import core.EngineTask;
import core.HardwareManager;
import core.hardware.PhysicalDeviceJudge;
import core.rendering.ColorFormatAndSpace;
import core.rendering.Recordable;
import core.rendering.RenderUtil;
import core.rendering.Renderer;
import core.rendering.Window;
import core.rendering.factories.CommandBufferFactory;
import core.resources.Destroyable;
import core.result.VulkanException;
import demos.util.BasicFramebufferFactory;
import demos.util.BasicSwapchainFactory;
import demos.util.RenderingTask;
import rendering.config.Attachments;
import rendering.config.GraphicsPipeline;
import rendering.config.ImageViewCreateInfo;
import rendering.geometry.MeshU2D;
import rendering.recording.RenderPass;

public class InitializeRendering implements EngineTask, Destroyable {
	
	private Engine engine;
	private Window window;
	
	private ListDestroy destroy = new ListDestroy();
	private List<EngineTask> tickTasks = new ArrayList<EngineTask>();
	
	private VkViewport.Buffer viewport;
	private VkRect2D.Buffer scissor;
	
	public InitializeRendering(Engine engine, Window window) {
		this.engine = engine;
		this.window = window;
	}
	
	
	@Override
	public void run() throws AssertionError {
		// Creating required vulkan objects.
		VkPhysicalDevice physicalDevice = getPhysicalDevice();
		ColorFormatAndSpace colorFormat = getColorFormat(window, physicalDevice);
		int renderQueueFamilyIndex = getRenderQueueFamilyIndex(window, physicalDevice);
		VkDevice device = getLogicalDevice(physicalDevice, renderQueueFamilyIndex);
		VkQueue renderQueue = getDeviceQueue(device, renderQueueFamilyIndex, 0);
		
		// Checking the window support
		checkSupport(window, physicalDevice, renderQueueFamilyIndex);
		
		RenderPass renderPass = createRenderPass(device, colorFormat.colorFormat);
		destroy.add(renderPass);
		long pipeline = createPipeline(physicalDevice, device, renderPass);
		
		Recordable cmdPreset = makePreset(window);
		Recordable cmdWork = makeWorkRecordable(physicalDevice, device, pipeline);
		renderPass.setPreset(cmdPreset);
		renderPass.setWork(cmdWork);
		
		CommandBufferFactory basicCMD = new CommandBufferFactory(device, renderPass, renderQueueFamilyIndex, 0);
		BasicSwapchainFactory swapchainFactory = new BasicSwapchainFactory(physicalDevice, colorFormat);
		BasicFramebufferFactory fbFactory = new BasicFramebufferFactory(device, renderPass.handle());
		destroy.add(fbFactory);
		
		ImageViewCreateInfo imageInfo = getImageViewCreateInfo();
		Renderer winRenderer = new Renderer(window, device, renderQueue, imageInfo.getInfo(), basicCMD, swapchainFactory, fbFactory);
		
		// Creating rendering task
		RenderingTask renderingTask = new RenderingTask(winRenderer);
		engine.addTickTask(renderingTask);
		tickTasks.add(renderingTask);
		
		window.setVisible(true);
		
		destroy.add(winRenderer);
	}
	
	@Override
	public void destroy() {
		for (EngineTask task : tickTasks)
			engine.removeTickTask(task);
		
		viewport.free();
		scissor.free();
		
		destroy.destroy();
	}

	/**
	 * Chooses the most appropriate physical device(GPU).
	 * Discrete GPUs are preferred and if it is available
	 * it will be returned. 
	 * 
	 * @return the physical device.
	 */
	private static VkPhysicalDevice getPhysicalDevice() {
		VkPhysicalDevice physicalDevice = HardwareManager.getBestPhysicalDevice(new PhysicalDeviceJudge() {

			@Override
			public int score(VkPhysicalDevice device) {

				VkPhysicalDeviceProperties props = VkPhysicalDeviceProperties.calloc();
				vkGetPhysicalDeviceProperties(device, props);
				
				int out = 0;
				
				if (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU)
					out = 1000;
				else if (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU)
					out = 100;
				
				props.free();
				return out;
			}
			
		});
		
		return physicalDevice;
	}

	/**
	 * Obtains suitable color format.
	 * 
	 * @param window			
	 * @param physicalDevice	
	 * @return
	 */
	private static ColorFormatAndSpace getColorFormat(Window window, VkPhysicalDevice physicalDevice) {
		ColorFormatAndSpace colorFormat = new ColorFormatAndSpace(0, 0);
		try {
			colorFormat = RenderUtil.getNextColorFormatAndSpace(0, physicalDevice, window.getSurface(), VK_FORMAT_B8G8R8A8_UNORM, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
		} catch (VulkanException e) {
			e.printStackTrace();
			throw new AssertionError("Failed to choose color format and space.");
		}
		
		return colorFormat;
	}
	
	/**
	 * Returns an index to the most suitable queue family.
	 * According to the {@link HardwareManager.HARDWARE_CFG} file.
	 * 
	 * @param window
	 * @param physicalDevice
	 * @return
	 */
	private static int getRenderQueueFamilyIndex(Window window, VkPhysicalDevice physicalDevice) {
		int renderQueueFamilyIndex = 0;
		
		try {
			renderQueueFamilyIndex = HardwareManager.getMostSuitableQueueFamily(physicalDevice);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
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
	private static VkDevice getLogicalDevice(VkPhysicalDevice physicalDevice, int renderQueueFamilyIndex) {

		FloatBuffer queuePriorities = memAllocFloat(1);
		queuePriorities.put(0.0f);
		queuePriorities.flip();
		
		List<String> extensions = new ArrayList<String>();
		extensions.add(VK_KHR_SWAPCHAIN_EXTENSION_NAME);

		VkDevice device = null;
		
		try {
			device = createLogicalDevice(physicalDevice, renderQueueFamilyIndex, queuePriorities, 0, null, extensions);
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
	private static void checkSupport(Window window, VkPhysicalDevice physicalDevice, int renderQueueFamilyIndex) {
		IntBuffer pSupported = memAllocInt(1);
		int err = vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, renderQueueFamilyIndex, window.getSurface(), pSupported);
		
		try {
			validate(err, "Failed to check device KHR surface suport.");
		} catch (VulkanException e1) {
			e1.printStackTrace();
		}
		
		if (pSupported.get(0) != VK_TRUE)
			throw new AssertionError("Device does not support the khr swapchain.");
		
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
		VkAttachmentReference.Buffer colorReference = VkAttachmentReference.calloc(1)
				.attachment(0)
				.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
		
		VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1)
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
			attachments = new Attachments(Application.getConfigAssets().getSubAsset("RenderPass1"), "attachment01.cfg");
			renderPass = new RenderPass(logicalDevice, attachments, subpass, null);
		} catch (AssertionError | IOException | VulkanException e) {
			e.printStackTrace();
			throw new AssertionError("Failed to create render pass.");
		}
		
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
	private static long createPipeline(VkPhysicalDevice physicalDevice, VkDevice device, RenderPass renderPass) {
		long pipeline = VK_NULL_HANDLE;

		try {
			long rpHandle = renderPass.handle();
			
			//renderingPipeline.getGraphicsPipeline();
			// Create the pipeline layout that is used to generate the rendering pipelines that
			// are based on this descriptor set layout
			VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc()
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
			 
			GraphicsPipeline gp = new GraphicsPipeline(Application.getConfigAssets().getSubAsset("pipeline"), "pipeline.cfg", device, rpHandle, layout);
			
			pipeline = gp.getPipelineHandle();
		} catch (VulkanException | IOException | AssertionError e) {
			e.printStackTrace();
			throw new AssertionError("Failed to create pipeline.");
		}
		
		return pipeline;
	}
	
	/**
	 * Creates a basic Recordable that will initialize
	 * the command recording process.
	 * 
	 * @param window
	 * @return
	 */
	private Recordable makePreset(Window window) {		
		// Update dynamic viewport state
		viewport = VkViewport.calloc(1)
				.width(window.getWidth())
				.height(window.getHeight())
				.minDepth(0.0f)
				.maxDepth(1.0f)
				.x(0f)
				.y(0f);
		
		//Update dynamic scissor state
		scissor = VkRect2D.calloc(1);
		scissor.extent().set(window.getWidth(), window.getHeight());
		scissor.offset().set(0, 0);
		
		Recordable out = new Recordable() {

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
	private static Recordable makeWorkRecordable(VkPhysicalDevice physicalDevice, VkDevice device, Long pipeline) {
		
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
		
		Recordable recCmd = new Recordable() {

			@Override
			public void record(VkCommandBuffer buffer) {
				
				//Bind the rendering pipeline (including the shaders)
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
