package core.result;

/**
 * Exception thrown in case of GLFW malfunction.
 *
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public class GLFWException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Exception message */
    private String message;

    /** Creates an exception with an empty message. */
    public GLFWException() {}

    /**
     * Creates an exception with the given message.
     *
     * @param message Message for the exception.
     */
    public GLFWException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        if (message == null) {
            return "";
        }

        return message;
    }
}
