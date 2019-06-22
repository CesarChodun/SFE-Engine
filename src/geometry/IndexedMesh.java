package geometry;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Class for storing information about 3D indexed mesh.
 * 
 * @author Cezary Chodun
 *
 */
public class IndexedMesh extends Mesh{

	/**
	 * List of indices.
	 */
	private IntBuffer indices;
	
	/**
	 * Creates a new IdexedMesh with given buffers.
	 * 
	 * @param ver	vertices buffer
	 * @param ind	indices buffer
	 */
	public IndexedMesh(FloatBuffer ver, IntBuffer ind) {
		super(ver);
		this.indices = ind;
	}

	/**
	 * 
	 * @return		indices buffer
	 */
	public IntBuffer getIndices() {
		return indices;
	}
}
