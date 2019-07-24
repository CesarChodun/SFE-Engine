package core.rendering;

import static core.rendering.RenderUtil.createInstance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;

import core.resources.Asset;
import core.result.VulkanException;

public class RenderAssetUtil {
	
	protected static final String INSTANCE_DATA_FILE = "instance.cfg";
	protected static final String INSTANCE_VALIDATION_LAYERS = "VALIDATION_LAYERS";
	protected static final String INSTANCE_EXTENSIONS = "EXTENSIONS";

	public static VkInstance createInstanceFromAsset(VkApplicationInfo appInfo, Asset config) throws FileNotFoundException, VulkanException {
		File cfgFile = config.get(INSTANCE_DATA_FILE);
		
		if (!cfgFile.exists())
			throw new FileNotFoundException("Failed to locate file: " + cfgFile.getPath().toString() + "!");
		
		JSONObject obj = new JSONObject(new JSONTokener(new FileReader(cfgFile)));
		
		JSONArray layers = obj.getJSONArray(INSTANCE_VALIDATION_LAYERS);
		List<String> layerNames = new ArrayList<String>(layers.length());
		for (int i = 0; i < layers.length(); i++)
			layerNames.add(layers.getString(i));
		
		JSONArray extensions = obj.getJSONArray(INSTANCE_EXTENSIONS);
		List<String> extensionNames = new ArrayList<String>(extensions.length());
		for (int i = 0; i < extensions.length(); i++)
			extensionNames.add(extensions.getString(i));
		
		return createInstance(appInfo, layerNames, extensionNames);
	}
	
}
