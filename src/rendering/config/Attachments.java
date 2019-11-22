package rendering.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkClearValue;

import core.resources.Asset;
import core.resources.ConfigFile;
import core.resources.Destroyable;
import core.resources.ResourceUtil;

/**
 * Class for storing attachments configuration.
 * 
 * @author Cezary Chodun
 * @since 19.10.2019
 */
public class Attachments implements Destroyable{

	/**
	 * Flags for the configuration file.
	 */
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
		CLEAR_VALUE_DEPTH_STENCIL_KEY = "clearValueDepthStencil",
		COLOR_ATTACHMENT_KEY = "colorAttachment";
	
	/**
	 * Default values for the configuration file.
	 */
	public final static String
		DEFAULT_FORMAT = "VK_FORMAT_B8G8R8_UNORM",
		DEFAULT_SAMPLES = "VK_SAMPLE_COUNT_1_BIT",
		DEFAULT_LOAD_OP = "VK_ATTACHMENT_LOAD_OP_CLEAR",
		DEFAULT_STORE_OP = "VK_ATTACHMENT_STORE_OP_STORE",
		DEFAULT_STENCIL_LOAD_OP = "VK_ATTACHMENT_LOAD_OP_DONT_CARE",
		DEFAULT_STENCIL_STORE_OP = "VK_ATTACHMENT_STORE_OP_DONT_CARE",
		DEFAULT_INITIAL_LAYOUT = "VK_IMAGE_LAYOUT_UNDEFINED",
		DEFAULT_FINAL_LAYOUT = "VK_IMAGE_LAYOUT_UNDEFINED";
	
	/**
	 * Default values for the configuration file.
	 */
	public final static Integer
		DEFAULT_FLAGS = 0,
		DEFAULT_ATTACHMENT_COUNT = 1;
	
	/**
	 * Default values for the configuration file.
	 */
	public static final Float
		DEFAULT_CLEAR_VALUE_DEPTH_STENCIL = 1.0f;
	
	/**
	 * Default values for the configuration file.
	 */
	public static final Float[] 
		DEFAULT_CLEAR_VALUE_COLOR = {0.0f, 0.0f, 0.7f, 1.0f};
	
	/**
	 * Default values for the configuration file.
	 */
	public static Boolean
		DEFAULT_COLOR_ATTACHMENT = true;

	/**
	 * Buffer with the loaded attachments.
	 */
	private VkAttachmentDescription.Buffer attachments;
	/**
	 * Buffer with the clear values corresponding to the attachments.
	 */
	private VkClearValue.Buffer clearValues;
	
	/**
	 * Loads attachments from the asset file(with clear values).
	 * 
	 * @param asset				Asset containing configuration files.
	 * @param fileNames			Names of the configuration files.
	 * 
	 * @throws AssertionError	If failed to create JSON file,
	 * 		or when failed to obtain a value from the configuration file.
	 * @throws IOException		If an I/O error occurred.
	 */
	public Attachments(Asset asset, String... fileNames) throws AssertionError, IOException {
		attachments = VkAttachmentDescription.calloc(fileNames.length);
		clearValues = VkClearValue.calloc(fileNames.length);
		
		for (int i = 0; i < fileNames.length; i++) 
			loadAttachment(asset, fileNames[i], attachments.get(i), clearValues.get(i));
	}
	
	private int getImageLayout(String name) {
			try {
				if (name.contains("KHR"))
					return  ResourceUtil.getStaticIntValueFromClass(KHRSwapchain.class, name);

				return ResourceUtil.getStaticIntValueFromClass(VK10.class, name);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		return VK10.VK_IMAGE_LAYOUT_UNDEFINED;
	}
	
	/**
	 * Loads an attachment from a file.
	 * 
	 * @param asset						Asset containing the configuration file.
	 * @param fileName					Name of the configuration file.
	 * @param pAttachmentDescription	Attachment description object that will store the data.
	 * @param pClearValue				Clear value object that will store the data.
	 * 
	 * @throws AssertionError		If failed to create JSON file, 
	 * 		or when failed to obtain a value from the configuration file.
	 * @throws IOException				If an I/O error occurred.
	 */
	private void loadAttachment(
			Asset asset, 
			String fileName, 
			VkAttachmentDescription pAttachmentDescription, 
			VkClearValue pClearValue
			)throws AssertionError, IOException {
		
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
				.initialLayout(getImageLayout(cfg.getString(INITIAL_LAYOUT_KEY, DEFAULT_INITIAL_LAYOUT)))
				.finalLayout(getImageLayout(cfg.getString(FINAL_LAYOUT_KEY, DEFAULT_FINAL_LAYOUT)));
			
				if (cfg.getBoolean(COLOR_ATTACHMENT_KEY, DEFAULT_COLOR_ATTACHMENT)) {
					List<Float> colorArray = cfg.getFloatArray(CLEAR_VALUE_COLOR_KEY, Arrays.asList(DEFAULT_CLEAR_VALUE_COLOR));
					
					pClearValue
						.color()
							.float32(0, colorArray.get(0).floatValue())
							.float32(1, colorArray.get(1).floatValue())
							.float32(2, colorArray.get(2).floatValue())
							.float32(3, colorArray.get(3).floatValue());
				}
				else 
					pClearValue.depthStencil()
							.depth(cfg.getFloat(CLEAR_VALUE_DEPTH_STENCIL_KEY, DEFAULT_CLEAR_VALUE_DEPTH_STENCIL));
				
			cfg.close();
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			cfg.close();
			e.printStackTrace();
			throw new AssertionError("Failed to load flag values!");
		}
	}
	
	/**
	 * Returns a buffer with the clear values. 
	 * 
	 * @return clear values buffer.
	 */
	public VkClearValue.Buffer getClearValues() {
		return clearValues;
	}
	
	/**
	 * Returns the attachments buffer.
	 * 
	 * @return	attachments buffer.
	 */
	public VkAttachmentDescription.Buffer getBuffer() {
		return attachments;
	}

	@Override
	public void destroy() {
		attachments.free();
		clearValues.free();
	}
	
}
