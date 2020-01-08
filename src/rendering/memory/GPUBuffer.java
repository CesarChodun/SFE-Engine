package rendering.memory;

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
