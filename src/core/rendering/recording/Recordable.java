package core.rendering.recording;

import org.lwjgl.vulkan.VkCommandBuffer;

public interface Recordable {

	/**
	 * Records a set of instructions to the given
	 * command buffer.
	 * 
	 * @param buffer
	 */
	public void record(VkCommandBuffer buffer);
	
}
