package rendering.config;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static core.result.VulkanResult.*;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineTessellationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import core.resources.Asset;
import core.resources.ConfigAsset;
import core.resources.ConfigFile;
import core.resources.Destroyable;
import core.resources.ResourceUtil;
import core.result.VulkanException;

public class GraphicsPipeline implements Destroyable{
	
	private static final String 
		VERTEX_INPUT_STATE_KEY = "pVertexInputState",
		VERTEX_INPUT_BINDINGS_KEY = "vertexInputBindings",
		VERTEX_ATTRIBUTE_BINDINGS_KEY = "vertexAttributeBindings",
		INPUT_ASSEMBLY_STATE = "inputAssemblyState",
		TESSELLATION_STATE = "tessellationState",
		RASTERIZATION_STATE = "rasterizationState",
		COLOR_BLEND_STATE = "colorBlendState",
		MULTISAMPLE_STATE = "multisampleState",
		VIEWPORT_STATE = "viewportState",
		DEPTH_STENCIL_STATE = "depthStencilState",
		STAGES = "stages",
		DYNAMIC_STATES = "dynamicStates";
	
	// Vertex Input Binding Description
	
	/** Keys for Vertex Input Binding Description. */
	private static final String
		VIB_BINDING_KEY = "binding",
		VIB_STRIDE_KEY = "stride",
		VIB_INPUT_RATE_KEY = "inputRate";
	
	private static final Integer
		DEFAULT_VIB_BINDING = 0,
		DEFAULT_VIB_STRIDE = 2 * 4;
	
	private static final String DEFAULT_VIB_INPUT_RATE = "VK_VERTEX_INPUT_RATE_VERTEX";
	
	// Vertex Input Attribute Description
		
	private static final String
		VIA_BINDING_KEY = "binding",
		VIA_LOCATION_KEY = "location",
		VIA_FORMAT_KEY = "format",
		VIA_OFFSET_KEY = "offset";
	
	private static final String DEFAULT_VIA_FORMAT = "VK_FORMAT_R32G32_SFLOAT";
	
	private static final Integer
		DEFAULT_VIA_BINDING = 0,
		DEFAULT_VIA_LOACATION = 0,
		DEFAULT_VIA_OFFSET = 0;
	
	// Input Assembly
	
	private static final String
		IA_TOPOLOGY_KEY = "topology",
		IA_PRIMITIVE_RESTART_ENABLE = "primitiveRestartEnable";
	
	private static final String DEFAULT_IA_TOPOLOGY = "VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST";
	
	private static final Boolean DEFAULT_PRIMITIVE_RESTART_ENABLE = false;
	
	// Tessellation State
	
	private static final String 
		TS_PATCH_CONTROL_POINTS_KEY = "patchControllPoints",
		OMMIT_TESSELLATION_STATE_KEY = "ommitTessellationState";
	
	private static final Boolean DEFAULT_OMMIT_TESSELLATION_STATE = true;
	
	private static final Integer DEFAULT_TS_PATCH_CONTROL_POINTS = 0;
	
	// Rasterization state
	
	private static final String
		RS_POLYGON_MODE_KEY = "polygonMode",
		RS_CULL_MODE_KEY = "cullMode",
		RS_FRONT_FACE_KEY = "frontFace",
		RS_LINE_WIDTH_KEY = "lineWidth",
		RS_DEPTH_CLAMP_ENABLE_KEY = "depthClampEnable",
		RS_RASTERIZER_DISCARD_ENABLE_KEY = "rasterizerDiscardEnable",
		RS_DEPTH_BIAS_ENABLE_KEY = "depthBiasEnable",
		RS_DEPTH_BIAS_CONSTANT_FACTOR_KEY = "depthBiasConstantFactor",
		RS_DEPTH_BIAS_CLAMP_KEY = "depthBiasClampKey",
		RS_DEPTH_BIAS_SLOPE_FACTOR_KEY = "depthBiasSlopeFactorKey";
		
	private static final Float
		DEFAULT_RS_LINE_WIDTH = 1.0f,
		DEFAULT_RS_DEPTH_BIAS_CONSTANT_FACTOR = 0.0f,
		DEFAULT_RS_DEPTH_BIAS_CLAMP = 0.0f,
		DEFAULT_RS_DEPTH_BIAS_SLOPE_FACTOR = 0.0f;
	
	private static final String
		DEFAULT_RS_POLYGON_MODE = "VK_POLYGON_MODE_FILL",
		DEFAULT_RS_CULL_MODE = "VK_CULL_MODE_NONE",
		DEFAULT_RS_FRONT_FACE = "VK_FRONT_FACE_COUNTER_CLOCKWISE";
	
	private static final Boolean
		DEFAULT_RS_DEPTH_CLAMP_ENABLE = false,
		DEFAULT_RS_RASTERIZER_DISCARD_ENABLE = false,
		DEFAULT_RS_DEPTH_BIAS_ENABLE = false;
	
	// Pipeline Color Blend State
	
	private static final String
		CB_ATTACHMENTS_KEY = "attachments",
		CB_BLEND_ENABLE_KEY = "blendEnable",
		CB_COLOR_WRITE_MASK = "colorWriteMask",
		CB_SRC_COLOR_BLEND_FACTOR_KEY = "srcColorBlendFactor",
		CB_DST_COLOR_BLEND_FACTOR_KEY = "dstColorBlendFactor",
		CB_COLOR_BLEND_OP_KEY = "colorBlendOp",
		CB_SRC_ALPHA_BLEND_FACTOR_KEY = "srcAlphaBlendFactor",
		CB_DST_ALPHA_BLEND_FACTOR_KEY = "dstAlphaBlendFactor",
		CB_ALPHA_BLEND_OP_KEY = "alphaBlendOp";
	
	private static final Boolean
		DEFAULT_CB_BLEND_ENABLE = false;
	
	private static final Integer
		DEFAULT_CB_COLOR_WRITE_MASK = 0xF;
	
	private static final String
		DEFAULT_CB_SRC_COLOR_BLEND_FACTOR = "VK_BLEND_FACTOR_ZERO",
		DEFAULT_CB_DST_COLOR_BLEND_FACTOR = "VK_BLEND_FACTOR_ZERO",
		DEFAULT_CB_COLOR_BLEND_OP = "VK_BLEND_OP_ADD",
		DEFAULT_CB_SRC_ALPHA_BLEND_FACTOR = "VK_BLEND_FACTOR_ZERO",
		DEFAULT_CB_DST_ALPHA_BLEND_FACTOR = "VK_BLEND_FACTOR_ZERO",
		DEFAULT_CB_ALPHA_BLEND_OP = "VK_BLEND_OP_ADD";
	
	// Multisample State
	
	private static final String
		MS_RASTERIZATION_SAMPLES_KEY = "rasterizationSamples",
		MS_SAMPLE_SHADING_ENABLE_KEY = "sampleShadingEnable",
		MS_MIN_SAMPLE_SHADING_KEY = "minSampleShadingKey",
		MS_ALPHA_TO_COVERAGE_ENABLE_KEY = "alphaToCoverageEnable",
		MS_ALPHA_TO_ONE_ENABLE = "alphaToOneEnable";
	
	private static final String 
		DEFAULT_MS_RASTERIZATION_SAMPLES = "VK_SAMPLE_COUNT_1_BIT";
	
	private static final Boolean
		DEFAULT_MS_SAMPLE_SHADING_ENABLE = false,
		DEFAULT_MS_ALPHA_TO_COVERAGE_ENABLE = false,
		DEFAULT_MS_ALPHA_TO_ONE_ENABLE = false;
	
	private static final Float
		DEFAULT_MS_MIN_SAMPLE_SHADING = 0.0f;
	
	// Viewport state
	
	private static final String
		VP_VIEWPORT_COUNT_KEY = "viewportCount",
		VP_SCISSORS_COUNT_KEY = "scissorsCount";
	
	private static final Integer
		DEFAULT_VP_VIEWPORT_COUNT = 1,
		DEFAULT_VP_SCISSOR_COUNT = 1;
	
	// Depth Stencil State
	
	private static final String
		DS_DEPTH_TEST_ENABLE_KEY = "depthTestEnable",
		DS_DEPTH_WRITE_ENABLE_KEY = "depthWriteEnable",
		DS_DEPTH_COMPARE_OP_KEY = "depthCompareOp",
		DS_DEPTH_BOUNDS_TEST_ENABLE_KEY = "depthBoundsEnable",
		DS_STENCIL_TEST_ENABLE_KEY = "stencilTestEnable",
		DS_MIN_DEPTH_BOUNDS_KEY = "minDepthBounds",
		DS_MAX_DEPTH_BOUNDS_KEY = "maxDepthBounds",
		DS_FRONT_KEY = "front",
		DS_BACK_KEY = "back",
		DS_FAIL_OP_KEY = "failOp",
		DS_PASS_OP_KEY = "passOp",
		DS_DEPTH_FAIL_OP_KEY = "depthFailOp",
		DS_COMPARE_OP_KEY = "compareOpKey";
	
	private static final String
		DEFAULT_DS_DEPTH_COMPARE_OP = "VK_COMPARE_OP_ALWAYS",
		DEFAULT_DS_FAIL_OP = "VK_STENCIL_OP_KEEP",
		DEFAULT_DS_PASS_OP = "VK_STENCIL_OP_KEEP",
		DEFAULT_DS_DEPTH_FAIL_OP = "VK_STENCIL_OP_KEEP",
		DEFAULT_DS_COMPARE_OP = "VK_COMPARE_OP_ALWAYS";
	
	private static final Boolean
		DEFAULT_DS_DEPTH_TEST_ENABLE = false,
		DEFAULT_DS_DEPTH_WRITE_ENABLE = false,
		DEFAULT_DS_DEPTH_BOUND_ENABLE = false,
		DEFAULT_DS_STENCIL_TEST_ENABLE = false;
	
	private static final Float
		DEFAULT_DS_MIN_DEPTH_BOUNDS = 0.0f,
		DEFAULT_DS_MAX_DEPTH_BOUNDS = 0.0f;
	
	// Stages
	
	private static final String
		STAGES_KEY = "stages",
		STAGE_KEY = "stage",
		FILE_KEY = "file",
		MAIN_KEY = "mainMethodName";
	
	private static final String
		DEFAULT_STAGE = "VK_SHADER_STAGE_VERTEX_BIT",
		DEFAULT_FILE = "storage/res/shaders/triangle.vert.spv",
		DEFAULT_MAIN = "main";
	
	// Dynamic states
	
	private static final String
		DYNAMIC_STATES_KEY = "dynamicStates";
	
	private static final List<String> 
		DEFAULT_DYNAMIC_STATES = new ArrayList<String>();
	
	private Asset asset;
	private String configFile;
	private VkDevice device;
	
	private long pipelineHandle;
	
	private VkGraphicsPipelineCreateInfo.Buffer pipelineCreateInfo;	

	private VkVertexInputBindingDescription.Buffer bindingDescription;
	private VkVertexInputAttributeDescription.Buffer attributeDescription;
	private VkPipelineVertexInputStateCreateInfo vertexCreateInfo;
	private VkPipelineInputAssemblyStateCreateInfo inputAsemblyInfo;
	private VkPipelineTessellationStateCreateInfo tessellationInfo;
	private VkPipelineRasterizationStateCreateInfo rasterizationStateInfo;
	private VkPipelineColorBlendAttachmentState.Buffer colorWriteMask;
	private VkPipelineColorBlendStateCreateInfo colorBlendStateInfo;
	private VkPipelineMultisampleStateCreateInfo multisampleState;
	private VkPipelineViewportStateCreateInfo viewportStateCreateInfo;
	private VkPipelineDepthStencilStateCreateInfo depthStencilState;
	private VkPipelineShaderStageCreateInfo.Buffer stages;
	private IntBuffer dynamicStates;
	private VkPipelineDynamicStateCreateInfo dynamicState;

	public GraphicsPipeline(Asset asset, String configFile, VkDevice device, long renderPass, long pipelineLayout) throws IOException, AssertionError, VulkanException {
		this.asset = asset;
		this.configFile = configFile;
		this.device = device;
		
		loadPipeline(renderPass, pipelineLayout);
	}
	
	private void loadPipeline(long renderPass, long layout) throws IOException, AssertionError, VulkanException {
		pipelineCreateInfo = VkGraphicsPipelineCreateInfo.calloc(1);
				
		ConfigFile cfg = asset.getConfigFile(configFile);
		
		loadPVertexInputState(cfg.getCfgAsset(VERTEX_INPUT_STATE_KEY));
		loadInputAsemblyInfo(cfg.getCfgAsset(INPUT_ASSEMBLY_STATE));
		loadTessellationState(cfg.getCfgAsset(TESSELLATION_STATE));
		loadRasterizationState(cfg.getCfgAsset(RASTERIZATION_STATE));
		loadPipelineColorBlendState(cfg.getCfgAsset(COLOR_BLEND_STATE));
		loadMultisampleState(cfg.getCfgAsset(MULTISAMPLE_STATE));
		loadViewportState(cfg.getCfgAsset(VIEWPORT_STATE));
		loadDepthStencilState(cfg.getCfgAsset(DEPTH_STENCIL_STATE));
		loadStages(cfg.getCfgAsset(STAGES));
		loadDynamicStates(cfg.getCfgAsset(DYNAMIC_STATES));

		pipelineCreateInfo
			.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
			.pVertexInputState(vertexCreateInfo)
			.pInputAssemblyState(inputAsemblyInfo)
			.pTessellationState(tessellationInfo)
			.pRasterizationState(rasterizationStateInfo)
			.pColorBlendState(colorBlendStateInfo)
			.pMultisampleState(multisampleState)
			.pViewportState(viewportStateCreateInfo)
			.pDepthStencilState(depthStencilState)
			.pStages(stages)
			.pDynamicState(dynamicState)
			.renderPass(renderPass)
			.layout(layout);
		
		cfg.close();
		
		LongBuffer pPipeline = memAllocLong(1);
		int err = vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, pipelineCreateInfo, null, pPipeline);
		
		validate(err, "Failed to create ");
		pipelineHandle = pPipeline.get(0);
		memFree(pPipeline);
		
		freeResources();
	}
	
	private void loadPVertexInputState(ConfigAsset cfg) {
		List<ConfigAsset> bindings = cfg.getCfgList(VERTEX_INPUT_BINDINGS_KEY);
		
		 //Bindings:
		 bindingDescription = VkVertexInputBindingDescription.calloc(bindings.size());
		 
		 for (int i = 0; i < bindings.size(); i++) {
			 ConfigAsset sCfg = bindings.get(i);
			 
			 bindingDescription.get(i)
				 .binding(sCfg.getInteger(VIB_BINDING_KEY, DEFAULT_VIB_BINDING)) // <- we bind our vertex buffer to point 0
				 .stride(sCfg.getInteger(VIB_STRIDE_KEY, DEFAULT_VIB_STRIDE))	// 
				 .inputRate(sCfg.getStaticIntFromClass(VK10.class, VIB_INPUT_RATE_KEY, DEFAULT_VIB_INPUT_RATE));
		 }
				
		 List<ConfigAsset> attributes = cfg.getCfgList(VERTEX_ATTRIBUTE_BINDINGS_KEY);
		 
		 //Attributes:
		 attributeDescription = VkVertexInputAttributeDescription.calloc(attributes.size());
		 for (int i = 0; i < attributes.size(); i++) {
			 ConfigAsset sCfg = attributes.get(i);

			 attributeDescription.get(i)
			 		.binding(sCfg.getInteger(VIA_BINDING_KEY, DEFAULT_VIA_BINDING))
			 		.location(sCfg.getInteger(VIA_LOCATION_KEY, DEFAULT_VIA_LOACATION)) //Location 0 : Position in shader
			 		.format(sCfg.getStaticIntFromClass(VK10.class, VIA_FORMAT_KEY, DEFAULT_VIA_FORMAT))
			 		.offset(sCfg.getInteger(VIA_OFFSET_KEY, DEFAULT_VIA_OFFSET));
		 }
		 
		 //asign to vertex buffer
		 vertexCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
				 .pNext(NULL)
				 .pVertexBindingDescriptions(bindingDescription)
				 .pVertexAttributeDescriptions(attributeDescription);
	}
	
	public long getPipelineHandle() {
		return pipelineHandle;
	}

	private void loadInputAsemblyInfo(ConfigAsset cfg) {
		inputAsemblyInfo = VkPipelineInputAssemblyStateCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
			 	.topology(cfg.getStaticIntFromClass(VK10.class, IA_TOPOLOGY_KEY, DEFAULT_IA_TOPOLOGY))
			 	.primitiveRestartEnable(cfg.getBoolean(IA_PRIMITIVE_RESTART_ENABLE, DEFAULT_PRIMITIVE_RESTART_ENABLE));
		
	}
	
	private void loadTessellationState(ConfigAsset cfg) {
		
		if (cfg.getBoolean(OMMIT_TESSELLATION_STATE_KEY, DEFAULT_OMMIT_TESSELLATION_STATE)) {
			tessellationInfo = null;
		}
		else {
			tessellationInfo = VkPipelineTessellationStateCreateInfo.calloc()
					.sType(VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_STATE_CREATE_INFO)
					.patchControlPoints(cfg.getInteger(TS_PATCH_CONTROL_POINTS_KEY, DEFAULT_TS_PATCH_CONTROL_POINTS));
		}
	}
	
	private void loadRasterizationState(ConfigAsset cfg) {
		
		//Rasterization state
		 rasterizationStateInfo = VkPipelineRasterizationStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
				 .polygonMode(cfg.getStaticIntFromClass(VK10.class, RS_POLYGON_MODE_KEY, DEFAULT_RS_POLYGON_MODE))
				 .cullMode(cfg.getStaticIntFromClass(VK10.class, RS_CULL_MODE_KEY, DEFAULT_RS_CULL_MODE))
				 .frontFace(cfg.getStaticIntFromClass(VK10.class, RS_FRONT_FACE_KEY, DEFAULT_RS_FRONT_FACE))
				 .lineWidth(cfg.getFloat(RS_LINE_WIDTH_KEY, DEFAULT_RS_LINE_WIDTH))
				 .depthClampEnable(cfg.getBoolean(RS_DEPTH_CLAMP_ENABLE_KEY, DEFAULT_RS_DEPTH_CLAMP_ENABLE))
				 .rasterizerDiscardEnable(cfg.getBoolean(RS_RASTERIZER_DISCARD_ENABLE_KEY, DEFAULT_RS_RASTERIZER_DISCARD_ENABLE))
				 .depthBiasEnable(cfg.getBoolean(RS_DEPTH_BIAS_ENABLE_KEY, DEFAULT_RS_DEPTH_BIAS_ENABLE))
				 .depthBiasConstantFactor(cfg.getFloat(RS_DEPTH_BIAS_CONSTANT_FACTOR_KEY, DEFAULT_RS_DEPTH_BIAS_CONSTANT_FACTOR))
				 .depthBiasClamp(cfg.getFloat(RS_DEPTH_BIAS_CLAMP_KEY, DEFAULT_RS_DEPTH_BIAS_CLAMP))
				 .depthBiasSlopeFactor(cfg.getFloat(RS_DEPTH_BIAS_SLOPE_FACTOR_KEY, DEFAULT_RS_DEPTH_BIAS_SLOPE_FACTOR));
	}
	
	private void loadPipelineColorBlendState(ConfigAsset cfg) {
		List<ConfigAsset> attachmentStates = cfg.getCfgList(CB_ATTACHMENTS_KEY);

		colorWriteMask = VkPipelineColorBlendAttachmentState.calloc(attachmentStates.size());
		for (int i = 0; i < attachmentStates.size(); i++) {
			colorWriteMask.get(i)
					.blendEnable(cfg.getBoolean(CB_BLEND_ENABLE_KEY, DEFAULT_CB_BLEND_ENABLE))
					.srcColorBlendFactor(cfg.getStaticIntFromClass(VK10.class, CB_SRC_COLOR_BLEND_FACTOR_KEY, DEFAULT_CB_SRC_COLOR_BLEND_FACTOR))
					.dstColorBlendFactor(cfg.getStaticIntFromClass(VK10.class, CB_DST_COLOR_BLEND_FACTOR_KEY, DEFAULT_CB_DST_COLOR_BLEND_FACTOR))
					.colorBlendOp(cfg.getStaticIntFromClass(VK10.class, CB_COLOR_BLEND_OP_KEY, DEFAULT_CB_COLOR_BLEND_OP))
					.srcAlphaBlendFactor(cfg.getStaticIntFromClass(VK10.class, CB_SRC_ALPHA_BLEND_FACTOR_KEY, DEFAULT_CB_SRC_ALPHA_BLEND_FACTOR))
					.dstAlphaBlendFactor(cfg.getStaticIntFromClass(VK10.class, CB_DST_ALPHA_BLEND_FACTOR_KEY, DEFAULT_CB_DST_ALPHA_BLEND_FACTOR))
					.alphaBlendOp(cfg.getStaticIntFromClass(VK10.class, CB_ALPHA_BLEND_OP_KEY, DEFAULT_CB_ALPHA_BLEND_OP))
					.colorWriteMask(cfg.getInteger(CB_COLOR_WRITE_MASK, DEFAULT_CB_COLOR_WRITE_MASK));
		}
				 
		 colorBlendStateInfo = VkPipelineColorBlendStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
				 .pAttachments(colorWriteMask);
	}
	
	private void loadMultisampleState(ConfigAsset cfg) {
		
		 multisampleState = VkPipelineMultisampleStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
				 .pSampleMask(null) //TODO: Enable as a JSON setting
				 .rasterizationSamples(cfg.getStaticIntFromClass(VK10.class, MS_RASTERIZATION_SAMPLES_KEY, DEFAULT_MS_RASTERIZATION_SAMPLES))
				 .sampleShadingEnable(cfg.getBoolean(MS_SAMPLE_SHADING_ENABLE_KEY, DEFAULT_MS_SAMPLE_SHADING_ENABLE))
				 .minSampleShading(cfg.getFloat(MS_MIN_SAMPLE_SHADING_KEY, DEFAULT_MS_MIN_SAMPLE_SHADING))
				 .alphaToCoverageEnable(cfg.getBoolean(MS_ALPHA_TO_COVERAGE_ENABLE_KEY, DEFAULT_MS_ALPHA_TO_COVERAGE_ENABLE))
				 .alphaToOneEnable(cfg.getBoolean(MS_ALPHA_TO_ONE_ENABLE, DEFAULT_MS_ALPHA_TO_ONE_ENABLE));
	}
	
	private void loadViewportState(ConfigAsset cfg) {
		 viewportStateCreateInfo = VkPipelineViewportStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
				 .viewportCount(cfg.getInteger(VP_VIEWPORT_COUNT_KEY, DEFAULT_VP_VIEWPORT_COUNT))
				 .scissorCount(cfg.getInteger(VP_SCISSORS_COUNT_KEY, DEFAULT_VP_SCISSOR_COUNT)); 
	}
	
	private void loadDepthStencilState(ConfigAsset cfg) {
		depthStencilState = VkPipelineDepthStencilStateCreateInfo.calloc()
				 // No depth test/write and no stencil used 
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO)
				 .depthTestEnable(cfg.getBoolean(DS_DEPTH_TEST_ENABLE_KEY, DEFAULT_DS_DEPTH_TEST_ENABLE))
				 .depthWriteEnable(cfg.getBoolean(DS_DEPTH_WRITE_ENABLE_KEY, DEFAULT_DS_DEPTH_WRITE_ENABLE))
				 .depthCompareOp(cfg.getStaticIntFromClass(VK10.class, DS_DEPTH_COMPARE_OP_KEY, DEFAULT_DS_DEPTH_COMPARE_OP))
				 .depthBoundsTestEnable(cfg.getBoolean(DS_DEPTH_BOUNDS_TEST_ENABLE_KEY, DEFAULT_DS_DEPTH_BOUND_ENABLE))
				 .stencilTestEnable(cfg.getBoolean(DS_STENCIL_TEST_ENABLE_KEY, DEFAULT_DS_STENCIL_TEST_ENABLE))
				 .minDepthBounds(cfg.getFloat(DS_MIN_DEPTH_BOUNDS_KEY, DEFAULT_DS_MIN_DEPTH_BOUNDS))
				 .maxDepthBounds(cfg.getFloat(DS_MAX_DEPTH_BOUNDS_KEY, DEFAULT_DS_MAX_DEPTH_BOUNDS));
		
		ConfigAsset frontCfg = cfg.getCfgAsset(DS_FRONT_KEY);
		depthStencilState.back()
			.failOp(frontCfg.getStaticIntFromClass(VK10.class, DS_FAIL_OP_KEY, DEFAULT_DS_FAIL_OP))
			.passOp(frontCfg.getStaticIntFromClass(VK10.class, DS_PASS_OP_KEY, DEFAULT_DS_PASS_OP))
			.depthFailOp(frontCfg.getStaticIntFromClass(VK10.class, DS_DEPTH_FAIL_OP_KEY, DEFAULT_DS_DEPTH_FAIL_OP))
			.compareOp(frontCfg.getStaticIntFromClass(VK10.class, DS_COMPARE_OP_KEY, DEFAULT_DS_COMPARE_OP));
		
		ConfigAsset backCfg = cfg.getCfgAsset(DS_BACK_KEY);
		depthStencilState.back()
			.failOp(backCfg.getStaticIntFromClass(VK10.class, DS_FAIL_OP_KEY, DEFAULT_DS_FAIL_OP))
			.passOp(backCfg.getStaticIntFromClass(VK10.class, DS_PASS_OP_KEY, DEFAULT_DS_PASS_OP))
			.depthFailOp(backCfg.getStaticIntFromClass(VK10.class, DS_DEPTH_FAIL_OP_KEY, DEFAULT_DS_DEPTH_FAIL_OP))
			.compareOp(backCfg.getStaticIntFromClass(VK10.class, DS_COMPARE_OP_KEY, DEFAULT_DS_COMPARE_OP));
	}
	
//	private ConfigAsset defaultStage() {
//		JSONObject json = new JSONObject();
//		
//		json.put(key, value)
//		
//	}

	private void loadStages(ConfigAsset cfg) throws VulkanException {
		List<ConfigAsset> cfgs = cfg.getCfgList(STAGES_KEY);
		
		if (cfgs.size() == 0)
			cfgs.add(new ConfigAsset(new JSONObject()));
			
		stages = VkPipelineShaderStageCreateInfo.calloc(cfgs.size());
		for (int i = 0; i < cfgs.size(); i++) {
			ConfigAsset sCfg = cfgs.get(i);
			
			stages.get(i).set(Util.createShaderStage(
					Util.createShaderModule(device, 
							new File(sCfg.getString(FILE_KEY, DEFAULT_FILE))), 
					sCfg.getStaticIntFromClass(VK10.class, STAGE_KEY, DEFAULT_STAGE), 
					sCfg.getString(MAIN_KEY, DEFAULT_MAIN)));
		}
	}
	
	private void loadDynamicStates(ConfigAsset cfg) {
		List<String> dynamicStatesNames = 
				cfg.getStringArray(DYNAMIC_STATES_KEY, DEFAULT_DYNAMIC_STATES);
		
		dynamicStates = memAllocInt(dynamicStatesNames.size());
		for (int i = 0; i < dynamicStatesNames.size(); i++) {
			try {
				dynamicStates.put(i, ResourceUtil.getStaticIntValueFromClass(
						VK10.class, dynamicStatesNames.get(i)));
			} catch (Exception e) {	
				e.printStackTrace();
			}
		}
		
		dynamicState = VkPipelineDynamicStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
				 .pDynamicStates(dynamicStates);
	}
	
	private void freeResources() {
		pipelineCreateInfo.free();

		bindingDescription.free();
		attributeDescription.free();
		vertexCreateInfo.free();
		inputAsemblyInfo.free();
		if (tessellationInfo != null)
			tessellationInfo.free();
		rasterizationStateInfo.free();
		colorWriteMask.free();
		colorBlendStateInfo.free();
		multisampleState.free();
		viewportStateCreateInfo.free();
		depthStencilState.free();
		stages.free();
		memFree(dynamicStates);
		dynamicState.free();
	}
	
	@Override
	public void destroy() {
		vkDestroyPipeline(device, pipelineHandle, null);
	}
}
