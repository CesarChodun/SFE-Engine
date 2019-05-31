package block_terrain;

import java.nio.ShortBuffer;

import static org.lwjgl.system.MemoryUtil.*;

public class Chunk {

	protected int size = 16;
	
	private ShortBuffer depth = null;
	private ShortBuffer type = null;
	
	public Chunk() {}
	
	public Chunk(int size) {
		this.size = size;
	}
	
	public void alloc() {
		depth = memAllocShort(size * size * size * Short.BYTES);
		type = memAllocShort(size * size * size * Short.BYTES);
	}
	
	public void setDepth(int id, short v) {
		depth.put(id, v);
	}
	
	public void setType(int id, short v) {
		type.put(id, v);
	}
	
	public short getDepth(int id) {
		return depth.get(id);
	}
	
	public short getType(int id) {
		return type.get(id);
	}
	
	public void destroy() {
		memFree(depth);
		memFree(type);
		
		depth = null;
		type = null;
	}
	
	public int getSize() {
		return size;
	}
}
