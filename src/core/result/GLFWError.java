package core.result;

/**
 * Class for handling GLFW errors.
 * 
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public class GLFWError extends Error {
	
	
	private static final long serialVersionUID = 4381649662106122057L;
	/** Error message. */
	private String message;
	
	/**
	 * Creates an Vulkan error without a message.
	 */
	public GLFWError() {}
	
	/**
	 * Creates an Vulkan error with the given message.
	 * 
	 * @param message	Error message.
	 */
	public GLFWError(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		if(message == null)
			return "";
		
		return message;
	}

}