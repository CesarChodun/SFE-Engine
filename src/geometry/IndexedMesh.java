package geometry;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class IndexedMesh extends Mesh{

	private IntBuffer indices;
	
	public IndexedMesh(FloatBuffer ver, IntBuffer ind) {
		super(ver);
		this.indices = ind;
	}

	public IntBuffer getIndices() {
		return indices;
	}
}
