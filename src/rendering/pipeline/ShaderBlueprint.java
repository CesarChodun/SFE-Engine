package rendering.pipeline;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDevice;

public interface ShaderBlueprint {

	public LongBuffer createLayoutsBuffer(VkDevice device);
	
}
