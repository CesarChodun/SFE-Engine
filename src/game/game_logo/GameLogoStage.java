package game.game_logo;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static core.rendering.RenderUtil.*;
import static core.result.VulkanResult.validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.json.JSONException;
import org.lwjgl.PointerBuffer;
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
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkViewport;

import core.Application;
import core.Engine;
import core.EngineTask;
import core.HardwareManager;
import core.hardware.PhysicalDeviceJudge;
import core.rendering.ColorFormatAndSpace;
import core.rendering.RenderUtil;
import core.rendering.Renderer;
import core.rendering.Window;
import core.rendering.factories.CommandBufferFactory;
import core.rendering.factories.SwapchainFactory;
import core.rendering.recording.Recordable;
import core.resources.Asset;
import core.result.VulkanException;
import demos.PrimitiveDemo.Vertices;
import game.GameStage;
import game.factories.BasicFramebufferFactory;
import game.factories.BasicSwapchainFactory;
import game.rendering.BasicPipeline;
import game.rendering.GLFWTask;
import game.rendering.RenderingPipeline;
import game.rendering.RenderingTask;
import game.rendering.WindowTask;
import game.rendering.WindowTask.WindowCloseCallback;
import geometry.Mesh;

public class GameLogoStage implements GameStage{
	
	private GameLogoWindow window;
	private Engine engine;
	
	private Mesh mesh;
	
	@Deprecated
	private void makeMesh() {
		List<Vector3f> ver = new ArrayList<Vector3f>();
		ver.add(new Vector3f(0, 0, 0));
		ver.add(new Vector3f(1, 0, 0));
		ver.add(new Vector3f(1, 1, 0));
		
		mesh = new Mesh(ver);
	}
	
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
	
	//TODO automate
	private long createRenderPass(VkDevice logicalDevice, int colorFormat) throws VulkanException {
		VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(1)
				.format(colorFormat)
				.samples(VK_SAMPLE_COUNT_1_BIT)
				.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
				.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
				.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
				.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
				.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)//VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
				.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);//VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);//VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);//
		
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
		
		VkRenderPassCreateInfo pCreateInfo = VkRenderPassCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
				.pNext(NULL)
				.pAttachments(attachments)
				.pSubpasses(subpass)
				.pDependencies(null);
		
		LongBuffer pRenderPass = memAllocLong(1);
		int err = vkCreateRenderPass(logicalDevice, pCreateInfo, null, pRenderPass);
		validate(err, "Failed to create render pass!");
		
		long handle = pRenderPass.get(0);
		
		memFree(pRenderPass);
		attachments.free();
		colorReference.free();
		subpass.free();
		pCreateInfo.free();
		
		return handle;
	}
	
	private Recordable makeRecordable(VkDevice device, VkPhysicalDevice physicalDevice, Long pipeline) {
		
		Recordable recCmd = new Recordable() {

			@Override
			public void record(VkCommandBuffer buffer) {
				// Update dynamic viewport state
//				VkViewport.Buffer viewport = VkViewport.calloc(1)
//						.height(window.getWindow().getHeight())
//						.width(window.getWindow().getWidth())
//						.minDepth(0.0f)
//						.maxDepth(1.0f);
//				vkCmdSetViewport(buffer, 0, viewport);
//				viewport.free();
				
				//Update dynamic scissor state
//				VkRect2D.Buffer scissor = VkRect2D.calloc(1);
//				scissor.extent().set(window.getWindow().getHeight(), window.getWindow().getWidth());
//				scissor.offset().set(0, 0);
//				vkCmdSetScissor(buffer, 0, scissor);
//				scissor.free();
				
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
	
//	@Deprecated
//	private static long createRenderPass(VkDevice device, ColorFormatAndSpace colorFormatAndSpace, int samples, int depthType) throws VulkanException {
//		//Attachments:
//		VkAttachmentDescription.Buffer attachmentDescription = VkAttachmentDescription.calloc(1);//2
//		attachmentDescription.get(0)
//				.format(colorFormatAndSpace.colorFormat)
//				.samples(samples)
//				.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
//				.storeOp(VK_ATTACHMENT_STORE_OP_STORE)
//				.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
//				.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
//				.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)//TODO: VK_IMAGE_LAYOUT_UNDEFINED/--/VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
//				.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);//TODO: VK_IMAGE_LAYOUT_PRESENT_SRC_KHR/--/VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
//				
////		attachmentDescription.get(1)
////				.flags(0)
////				.format(depthType)
////				.samples(samples)
////				.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
////				.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
////				.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE)
////				.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
////				.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
////				.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
//		
//		//Subpass:
//		VkAttachmentReference.Buffer colorReference = VkAttachmentReference.calloc(1)
//			.attachment(0)
//			.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
//		VkAttachmentReference depthReference = VkAttachmentReference.calloc()
//			.attachment(1)
//			.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
//		
//		VkSubpassDescription.Buffer subpassDescription = VkSubpassDescription.calloc(1)
//				.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
//				.flags(0)
//				.pInputAttachments(null)
//				.colorAttachmentCount(colorReference.remaining())
//				.pColorAttachments(colorReference)
//				.pResolveAttachments(null)
//				.pDepthStencilAttachment(depthReference)
//				.pPreserveAttachments(null);
//		
//		VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.calloc()
//				.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
//				.pNext(NULL)
//				.pAttachments(attachmentDescription)
//				.pSubpasses(subpassDescription)
//				.pDependencies(null);
//		
//		//Render pass:
//		LongBuffer pRenderPass = memAllocLong(1);
//		int err = vkCreateRenderPass(device, renderPassCreateInfo, null, pRenderPass);
//		validate(err, "Failed to create render pass!");
//		
//		long renderPass = pRenderPass.get(0);
//		memFree(pRenderPass);
//		
//		attachmentDescription.free();
//		colorReference.free();
//		depthReference.free();
//		subpassDescription.free();
//		renderPassCreateInfo.free();
//		
//		return renderPass;
//	}
	
	
	
	@Override
	public void present() throws GameStageException {
		// TODO Auto-generated method stub
		
		makeMesh();
		
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

		long renderPass = VK_NULL_HANDLE;
		try {
			renderPass = createRenderPass(device, colorFormat.colorFormat);
		} catch (VulkanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		float[] cv = new float[4];
		cv[0] = 1.0f;
		cv[1] = 1.0f;
		cv[2] = 1.0f;
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
			pipeline = BasicPipeline.createPipeline(physicalDevice, device, renderPass);//renderingPipeline.getGraphicsPipeline();
		} catch (VulkanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Recordable cmdRecord = makeRecordable(device, physicalDevice, pipeline);
		CommandBufferFactory basicCMD = new CommandBufferFactory(device, cmdRecord, renderPass, renderQueueFamilyIndex, bufferFlags, cv);
		BasicSwapchainFactory swapchainFactory = new BasicSwapchainFactory(physicalDevice, device, colorFormat);
		BasicFramebufferFactory fbFactory = new BasicFramebufferFactory(device, renderPass);
		
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
