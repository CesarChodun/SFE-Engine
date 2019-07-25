package core.result;

public class GLFWError extends Error {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4381649662106122057L;
	private String message;
	
	public GLFWError() {}
	
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