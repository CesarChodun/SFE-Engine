package core.result;

import java.util.HashMap;

/**
 *    Enum for representing exceptions
 *    that can be thrown by Vulkan loader.
 * 
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public enum VulkanExceptions {

    UNKNOWN_EXCEPTION(-1, "UNKNOWN_EXCEPTION"),
    VK_SUCCESS(0, "VK_SUCCESS"),
    VK_NOT_READY(1, "VK_NOT_READY"),
    VK_TIMEOUT(2, "VK_TIMEOUT"),
    VK_EVENT_SET(3, "VK_EVENT_SET"),
    VK_EVENT_RESET(4, "VK_EVENT_RESET"),
    VK_INCOMPLETE(5, "VK_INCOMPLETE"),
    VK_SUBOPTIMAL_KHR(1000001003, "VK_SUBOPTIMAL_KHR");
    
    /**
     * A map of the Vulkan exceptions and corresponding ID numbers.
     */
    private static final HashMap<Integer, VulkanExceptions> exceptions = new HashMap<Integer, VulkanExceptions>();
    
    /**
     * The Vulkan exception ID number.
     */
    protected int exceptionNumber;
    /**
     * The name of the Vulkan exception.
     */
    protected String name;
    
    /**
     * Creates a new enum VulkanExceptions
     * with the given exception number, and name.
     * 
     * @param exceptionNumber    The exception ID number.
     * @param name                The exception name.
     */
    VulkanExceptions(int exceptionNumber, String name){
        this.exceptionNumber = exceptionNumber;
        this.name = name;
    }
    
    /**
     * Returns a Vulkan exception type corresponding
     * to the given exception ID number.
     * 
     * @param num                Exception ID number.
     * @return                    The Vulkan exception type.
     */
    public static VulkanExceptions getVulkanException(int num) {
        if(exceptions.size() == 0)
            populateExceptionMap();
        
        VulkanExceptions exception = exceptions.get(num);
        if(exception == null)
            return UNKNOWN_EXCEPTION;
        
        return exception;
    }
    
    /**
     * Populates the exception map.
     */
    private static void populateExceptionMap() {
        VulkanExceptions[] errorList = VulkanExceptions.values();
        
        for(VulkanExceptions e : errorList)
            exceptions.put(e.getExceptionNumber(), e);
    }

    /**
     * Returns the Exception ID number.
     * 
     * @return                    Exception ID number.
     */
    public int getExceptionNumber() {
        return exceptionNumber;
    }

    /**
     * Returns the Exception name.
     * 
     * @return                    The Exception name.
     */
    public String getName() {
        return name;
    }
}
