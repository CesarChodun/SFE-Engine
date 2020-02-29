package core.result;

import java.util.HashMap;

/**
 *    Enum for representing errors that
 *    can be thrown by Vulkan loader.
 * 
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public enum VulkanErrors {

    UNKNOWN_ERROR(1, "UNKNOWN_ERROR"),
    VK_ERROR_OUT_OF_HOST_MEMORY(-1, "VK_ERROR_OUT_OF_HOST_MEMORY"),
    VK_ERROR_OUT_OF_DEVICE_MEMORY(-2, "VK_ERROR_OUT_OF_DEVICE_MEMORY"),
    VK_ERROR_INITIALIZATION_FAILED(-3, "VK_ERROR_INITIALIZATION_FAILED"),
    VK_ERROR_DEVICE_LOST(-4, "VK_ERROR_DEVICE_LOST"),
    VK_ERROR_MEMORY_MAP_FAILED(-5, "VK_ERROR_MEMORY_MAP_FAILED"),
    VK_ERROR_LAYER_NOT_PRESENT(-6, "VK_ERROR_LAYER_NOT_PRESENT"),
    VK_ERROR_EXTENSION_NOT_PRESENT(-7, "VK_ERROR_EXTENSION_NOT_PRESENT"),
    VK_ERROR_FEATURE_NOT_PRESENT(-8, "VK_ERROR_FEATURE_NOT_PRESENT"),
    VK_ERROR_INCOMPATIBLE_DRIVER(-9, "VK_ERROR_INCOMPATIBLE_DRIVER"),
    VK_ERROR_TOO_MANY_OBJECTS(-10, "VK_ERROR_TOO_MANY_OBJECTS"),
    VK_ERROR_FORMAT_NOT_SUPPORTED(-11, "VK_ERROR_FORMAT_NOT_SUPPORTED"),
    VK_ERROR_FRAGMENTED_POOL(-12, "VK_ERROR_FRAGMENTED_POOL"),
    VK_ERROR_OUT_OF_POOL_MEMORY(-1000069000, "VK_ERROR_OUT_OF_POOL_MEMORY"),
    VK_ERROR_INVALID_EXTERNAL_HANDLE(-1000072003, "VK_ERROR_INVALID_EXTERNAL_HANDLE"),
    VK_ERROR_SURFACE_LOST_KHR(-1000000000, "VK_ERROR_SURFACE_LOST_KHR"),
    VK_ERROR_NATIVE_WINDOW_IN_USE_KHR(-1000000001, "VK_ERROR_NATIVE_WINDOW_IN_USE_KHR"),
    VK_ERROR_OUT_OF_DATE_KHR(-1000001004, "VK_ERROR_OUT_OF_DATE_KHR"),
    VK_ERROR_INCOMPATIBLE_DISPLAY_KHR(-1000003001, "VK_ERROR_INCOMPATIBLE_DISPLAY_KHR"),
    VK_ERROR_VALIDATION_FAILED_EXT(-1000011001, "VK_ERROR_VALIDATION_FAILED_EXT"),
    VK_ERROR_INVALID_SHADER_NV(-1000012000, "VK_ERROR_INVALID_SHADER_NV"),
    VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT(-1000158000, "VK_ERROR_INVALID_DRM_FORMAT_MODIFIER_PLANE_LAYOUT_EXT"),
    VK_ERROR_FRAGMENTATION_EXT(-1000161000, "VK_ERROR_FRAGMENTATION_EXT"),
    VK_ERROR_NOT_PERMITTED_EXT(-1000174001, "VK_ERROR_NOT_PERMITTED_EXT"),
    VK_ERROR_INVALID_DEVICE_ADDRESS_EXT(-1000244000, "VK_ERROR_INVALID_DEVICE_ADDRESS_EXT"),
    VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT(-1000255000, "VK_ERROR_FULL_SCREEN_EXCLUSIVE_MODE_LOST_EXT");
    
    /**
     * A map of Vulkan errors with the corresponding error id numbers.
     */
    private static final HashMap<Integer, VulkanErrors> errors = new HashMap<Integer, VulkanErrors>();
    
    /**
     * Vulkan error id number.
     */
    private int errorNumber;
    /**
     * Name of the vulkan error.
     */
    private String name;
    
    /**
     * Creates a new VulkanErrors enum with
     * given error number and name.
     * 
     * @param errorNumber    ID number of the error.
     * @param name            Name of the error.
     */
    VulkanErrors(int errorNumber, String name) {
        this.errorNumber = errorNumber;
        this.name = name;
    }
    
    /**
     * Obtains Vulkan error corresponding to
     * vulkan error ID.
     * 
     * @param num            Vulkan error ID.
     * @return                Returns th Vulkan error.
     */
    public static VulkanErrors getVulkanError(int num) {
        if(errors.size() == 0)
            populateErrorMap();
        
        VulkanErrors error = errors.get(num);
        if(error == null)
            return UNKNOWN_ERROR;
        
        return error;
    }
    
    /**
     * Fills the map with errors from this enum.
     */
    private static void populateErrorMap() {
        VulkanErrors[] errorList = VulkanErrors.values();
        
        for(VulkanErrors e : errorList)
            errors.put(e.getErrorNumber(), e);
    }

    /**
     * Returns the Vulkan error ID.
     * 
     * @return                Error ID.
     */
    public int getErrorNumber() {
        return errorNumber;
    }

    /**
     * Returns the Vulkan error name
     * @return                Error name.
     */
    public String getName() {
        return name;
    }
}
