package core.result;

/**
 * 
 * @author Cezary Chodun
 *
 *	Class for managing Vulkan results.
 *
 */
public class VulkanResult {

	/**
	 * Checks whether the Vulkan result ID number
	 * represents error, exception, or success message.
	 * And based on that either throws an error,
	 * an exception, or does nothing.
	 * 
	 * @param vkResult			The Vulkan result.
	 * @param message			Message in case of error/exception.
	 * @throws VulkanException	In case that vkResult represents 
	 * 							an exception message.
	 */
	public static void validate(int vkResult, String message) throws VulkanException {
		if(vkResult == 0)
			return;
		
		if(vkResult > 0)
			throw new VulkanException(vkResult, message);
		
		throw new VulkanError(vkResult, message);
	}
	
}
