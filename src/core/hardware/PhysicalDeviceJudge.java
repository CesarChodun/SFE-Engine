package core.hardware;

import org.lwjgl.vulkan.VkPhysicalDevice;

/**
 * Class for determining a physical device score based on its properties.
 *
 * @author Cezary Chodun
 * @since 24.09.2019
 */
public interface PhysicalDeviceJudge {

    /**
     * Calculates a physical device score base on it's properties. The device with higher score is
     * more suitable.
     *
     * @param device Physical device to be evaluated.
     * @return Score of the device.
     */
    public int score(VkPhysicalDevice device);
}
