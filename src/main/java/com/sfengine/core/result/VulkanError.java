package com.sfengine.core.result;

/**
 * Class representing an error caused by the vulkan loader.
 *
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public class VulkanError extends Error {

    /** Auto generated serial number. */
    private static final long serialVersionUID = 8514946153604944692L;

    /** Vulkan error number. */
    private int number;
    /** Vulkan error type. */
    private VulkanErrors error;
    /** Error message to be displayed. */
    private String message;

    /**
     * Creates a new VulkanError with the given Vulkan error number.
     *
     * @param errorNumber Vulkan identification number.
     */
    public VulkanError(int errorNumber) {
        this.number = errorNumber;
        this.error = VulkanErrors.getVulkanError(errorNumber);
    }

    /**
     * Creates a new VulkanError with the given Vulkan error number and message.
     *
     * @param errorNumber Vulkan identification number.
     * @param message Error message.
     */
    public VulkanError(int errorNumber, String message) {
        this(errorNumber);
        this.message = message;
    }

    /**
     * Creates a new VulkanError with the given Vulkan error.
     *
     * @param error The Vulkan error.
     */
    public VulkanError(VulkanErrors error) {
        this.number = error.getErrorNumber();
        this.error = error;
    }

    /**
     * Creates a new VulkanError with the given Vulkan error and given message.
     *
     * @param error The Vulkan error.
     * @param message Error message.
     */
    public VulkanError(VulkanErrors error, String message) {
        this(error);
        this.message = message;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append("Vulkan ");
        sb.append(error.getName());
        sb.append(" Error(");

        if (number < 0) {
            sb.append(number);
        } else {
            sb.append("?");
        }

        sb.append(")");

        if (message != null) {
            sb.append(": " + message);
        }

        return sb.toString();
    }
}
