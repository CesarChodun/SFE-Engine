package core.result;

/**
 *    Class representing an exception caused
 *    by the vulkan loader.
 * 
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public class VulkanException extends Exception{
    
    /**
     * Auto generated serial number. 
     */
    private static final long serialVersionUID = -2371351734264366098L;
    
    /**
     * Vulkan exception ID number.
     */
    protected int number;
    /**
     * Vulkan exception type.
     */
    protected VulkanExceptions exception;
    /**
     * Exception message.
     */
    protected String message;
    
    /**
     * Creates a new VulkanException with
     * the exception type corresponding to
     * the given exception number. 
     * 
     * @param exceptionNumber    Vulkan exception number.
     */
    public VulkanException(int exceptionNumber) {
        this.number = exceptionNumber;
        this.exception = VulkanExceptions.getVulkanException(exceptionNumber);
    }
    
    /**
     * Creates a new VulkanException with
     * the exception type corresponding to
     * the given exception number and with 
     * the given message. 
     * 
     * @param exceptionNumber    Vulkan exception number.
     * @param message            Exception message.
     */
    public VulkanException(int exceptionNumber, String message) {
        this(exceptionNumber);
        this.message = message;
    }
    
    /**
     * Creates a new VulkanException with
     * the given exception type.
     * 
     * @param exception            Vulkan exception type.
     */
    public VulkanException(VulkanExceptions exception) {
        this.number = exception.getExceptionNumber();
        this.exception = exception;
    }
    
    /**
     * Creates a new VulkanException with
     * the given exception type and message.
     * 
     * @param exception            Vulkan exception type.
     * @param message            Exception message.
     */
    public VulkanException(VulkanExceptions exception, String message) {
        this(exception);
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Vulkan ");
        sb.append(exception.getName());
        sb.append(" Exception(");
        
        if (number >= 0)
            sb.append(number);
        else
            sb.append("?");
        
        sb.append(")");
        
        if(message != null)
            sb.append(": " + message);
        
        return sb.toString();
    }
    
}
