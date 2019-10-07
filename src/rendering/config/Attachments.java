package rendering.config;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;

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
		FINAL_LAYOUT_KEY = "finalLayout";
	
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
	

	private VkAttachmentDescription.Buffer attachments;
	
	public Attachments(Asset asset, String... fileNames) throws AssertionError, IOException {
		attachments = VkAttachmentDescription.calloc(fileNames.length);
		
		for (String file : fileNames) 
			attachments.put(loadAttachment(asset, file));
		
		attachments.flip();
	}
	
	private VkAttachmentDescription loadAttachment(Asset asset, String fileName) throws AssertionError, IOException {
		ConfigFile cfg = asset.getConfigFile(fileName);
		
		try {
			VkAttachmentDescription desc = VkAttachmentDescription.calloc()
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
			
			return desc;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			throw new AssertionError("Failed to load flag values!");
		}
	}
	
	public VkAttachmentDescription.Buffer getBuffer() {
		return attachments;
	}
	
}
