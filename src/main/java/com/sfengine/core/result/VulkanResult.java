package com.sfengine.core.result;

/**
 * Class for managing Vulkan results.
 *
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public class VulkanResult {

    /**
     * Checks whether the Vulkan result ID number represents error, exception, or success message.
     * And based on that either throws an error, an exception, or does nothing.
     *
     * @param vkResult The Vulkan result.
     * @param message Message in case of an error/exception.
     * @throws VulkanException In case that vkResult represents an exception message.
     */
    public static void validate(int vkResult, String message) throws VulkanException {
        if (vkResult == 0) {
            return;
        }

        if (vkResult > 0) {
            throw new VulkanException(vkResult, message);
        }

        throw new VulkanError(vkResult, message);
    }

    /**.
     * Throws vulkan error if the vkResult ID is not VK_SUCCESS.
     *
     * @param vkResult The Vulkan result.
     * @param message Message in case of an error.
     */
    public static void assertValidate(int vkResult, String message) {
        if (vkResult == 0) {
            return;
        }

        throw new VulkanError(vkResult, message);
    }
}
