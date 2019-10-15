package game.game_logo;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static core.rendering.RenderUtil.*;
import static core.result.VulkanResult.validate;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONException;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
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
import core.result.VulkanException;
import game.GameStage;
import game.factories.BasicFramebufferFactory;
import game.factories.BasicSwapchainFactory;
import game.rendering.BasicPipeline;
import game.rendering.GLFWTask;
import game.rendering.RenderingTask;
import game.rendering.WindowTask;
import game.rendering.WindowTask.WindowCloseCallback;
import rendering.config.Attachments;
import rendering.recording.RenderPass;

public class GameLogoStage implements GameStage{
	
	private GameLogoWindow window;
	private Engine engine;
	
	public GameLogoStage(Engine engine) {
		this.engine = engine;
	}
	
	public static void initializeHardware() throws VulkanException, IOException {
		
		// Game data initialization(in the current folder)
		Application.init(new File(""));
		
		//Initialize hardware
		HardwareManager.init(Application.getApplicationInfo(), Application.getConfigAssets());
		
		
	}

	@Override
	public void initialize() throws InitializationException {
		// TODO Auto-generated method stub
		
		try {
			initializeHardware();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InitializationException("Failed to initialize hardware!");
		}
		
		try {
			window = new GameLogoWindow(Application.getConfigAssets());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VulkanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class WindowShutDown implements EngineTask {

		private Window window;
		private EngineTask windowTask;
		
		public WindowShutDown(Window window, WindowTask windowTask) {
			this.window = window;
			this.windowTask = windowTask;
		}
		
		@Override
		public void run() throws AssertionError {
			engine.removeTickTask(windowTask);
			window.setVisible(false);
			window.destroyWindow();
			engine.stop();//TODO check
		}
		
	}
	
	//TODO automate subpasses?
	private RenderPass createRenderPass(VkDevice logicalDevice, int colorFormat) throws VulkanException, AssertionError, IOException {
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
		
		Attachments attachments = new Attachments(Application.getConfigAssets().getSubAsset("RenderPass1"), "attachment01.cfg");
		
		
		return new RenderPass(logicalDevice, attachments, subpass, null);
	}
	
	private Recordable makeRecordable(VkDevice device, VkPhysicalDevice physicalDevice, Long pipeline) {
		
		Recordable recCmd = new Recordable() {

			@Override
			public void record(VkCommandBuffer buffer) {
				// Update dynamic viewport state
				VkViewport.Buffer viewport = VkViewport.calloc(1)
						.width(window.getWindow().getWidth())
						.height(window.getWindow().getHeight())
						.minDepth(0.0f)
						.maxDepth(1.0f)
						.x(0f)
						.y(0f);
				vkCmdSetViewport(buffer, 0, viewport);
				viewport.free();
				
				//Update dynamic scissor state
				VkRect2D.Buffer scissor = VkRect2D.calloc(1);
				scissor.extent().set(window.getWindow().getWidth(), window.getWindow().getHeight());
				scissor.offset().set(0, 0);
				vkCmdSetScissor(buffer, 0, scissor);
				scissor.free();
				
				//Bind the rendering pipeline (including the shaders)
				vkCmdBindPipeline(buffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
				
				// Bind triangle vertices
				LongBuffer offsets = memAllocLong(1);
				offsets.put(0, 0L);
				LongBuffer pBuffers = memAllocLong(1);
				pBuffers.put(0, BasicPipeline.verticesBuffer);//mesh.getVkVertices(device, physicalDevice).getVerticesBuffer()
				vkCmdBindVertexBuffers(buffer, 0, pBuffers, offsets);
				
				memFree(pBuffers);
				memFree(offsets);
				
				//Draw triangle
//				if(!quad)
					vkCmdDraw(buffer, 3, 1, 0, 0);//TODO check
//				else
//					vkCmdDraw(buffer, 6, 1, 0, 0);
			}
			
		};
		
		return recCmd;
	}
	
	
	@Override
	public void present() throws GameStageException {
		
		VkPhysicalDevice physicalDevice = HardwareManager.getBestPhysicalDevice(new PhysicalDeviceJudge() {

			@Override
			public int score(VkPhysicalDevice device) {

				VkPhysicalDeviceProperties props = VkPhysicalDeviceProperties.calloc();
				vkGetPhysicalDeviceProperties(device, props);
				if (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU)
					return 1000;
				else if (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU)
					return 100;
				
				props.free();
				return 0;
			}
			
		});
		
		//TODO
		ColorFormatAndSpace colorFormat = new ColorFormatAndSpace(0, 0);

		int renderQueueFamilyIndex = 0;
		try {
			colorFormat = RenderUtil.getNextColorFormatAndSpace(0, physicalDevice, window.getWindow().getSurface(), VK_FORMAT_B8G8R8A8_UNORM, VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
			renderQueueFamilyIndex = HardwareManager.getMostSuitableQueueFamily(physicalDevice);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | VulkanException e) {
			e.printStackTrace();
		}//TODO
		int bufferFlags = 0;//TODO automate

		FloatBuffer queuePriorities = memAllocFloat(1);
		queuePriorities.put(0.0f);
		queuePriorities.flip();
		VkDevice device = null;
		
		List<String> extensions = new ArrayList<String>();
		extensions.add(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
		
		try {
			device = createLogicalDevice(physicalDevice, renderQueueFamilyIndex, queuePriorities, 0, null, extensions);
		} catch (VulkanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //TODO
//		memFree(queuePriorities);
		VkQueue queue = getDeviceQueue(device, renderQueueFamilyIndex, 0);//TODO
		

		IntBuffer pSupported = memAllocInt(1);
		int err = vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, renderQueueFamilyIndex, window.getWindow().getSurface(), pSupported);
		try {
			validate(err, "Failed to check device KHR surface suport.");
		} catch (VulkanException e1) {
			e1.printStackTrace();
		}
		if (pSupported.get(0) != VK_TRUE)
			throw new AssertionError("Device does not support the khr swapchain.");

		RenderPass renderPass = null;
		try {
			renderPass = createRenderPass(device, colorFormat.colorFormat);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (renderPass == null)
			throw new AssertionError("Failed to create render pass!");
		
		float[] cv = new float[4];
		cv[0] = 0.08f;
		cv[1] = 0.10f;
		cv[2] = 0.16f;
		cv[3] = 1.0f;
		
//		//Make render pass
//		long renderPass = createRenderPass(device, colorFormat, VK_SAMPLE_COUNT_1_BIT, VK_FORMAT_D16_UNORM);
		
		VkImageViewCreateInfo ivInfo = VkImageViewCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.viewType(VK_IMAGE_VIEW_TYPE_2D)
				.format(colorFormat.colorFormat);
		ivInfo.components()
			.r(VK_COMPONENT_SWIZZLE_R)
			.g(VK_COMPONENT_SWIZZLE_G)
			.b(VK_COMPONENT_SWIZZLE_B)
			.a(VK_COMPONENT_SWIZZLE_A);
		ivInfo.subresourceRange()
		.aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
		.baseMipLevel(0)
		.layerCount(1)
		.baseArrayLayer(0)
		.levelCount(VK_REMAINING_MIP_LEVELS);
		
//		RenderingPipeline renderingPipeline;
		long pipeline = VK_NULL_HANDLE;
		try {
//			renderingPipeline = new RenderingPipeline(device, renderPass);
			long rpHandle = VK_NULL_HANDLE;
			if (renderPass != null)
				rpHandle = renderPass.handle();
			
			pipeline = BasicPipeline.createPipeline(physicalDevice, device, rpHandle);//renderingPipeline.getGraphicsPipeline();
		} catch (VulkanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Recordable cmdRecord = makeRecordable(device, physicalDevice, pipeline);
		renderPass.setWork(cmdRecord);
		CommandBufferFactory basicCMD = new CommandBufferFactory(device, renderPass, renderQueueFamilyIndex, bufferFlags, cv);
		BasicSwapchainFactory swapchainFactory = new BasicSwapchainFactory(physicalDevice, device, colorFormat);
		BasicFramebufferFactory fbFactory = new BasicFramebufferFactory(device, renderPass.handle());
		
		Renderer winRenderer = new Renderer(window.getWindow(), device, queue, ivInfo, basicCMD, swapchainFactory, fbFactory);
		

//		ivInfo.free();
		
		window.show();
		long ID = window.getWindow().getWindowID();
		
		//GLFW task
		GLFWTask task = new GLFWTask();
		engine.addTickTask(task);

		//Vulkan rendering task
		RenderingTask renderingTask = new RenderingTask(winRenderer);
		engine.addTickTask(renderingTask);
		
		
		//Window task
		WindowTask windowTask = new WindowTask(ID);
		windowTask.setCloseCall(new WindowCloseCallback() {

			@Override
			public void close(long windowID) {
				WindowShutDown sdOperation = new WindowShutDown(window.getWindow(), windowTask);
				engine.addTask(sdOperation);
			}
			
		});
		engine.addTickTask(windowTask);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
//		window.destroy();
	}

}
