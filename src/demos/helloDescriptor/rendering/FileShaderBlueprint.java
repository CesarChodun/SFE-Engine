package demos.helloDescriptor.rendering;

import static rendering.pipeline.PipelineUtil.*;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDevice;

import core.Util;
import core.resources.ConfigAsset;
import rendering.pipeline.ShaderBlueprint;

public class FileShaderBlueprint implements ShaderBlueprint {

	VkDescriptorSetLayoutBinding.Buffer layoutBindings;
	
	
	public FileShaderBlueprint(ConfigAsset cfg) {
		loadFromAsset(cfg);
	}
	
	private void loadFromAsset(ConfigAsset cfg) {
		
	}
	
	@Override
	public LongBuffer createLayoutsBuffer(VkDevice device) {
		return createDescriptorSetLayout(device, layoutBindings);
	}

}
