package components.memory;

/**
 * GPU memory buffer data.
 *
 * @author Cezary
 * @since 10.01.2020
 */
public class GPUBuffer {
    /** Vulkan memory buffer. */
    public long buffer;
    /** Allocated memory address. */
    public long memory;
    /** Size of the buffer. */
    public long allocationSize;

    public GPUBuffer() {}

    public GPUBuffer(long buffer, long memory, long allocationSize) {
        this.buffer = buffer;
        this.memory = memory;
        this.allocationSize = allocationSize;
    }
}
