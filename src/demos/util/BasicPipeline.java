package demos.util;

import static core.result.VulkanResult.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import core.result.VulkanException;

public class BasicPipeline {
	
	private static final boolean quad = false;
	
	public static ByteBuffer vertexBuffer;
	public static long verticesBuffer;
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 			Returns memory type that meets requirements.
	 * </p>
	 * @param memoryProperties	- Memory properties.
	 * @param bits				- Interesting indices.
	 * @param properties		- Properties that memory type should meet.
	 * @param typeIndex			- Integer buffer for returned value.
	 * @return					- Information about successfulness of the operation(true - success, false - fail).
	 */
	 public static boolean getMemoryType(VkPhysicalDeviceMemoryProperties memoryProperties, int bits, int properties, IntBuffer typeIndex) {
		 for(int i = 0; i < 32; i++) 
			 if((bits & (1<<i)) > 0)
				 if((memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
					 typeIndex.put(0, i);
					 return true;
				 }
		 
		 return false;
	 }

	public static long createPipeline(VkPhysicalDevice physicalDevice, VkDevice logicalDevice, long renderPass) throws VulkanException {
		
		VkPhysicalDeviceMemoryProperties pMemoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
		vkGetPhysicalDeviceMemoryProperties(physicalDevice, pMemoryProperties);
		
		 if(quad)
			 vertexBuffer = memAlloc(6*2*4);
		 else
			 vertexBuffer = memAlloc(3*2*4);
		 FloatBuffer fb = vertexBuffer.asFloatBuffer();
		 
		 
		 if(quad) {
			 fb.put(-0.5f).put(-0.5f);
			 fb.put(0.5f).put(-0.5f);
			 fb.put(-0.5f).put(0.5f);
			 fb.put(-0.5f).put(0.5f);
			 fb.put(0.5f).put(-0.5f);
			 fb.put(0.5f).put(0.5f);
		 }
		 else {
			 fb.put(-0.5f).put(-0.5f);
			 fb.put( 0.5f).put(-0.5f);
			 fb.put( 0.0f).put( 0.5f);
		 }
		 
		 VkMemoryAllocateInfo memAlloc = VkMemoryAllocateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
				 .pNext(NULL)
				 .allocationSize(0)
				 .memoryTypeIndex(0);
		 VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
		 
		 VkBufferCreateInfo bufInfo = VkBufferCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
				 .pNext(NULL)
				 .usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
				 .size(vertexBuffer.remaining())
				 .flags(0);
		 
		 LongBuffer pBuffer = memAllocLong(1);
		 int err = vkCreateBuffer(logicalDevice, bufInfo, null, pBuffer);
		 validate(err, "Failed to create buffer!");
		 verticesBuffer = pBuffer.get(0);
		 memFree(pBuffer);
		 bufInfo.free();
		 
		 vkGetBufferMemoryRequirements(logicalDevice, verticesBuffer, memReqs);
		 memAlloc.allocationSize(memReqs.size());
		 IntBuffer memoryTypeIndex = memAllocInt(1);
		 if(!getMemoryType(pMemoryProperties, memReqs.memoryTypeBits(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, memoryTypeIndex))
			 throw new AssertionError("Failed to obtain memry type.");
		 memAlloc.memoryTypeIndex(memoryTypeIndex.get(0));
		 memFree(memoryTypeIndex);
		 memReqs.free();
		 pMemoryProperties.free();
		 
		 LongBuffer pMemory = memAllocLong(1);
		 err = vkAllocateMemory(logicalDevice, memAlloc, null, pMemory);
		 long memory = pMemory.get(0);
		 validate(err, "Failed to alocate vertex memory!");
		 memFree(pMemory);
		 
		 PointerBuffer pData = memAllocPointer(1);
		 err = vkMapMemory(logicalDevice, memory, 0, memAlloc.allocationSize(), 0, pData);
		 memAlloc.free();
		 long data = pData.get(0);
		 memFree(pData);
		 validate(err, "Failed to map memory!");
		 MemoryUtil.memCopy(memAddress(vertexBuffer), data, vertexBuffer.remaining());
		 memFree(vertexBuffer);
		 vkUnmapMemory(logicalDevice, memory);
		 err = vkBindBufferMemory(logicalDevice, verticesBuffer, memory, 0);
		 validate(err, "Failed to bind memory to vertex buffer!");
		 
		 //Binding:
		 VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1)
				 .binding(0) // <- we bind our vertex buffer to point 0
				 .stride(2*4)
				 .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
		 
		 // Attribute descriptions
		 // Describes memory layout and shader attribute locations
		 VkVertexInputAttributeDescription.Buffer attributeDescription = VkVertexInputAttributeDescription.calloc(1);
		 //Location 0 : Position
		 attributeDescription.get(0)
		 		.binding(0)
		 		.location(0)
		 		.format(VK_FORMAT_R32G32_SFLOAT)
		 		.offset(0);
		 
		 //asign to vertex buffer
		 VkPipelineVertexInputStateCreateInfo vertexCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
				 .pNext(NULL)
				 .pVertexBindingDescriptions(bindingDescription)
				 .pVertexAttributeDescriptions(attributeDescription);
		
		 // Vertex input state
	     // Describes the topoloy used with this pipeline
		 VkPipelineInputAssemblyStateCreateInfo inputAssemblyInfo = VkPipelineInputAssemblyStateCreateInfo.calloc()
		 	.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
		 	.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
		 	.primitiveRestartEnable(false);
		 
		 //Rasterization state
		 VkPipelineRasterizationStateCreateInfo rasterizationStateInfo = VkPipelineRasterizationStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
				 .polygonMode(VK_POLYGON_MODE_FILL)
				 .cullMode(VK_CULL_MODE_NONE)
				 .frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE)
				 .lineWidth(1.0f)
				 .depthClampEnable(false)
				 .rasterizerDiscardEnable(false)
				 .depthBiasEnable(false);
		 
		 // Color blend state
		 // Describes blend modes and color masks
		 VkPipelineColorBlendAttachmentState.Buffer colorWriteMask = VkPipelineColorBlendAttachmentState.calloc(1)
				 .blendEnable(false)
				 .colorWriteMask(0xF);
		 VkPipelineColorBlendStateCreateInfo colorBlendStateInfo = VkPipelineColorBlendStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
				 .pAttachments(colorWriteMask);
		 
		 // Viewport state
		 VkPipelineViewportStateCreateInfo viewportStateCreateInfo = VkPipelineViewportStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
				 .viewportCount(1) // <- one viewport
				 .scissorCount(1); // <- one scissor rectangle
		 
		// Enable dynamic states
	        // Describes the dynamic states to be used with this pipeline
	        // Dynamic states can be set even after the pipeline has been created
	        // So there is no need to create new pipelines just for changing
	        // a viewport's dimensions or a scissor box
		 IntBuffer dynamicStates = memAllocInt(2);
		 dynamicStates.put(VK_DYNAMIC_STATE_VIEWPORT).put(VK_DYNAMIC_STATE_SCISSOR).flip();
		 VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.calloc()
				 // The dynamic state properties themselves are stored in the command buffer
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
				 .pDynamicStates(dynamicStates);//dynamicStates
		 
		 //Depth and stencil state
	     // Describes depth and stenctil test and compare ops
		 VkPipelineDepthStencilStateCreateInfo depthStencilState = VkPipelineDepthStencilStateCreateInfo.calloc()
				 // No depth test/write and no stencil used 
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
				 .depthTestEnable(false)
				 .depthWriteEnable(false)
				 .depthCompareOp(VK_COMPARE_OP_ALWAYS)
				 .depthBoundsTestEnable(false)
				 .stencilTestEnable(false);
		 depthStencilState.back()
		 .failOp(VK_STENCIL_OP_KEEP)
		 .passOp(VK_STENCIL_OP_KEEP)
		 .compareOp(VK_COMPARE_OP_ALWAYS);
		 depthStencilState.front(depthStencilState.back());
		 
		 //Multi sampling state
		// No multi sampling used in this example
		 VkPipelineMultisampleStateCreateInfo multisampleState = VkPipelineMultisampleStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
				 .pSampleMask(null)
				 .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
		 
		 //Load shaders
		 VkPipelineShaderStageCreateInfo.Buffer stages = VkPipelineShaderStageCreateInfo.calloc(2);
		
		 loadShaderStage(
				 stages.get(0), 
				 createShaderModule(
						 logicalDevice, 
						 new File("storage/res/shaders/triangle.vert.spv")
						 ), 
				 VK_SHADER_STAGE_VERTEX_BIT, "main");
		 
		 loadShaderStage(
				 stages.get(1), 
				 createShaderModule(
						 logicalDevice, 
						 new File("storage/res/shaders/triangle.frag.spv")
						 ), 
				 VK_SHADER_STAGE_FRAGMENT_BIT, 
				 "main");
						 
		 // Create the pipeline layout that is used to generate the rendering pipelines that
	     // are based on this descriptor set layout
		 VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
				 .pNext(NULL)
				 .pSetLayouts(null)
				 .pPushConstantRanges(null);
		 
		 LongBuffer pLayout = memAllocLong(1);
		 err = vkCreatePipelineLayout(logicalDevice, pipelineLayoutCreateInfo, null, pLayout);
		 validate(err, "Failed to create pipeline layout.");
		 long layout = pLayout.get(0);
		 memFree(pLayout);
		 pipelineLayoutCreateInfo.free();
		 
		 VkGraphicsPipelineCreateInfo.Buffer pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1)
				 .sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
				 .layout(layout)	// <- the layout used for this pipeline (NEEDS TO BE SET! even though it is basically empty)
				 .renderPass(renderPass)// <- renderpass this pipeline is attached to
				 .pVertexInputState(vertexCreateInfo)
				 .pInputAssemblyState(inputAssemblyInfo)
				 .pTessellationState(null)
				 .pRasterizationState(rasterizationStateInfo)
				 .pColorBlendState(colorBlendStateInfo)
				 .pMultisampleState(multisampleState)
				 .pViewportState(viewportStateCreateInfo)
				 .pDepthStencilState(depthStencilState)
				 .pStages(stages)
				 .pDynamicState(dynamicState)
//				 .basePipelineHandle(VK_NULL_POINTER) //Default value
//				 .basePipelineIndex(0)	//Default value
				 ;
		 
		 LongBuffer pPipeline = memAllocLong(1);
		 err = vkCreateGraphicsPipelines(logicalDevice, VK_NULL_HANDLE, pipelineCreateInfo, null, pPipeline);
		 long pipelineHandle = pPipeline.get(0);
		 memFree(pPipeline);
		 vertexCreateInfo.free();
		 inputAssemblyInfo.free();
		 rasterizationStateInfo.free();
		 colorWriteMask.free();
		 colorBlendStateInfo.free();
		 viewportStateCreateInfo.free();
		 memFree(dynamicStates);
		 dynamicState.free();
		 depthStencilState.free();
		 multisampleState.free();
		 
		 bindingDescription.free();
		 attributeDescription.free();
		 
		 for (int i = 0; i < stages.remaining(); i++)
			 memFree(stages.get(i).pName());
		 stages.free();
		 
		 pipelineCreateInfo.free();
		 
		 return pipelineHandle;
	 }
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Converts file content to byte buffer.
	 * </p>
	 * @param file
	 * @return
	 */
	public static ByteBuffer fileToByteBuffer(File file) {
		 if(file.isDirectory())
			 return null;
		 
		 ByteBuffer buffer = null; 
		 
		 try {
			 FileInputStream fis = new FileInputStream(file);
			 FileChannel fc = fis.getChannel();
			 buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			 fc.close();
			 fis.close();

		 } catch (IOException e) {
			 e.printStackTrace();
		 }
		 
		 return buffer;
	 }
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Creates shader stage.
	 * </p>
	 * @param shaderModule	- Shader module.
	 * @param stage			- Shader stage.
	 * @param invokeName	- Name of the method to be invoked.
	 * @return
	 */
	public static VkPipelineShaderStageCreateInfo createShaderStage(long shaderModule, int stage, String invokeName) {
		 VkPipelineShaderStageCreateInfo shaderStage = VkPipelineShaderStageCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
				 .pNext(NULL)
				 .stage(stage)
				 .module(shaderModule)
				 .pName(memUTF8(invokeName));
		 return shaderStage;
	 }
	

	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Creates shader stage.
	 * </p>
	 * @param shaderModule	- Shader module.
	 * @param stage			- Shader stage.
	 * @param invokeName	- Name of the method to be invoked.
	 * @return
	 */
	public static void loadShaderStage(
			VkPipelineShaderStageCreateInfo info, 
			long shaderModule, 
			int stage, 
			String invokeName
			) {
		
		 info
			 .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
			 .pNext(NULL)
			 .stage(stage)
			 .module(shaderModule)
			 .pName(memUTF8(invokeName));
	 }
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Creates a shader module.
	 * </p>
	 * @param logicalDevice
	 * @param file
	 * @return
	 * @throws VulkanException 
	 */
	public static long createShaderModule(VkDevice logicalDevice, File file) throws VulkanException {
		ByteBuffer shaderData = fileToByteBuffer(file);
	 
		VkShaderModuleCreateInfo moduleCreateInfo  = VkShaderModuleCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
				 .flags(0)
				 .pNext(NULL)
				 .pCode(shaderData);
		 
		LongBuffer pShaderModule = memAllocLong(1);
		int err = vkCreateShaderModule(logicalDevice, moduleCreateInfo, null, pShaderModule);
		validate(err, "Failed to create shader module!");
		long handle = pShaderModule.get(0);
		 
		memFree(pShaderModule);
		moduleCreateInfo.free();
		
		return handle;
	 }
}
