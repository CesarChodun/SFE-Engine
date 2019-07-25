package core.result;

public class GLFWException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public GLFWException() {}
	
	public GLFWException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		if(message == null)
			return "";
		
		return message;
	}

}
