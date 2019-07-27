package core.rendering.factories;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;

public interface CommandBufferFactory {

	public VkCommandBuffer[] createCmdBuffers(long[] swapchainImages);
	
	public void destroyCmdBuffers(VkCommandBuffer[] buffers);
}
