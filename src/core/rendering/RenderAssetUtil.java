package core.rendering;

import static core.rendering.RenderUtil.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import core.resources.Asset;
import core.resources.ConfigFile;
import core.result.VulkanException;

public class RenderAssetUtil {
	
	protected static final String INSTANCE_CONFIG_FILE = "instance.cfg";
	protected static final String INSTANCE_VALIDATION_LAYERS_KEY = "VALIDATION_LAYERS";
	protected static final String INSTANCE_EXTENSIONS_KEY = "EXTENSIONS";

	public static VkInstance createInstanceFromAsset(VkApplicationInfo appInfo, Asset config, String[] requiredExtensions) throws VulkanException, IOException {
		
		ConfigFile cfgFile = config.getConfigFile(INSTANCE_CONFIG_FILE);
		
		List<String> layerNames = cfgFile.getArray(INSTANCE_VALIDATION_LAYERS_KEY, new ArrayList<String>());
		List<String> extensions = cfgFile.getArray(INSTANCE_EXTENSIONS_KEY, new ArrayList<String>());
		for (String required : requiredExtensions)
			extensions.add(required);
		
		cfgFile.close();
		
		return createInstance(appInfo, layerNames, extensions);
	}
	
//	@Deprecated
//	/**
//	 * 
//	 * 
//	 * 
//	 * @param appInfo
//	 * @param config
//	 * @param requiredExtensions
//	 * 
//	 * @return			Instance handle.
//	 * 
//	 * @throws VulkanException	
//	 * @throws IOException
//	 */
//	public static VkInstance createInstanceFromAsset(VkApplicationInfo appInfo, Asset config, String[] requiredExtensions) throws VulkanException, IOException {
//		
//		if (!config.exists(INSTANCE_CONFIG_FILE)) {
//			config.newFile(INSTANCE_CONFIG_FILE);
//			
//			JSONObject obj = new JSONObject();
//			obj.put(INSTANCE_VALIDATION_LAYERS_KEY, new ArrayList<String>());
//			obj.put(INSTANCE_EXTENSIONS_KEY, new ArrayList<String>());
//			
//			FileWriter writer = new FileWriter(config.get(INSTANCE_CONFIG_FILE));
//			obj.write(writer, 4, 1);
//			writer.close();
//		}
//		File cfgFile = config.get(INSTANCE_CONFIG_FILE);
//		
////		if (!cfgFile.exists())
////			throw new FileNotFoundException("Failed to locate file: " + cfgFile.getPath().toString() + "!");
//		
//		JSONObject obj = new JSONObject(new JSONTokener(new FileReader(cfgFile)));
//		
//		JSONArray layers = obj.getJSONArray(INSTANCE_VALIDATION_LAYERS_KEY);
//		List<String> layerNames = new ArrayList<String>(layers.length());
//		for (int i = 0; i < layers.length(); i++)
//			layerNames.add(layers.getString(i));
//		
//		JSONArray extensions = obj.getJSONArray(INSTANCE_EXTENSIONS_KEY);
//		List<String> extensionNames = new ArrayList<String>(extensions.length());
//		for (int i = 0; i < extensions.length(); i++)
//			extensionNames.add(extensions.getString(i));
//		for (String required : requiredExtensions)
//			extensionNames.add(required);
//		
//		return createInstance(appInfo, layerNames, extensionNames);
//	}
	
}
