package game.rendering;

import static core.result.VulkanResult.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ZERO;
import static org.lwjgl.vulkan.VK10.VK_BLEND_OP_ADD;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_ALWAYS;
import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS_OR_EQUAL;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_NONE;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_SCISSOR;
import static org.lwjgl.vulkan.VK10.VK_DYNAMIC_STATE_VIEWPORT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_NO_OP;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STENCIL_OP_KEEP;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;

import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
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
import org.lwjgl.vulkan.VkStencilOpState;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import core.result.VulkanException;

public class RenderingPipeline {
	
	private VkPipelineShaderStageCreateInfo.Buffer PSSCreateInfo;
	private VkVertexInputBindingDescription.Buffer vertexBinding;
	private VkVertexInputAttributeDescription.Buffer vertexAttribute;

	private static long descriptorSetLayoutA, descriptorSetLayoutB;
	
	private VkDevice device;
	private long pipelineLayout;
	private long renderPass;
	private long graphicsPipeline;
	
	public RenderingPipeline(VkDevice device, long renderPass) throws VulkanException {
		this.device = device;
		this.renderPass = renderPass;
		
		createShaders();
		createMeshDescription();
		createDescriptorSetLayout();
		createPipelineLayout();
		createPipeline();
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
	
	public void createShaders() throws VulkanException {
		PSSCreateInfo = VkPipelineShaderStageCreateInfo.calloc(2);
		
		PSSCreateInfo.put(createShaderStage(createShaderModule(device, new File("storage/res/shaders/CubeDemo/vertex.vert.spv")), VK_SHADER_STAGE_VERTEX_BIT, "main"));
		PSSCreateInfo.put(createShaderStage(createShaderModule(device, new File("storage/res/shaders/CubeDemo/fragment.frag.spv")), VK_SHADER_STAGE_FRAGMENT_BIT, "main"));
		PSSCreateInfo.flip();
	}
	
	public void createMeshDescription() {
		vertexBinding = VkVertexInputBindingDescription.calloc(1)
				.binding(0)
				.inputRate(VK_VERTEX_INPUT_RATE_VERTEX)
				.stride(6*4);//pos(x, y, z), norm(x, y, z)
		
		vertexAttribute = VkVertexInputAttributeDescription.calloc(2);
		vertexAttribute.get(0)
				.binding(0)
				.format(VK_FORMAT_R32G32B32_SFLOAT)
				.location(0)
				.offset(0);
		vertexAttribute.get(1)
				.binding(0)
				.location(1)
				.format(VK_FORMAT_R32G32B32_SFLOAT)
				.offset(3*4);
	}
	
	/**
	 * <h5>Description:</h5>
	 * <p>
	 * 		Creates descriptor set layout.
	 * </p>
	 * @param device		- Logical device
	 * @param layoutBinding	- Layout binding
	 * @return				- Handle to descriptor set layout
	 * @throws VulkanException 
	 */
	public static long createDescriptorSetLayout(VkDevice device, VkDescriptorSetLayoutBinding.Buffer layoutBinding) throws VulkanException {
		VkDescriptorSetLayoutCreateInfo layoutCreateInfo = VkDescriptorSetLayoutCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
				.pNext(NULL)
				.pBindings(layoutBinding);
		
		LongBuffer pSetLayout = memAllocLong(1);
		int err = vkCreateDescriptorSetLayout(device, layoutCreateInfo, null, pSetLayout);
		validate(err, "Filed to create descriptor set layout!");
		long ans = pSetLayout.get(0);
		memFree(pSetLayout);
		layoutCreateInfo.free();
		return ans;
	}
	
	public void createDescriptorSetLayout() throws VulkanException {
		VkDescriptorSetLayoutBinding.Buffer layoutBindingA = VkDescriptorSetLayoutBinding.calloc(1);
		layoutBindingA.get(0)
				.binding(0)		//<<--- shader binding "layout (std140, binding = VALUE) uniform NAME"
				.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
				.descriptorCount(1)
				.stageFlags(VK_SHADER_STAGE_VERTEX_BIT)
				.pImmutableSamplers(null);
		VkDescriptorSetLayoutBinding.Buffer layoutBindingB = VkDescriptorSetLayoutBinding.calloc(1);
		layoutBindingB.get(0)
		.binding(0)		//<<--- shader binding "layout (std140, binding = VALUE) uniform NAME"
		.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
		.descriptorCount(1)
		.stageFlags(VK_SHADER_STAGE_VERTEX_BIT)
		.pImmutableSamplers(null);
		
		descriptorSetLayoutA = createDescriptorSetLayout(device, layoutBindingA);
		descriptorSetLayoutB = createDescriptorSetLayout(device, layoutBindingB);
	}
	
	public void createPipelineLayout() throws VulkanException {
		LongBuffer pSetLayouts = memAllocLong(2);
		pSetLayouts.put(descriptorSetLayoutA);
		pSetLayouts.put(descriptorSetLayoutB);
		pSetLayouts.flip();
		
		VkPipelineLayoutCreateInfo pPipelineLayoutCreateInfo = VkPipelineLayoutCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.pPushConstantRanges(null)
				.pSetLayouts(pSetLayouts);

		memFree(pSetLayouts);
		
		LongBuffer pPipelineLayout = memAllocLong(1);
		int err = vkCreatePipelineLayout(device, pPipelineLayoutCreateInfo, null, pPipelineLayout);
		validate(err, "Failed to create pipeline layout!");
		
		pipelineLayout = pPipelineLayout.get(0);
		
		memFree(pPipelineLayout);
		pPipelineLayoutCreateInfo.free();
	}

	public void createPipeline() throws VulkanException {
		//Dynamic states
		IntBuffer buf = memAllocInt(2);
		buf.put(VK_DYNAMIC_STATE_VIEWPORT).put(VK_DYNAMIC_STATE_SCISSOR).flip();
		
		VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = VkPipelineDynamicStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.pDynamicStates(buf);
		
		//Vertex input state
		VkPipelineVertexInputStateCreateInfo vi = VkPipelineVertexInputStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.pVertexBindingDescriptions(vertexBinding)
				.pVertexAttributeDescriptions(vertexAttribute);
		
		VkPipelineInputAssemblyStateCreateInfo ia = VkPipelineInputAssemblyStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.primitiveRestartEnable(false)
				.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
		
		VkPipelineRasterizationStateCreateInfo rs = VkPipelineRasterizationStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.cullMode(VK_CULL_MODE_NONE)//TODO change back or front
				.depthBiasClamp(0)
				.depthBiasConstantFactor(0)
				.depthBiasEnable(false)
				.depthBiasSlopeFactor(0)
				.depthClampEnable(false)//TODO: it was set to true but not supported.
				.frontFace(VK_FRONT_FACE_CLOCKWISE)
				.lineWidth(1.0f)
				.polygonMode(VK_POLYGON_MODE_FILL)
				.rasterizerDiscardEnable(false);

		// Pipeline color blend state
		VkPipelineColorBlendAttachmentState.Buffer attachmentState = VkPipelineColorBlendAttachmentState.calloc(1)
				.blendEnable(false)
				.colorWriteMask(0xf)
				.colorBlendOp(VK_BLEND_OP_ADD)
				.alphaBlendOp(VK_BLEND_OP_ADD)
				.srcColorBlendFactor(VK_BLEND_FACTOR_ZERO)
				.dstColorBlendFactor(VK_BLEND_FACTOR_ZERO)
				.srcAlphaBlendFactor(VK_BLEND_FACTOR_ZERO)
				.dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO);
		
		FloatBuffer blendConstants = memAllocFloat(4);
		blendConstants.put(1.0f).put(1.0f).put(1.0f).put(1.0f).flip();
		
		VkPipelineColorBlendStateCreateInfo cb = VkPipelineColorBlendStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.pAttachments(attachmentState)
				.logicOpEnable(false)
				.logicOp(VK_LOGIC_OP_NO_OP)
				.blendConstants(blendConstants);
		
		// Pipeline viewport state
		VkPipelineViewportStateCreateInfo vp = VkPipelineViewportStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.viewportCount(1)
				.scissorCount(1)
				.pScissors(null)
				.pViewports(null);
		
		//Depth buffer
		VkStencilOpState stencilOP = VkStencilOpState.calloc()
				.failOp(VK_STENCIL_OP_KEEP)
				.passOp(VK_STENCIL_OP_KEEP)//VK_STENCIL_OP_KEEP
				.compareOp(VK_COMPARE_OP_ALWAYS)
				.compareMask(0)
				.reference(0)
				.depthFailOp(VK_STENCIL_OP_KEEP)
				.writeMask(0);
		
		VkPipelineDepthStencilStateCreateInfo ds = VkPipelineDepthStencilStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.depthTestEnable(true)
				.depthWriteEnable(true)
				.depthCompareOp(VK_COMPARE_OP_LESS_OR_EQUAL)//VK_COMPARE_OP_ALWAYS//VK_COMPARE_OP_LESS_OR_EQUAL
				.depthBoundsTestEnable(false)
				.minDepthBounds(0.0f)
				.maxDepthBounds(1.0f)
				.stencilTestEnable(false)
				.back(stencilOP)
				.front(stencilOP);
		
		//Multisampling
		VkPipelineMultisampleStateCreateInfo ms = VkPipelineMultisampleStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.pSampleMask(null)
				.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT)
				.sampleShadingEnable(false)
				.alphaToCoverageEnable(false)
				.alphaToOneEnable(false)
				.minSampleShading(0.0f);
		
		//Create graphics pipeline
		VkGraphicsPipelineCreateInfo.Buffer pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1)
				.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
				.pNext(NULL)
				.flags(0)
				.layout(pipelineLayout)
				.basePipelineHandle(VK_NULL_HANDLE)
				.basePipelineIndex(0)
				.pVertexInputState(vi)//vi
				.pInputAssemblyState(ia)
				.pRasterizationState(rs)
				.pColorBlendState(cb)
				.pTessellationState(null)
				.pMultisampleState(ms)
				.pDynamicState(dynamicStateCreateInfo)
				.pViewportState(vp)
				.pDepthStencilState(ds)
				.pStages(PSSCreateInfo)
				.renderPass(renderPass)
				.subpass(0);
		
		LongBuffer pGraphicsPipeline = memAllocLong(1);
		int err = vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, pipelineCreateInfo, null, pGraphicsPipeline);
		validate(err, "Failed to create graphics pipeline!");
		
		graphicsPipeline = pGraphicsPipeline.get(0);
		memFree(pGraphicsPipeline);
	}

	public long getPipelineLayout() {
		return pipelineLayout;
	}

	public void setPipelineLayout(long pipelineLayout) {
		this.pipelineLayout = pipelineLayout;
	}

	public long getGraphicsPipeline() {
		return graphicsPipeline;
	}

	public void setGraphicsPipeline(long graphicsPipeline) {
		this.graphicsPipeline = graphicsPipeline;
	}
}
