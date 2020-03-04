package rendering.memory;

/**
 * GPU memory buffer data.
 *
 * @author Cezary
 * @since 10.01.2020
 */
public class GPUBuffer {
    public long buffer;
    public long memory;
    public long allocationSize;

    public GPUBuffer() {}

    public GPUBuffer(long buffer, long memory, long allocationSize) {
        this.buffer = buffer;
        this.memory = memory;
        this.allocationSize = allocationSize;
    }
}
