package debug;

import static org.lwjgl.system.MemoryUtil.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkLayerProperties;

public class ValidationLayers {

	public static void main(String[] args) {
		
//		Paths.get("/Users/cesarchodun/Projects/Vulkan/Vulkan-ValidationLayers-sdk-1.1.108.0/build/Vulkan-Loader/build/install/lib/libvulkan.1.1.108.dylib").isAbsolute())
		
		IntBuffer propsCount = memAllocInt(1);
		VK10.nvkEnumerateInstanceLayerProperties(memAddress(propsCount), NULL);
		int count = propsCount.get(0);
		System.err.println("Layer count: " + count);
		
		VkLayerProperties.Buffer props = VkLayerProperties.calloc(count);
		VK10.nvkEnumerateInstanceLayerProperties(memAddress(propsCount), memAddress(props));
		
		for (int i = 0; i < count; i++) {
			System.err.print("Layer: \"" + props.get(i).descriptionString());
			System.err.println("\" with name: " + props.get(i).layerNameString());
			
		}
	}
	
}
//‎⁨Untitled/⁨usr⁩/local/⁨share⁩/⁨vulkan⁩