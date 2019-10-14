package rendering.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkClearValue;

import core.resources.Asset;
import core.resources.ConfigFile;

public class Attachments {

	
	public final static String 
		FLAGS_KEY = "flags",
		FORMAT_KEY = "format",
		SAMPLES_KEY = "samples",
		LOAD_OP_KEY = "loadOp",
		STORE_OP_KEY = "stroeOp",
		STENCIL_LOAD_OP_KEY = "stencilLoadOp",
		STENCIL_STORE_OP_KEY = "stencilStoreOp",
		INITIAL_LAYOUT_KEY = "initialLayout",
		FINAL_LAYOUT_KEY = "finalLayout",
		CLEAR_VALUE_COLOR_KEY = "clearValueColor",
		CLEAR_VALUE_DEPTH_STENCIL_KEY = "clearValueDepthStencil";
	
	public final static String
		DEFAULT_FORMAT = "VK_FORMAT_B8G8R8_UNORM",
		DEFAULT_SAMPLES = "VK_SAMPLE_COUNT_1_BIT",
		DEFAULT_LOAD_OP = "VK_ATTACHMENT_LOAD_OP_CLEAR",
		DEFAULT_STORE_OP = "VK_ATTACHMENT_STORE_OP_STORE",
		DEFAULT_STENCIL_LOAD_OP = "VK_ATTACHMENT_LOAD_OP_DONT_CARE",
		DEFAULT_STENCIL_STORE_OP = "VK_ATTACHMENT_STORE_OP_DONT_CARE",
		DEFAULT_INITIAL_LAYOUT = "VK_IMAGE_LAYOUT_UNDEFINED",
		DEFAULT_FINAL_LAYOUT = "VK_IMAGE_LAYOUT_UNDEFINED";
	
	public final static Integer
		DEFAULT_FLAGS = 0,
		DEFAULT_ATTACHMENT_COUNT = 1;
	
	public static final Float
		DEFAULT_CLEAR_VALUE_DEPTH_STENCIL = 1.0f;
	
	public static final Float[] 
		DEFAULT_CLEAR_VALUE_COLOR = {0.0f, 0.0f, 1.0f, 1.0f};

	private VkAttachmentDescription.Buffer attachments;
	private VkClearValue.Buffer clearValues;
	
	public Attachments(Asset asset, String... fileNames) throws AssertionError, IOException {
		attachments = VkAttachmentDescription.calloc(fileNames.length);
		clearValues = VkClearValue.calloc(fileNames.length);
		
		for (int i = 0; i < fileNames.length; i++) {
			loadAttachment(asset, fileNames[i], attachments.get(i));
			loadClearValue(asset, fileNames[i], clearValues.get(i));
		}
		
		attachments.flip();
	}
	
	private void loadClearValue(Asset asset, String fileName, VkClearValue pClearValue) throws IOException, AssertionError {
		ConfigFile cfg = asset.getConfigFile(fileName);
		List<Float> colorArray = cfg.getArray(CLEAR_VALUE_COLOR_KEY, Arrays.asList(DEFAULT_CLEAR_VALUE_COLOR));
		
		pClearValue
			.color()
				.float32(0, colorArray.get(0))
				.float32(1, colorArray.get(1))
				.float32(2, colorArray.get(2))
				.float32(3, colorArray.get(3));
		pClearValue
			.depthStencil()
				.depth(cfg.getFloat(CLEAR_VALUE_DEPTH_STENCIL_KEY, DEFAULT_CLEAR_VALUE_DEPTH_STENCIL));
		
	}
	
	private void loadAttachment(Asset asset, String fileName, VkAttachmentDescription pAttachmentDescription) throws AssertionError, IOException {
		ConfigFile cfg = asset.getConfigFile(fileName);
		
		try {
			pAttachmentDescription
				.flags(cfg.getFlags(VK10.class, FLAGS_KEY, new ArrayList<String>()))
				.format(cfg.getStaticIntFromClass(VK10.class, FORMAT_KEY, DEFAULT_FORMAT))
				.samples(cfg.getStaticIntFromClass(VK10.class, SAMPLES_KEY, DEFAULT_SAMPLES))
				.loadOp(cfg.getStaticIntFromClass(VK10.class, LOAD_OP_KEY, DEFAULT_LOAD_OP))
				.storeOp(cfg.getStaticIntFromClass(VK10.class, STORE_OP_KEY, DEFAULT_STORE_OP))
				.stencilLoadOp(cfg.getStaticIntFromClass(VK10.class, STENCIL_LOAD_OP_KEY, DEFAULT_STENCIL_LOAD_OP))
				.stencilStoreOp(cfg.getStaticIntFromClass(VK10.class, STENCIL_STORE_OP_KEY, DEFAULT_STENCIL_STORE_OP))
				.initialLayout(cfg.getStaticIntFromClass(VK10.class, INITIAL_LAYOUT_KEY, DEFAULT_INITIAL_LAYOUT))
				.finalLayout(cfg.getStaticIntFromClass(VK10.class, FINAL_LAYOUT_KEY, DEFAULT_FINAL_LAYOUT));
			
			cfg.close();
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			cfg.close();
			e.printStackTrace();
			throw new AssertionError("Failed to load flag values!");
		}
	}
	
	public VkAttachmentDescription.Buffer getBuffer() {
		return attachments;
	}
	
}
