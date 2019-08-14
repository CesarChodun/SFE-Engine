package core.hardware;

import org.lwjgl.vulkan.VkPhysicalDevice;

public interface PhysicalDeviceJudge {

	public int score(VkPhysicalDevice device);
	
}
