package rendering.config;

import static rendering.pipeline.PipelineUtil.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDevice;

import core.resources.ConfigAsset;
import core.resources.ConfigFile;
import core.resources.Destroyable;
import core.result.VulkanException;
import rendering.engine.shader.DescriptorSetBlueprint;

/**
 * 
 * Descriptor set blueprint with data loaded from a file.
 * 
 * @author Cezary Chodun
 * @since 04.01.2020
 */
public class FileDescriptorSetBlueprint implements DescriptorSetBlueprint, Destroyable {
	
	/** Class specific logger. */
	private final static Logger logger = Logger.getLogger(FileDescriptorSetBlueprint.class.getName());
	
	/** Keys for loading data from JSON files. */
	private static final String 
		BINDINGS_COUNT_KEY = "bindingsCount",
		BINDINGS_KEY = "bindings",
		DESCRIPTOR_COUNT_KEY = "descriptorCount",
		BINDING_KEY = "binding",
		DESCRIPTORS_TYPE_KEY = "descriptorsType",
		STAGE_FLAGS_KEY = "stages",
		DEFAULT_DESCRIPTORS_TYPE = "VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER";
	
	/** Default values for descriptor set layout fields. */
	private static final Integer 
		DEFAULT_BINDINGS_COUNT = 0,
		DEFAULT_DESCRIPTOR_COUNT = 0,
		DEFAULT_BINDING = 0;
	
	private VkDevice device;
	
	private long descriptorLayout;
	private int bindings;
	VkDescriptorSetLayoutBinding.Buffer layoutBindings;
	
	/**
	 * Creates a descriptor set layout using the supplied configuration file.
	 * 
	 * @param device	Must be a valid Vulkan device.
	 * @param cfg		Must be a valid configuration file.
	 */
	public FileDescriptorSetBlueprint(VkDevice device, ConfigFile cfg) {
		this.device = device;
		loadFromAsset(cfg);
		cfg.close();
	}

	/**
	 * Loads the configuration data from a file(asset).
	 * 
	 * @param cfg
	 */
	private void loadFromAsset(ConfigAsset cfg) {
		bindings = cfg.getInteger(BINDINGS_COUNT_KEY, DEFAULT_BINDINGS_COUNT);
		layoutBindings = VkDescriptorSetLayoutBinding.calloc(bindings);
		List<ConfigAsset> cfgs = cfg.getCfgList(BINDINGS_KEY);
		
		for (int i = 0; i < cfgs.size(); i++) {
			try {
				layoutBindings.get(i)
					.binding(cfgs.get(i).getInteger(BINDING_KEY, DEFAULT_BINDING))
					.stageFlags(cfgs.get(i).getFlags(VK10.class, STAGE_FLAGS_KEY))
					.descriptorType(cfgs.get(i).getStaticIntFromClass(VK10.class, DESCRIPTORS_TYPE_KEY, DEFAULT_DESCRIPTORS_TYPE))
					.descriptorCount(cfgs.get(i).getInteger(DESCRIPTOR_COUNT_KEY, DEFAULT_DESCRIPTOR_COUNT));
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Failed to load descriptor set binding information.");
				e.printStackTrace();
			}
		}
		
		try {
			descriptorLayout = createDescriptorSetLayout(device, layoutBindings);
		} catch (VulkanException e) {
			logger.log(Level.SEVERE, "Failed to create descriptor set layout.");
			e.printStackTrace();
		}
	}
	
	@Override
	public long getLayout() {
		return descriptorLayout;
	}

	@Override
	public void destroy() {
		layoutBindings.free();
		VK10.vkDestroyDescriptorSetLayout(device, descriptorLayout, null);
	}
	
	@Override
	public int descriptorCount() {
		return bindings;
	}

}
