package geometry;

import static org.lwjgl.system.MemoryUtil.*;

import java.nio.FloatBuffer;
import java.util.List;

import org.joml.Vector3f;

/**
 * Class for storing information about 3D mesh.
 * 
 * @author Cezary Chodun
 *
 */
public class Mesh {

	/**
	 * Vertices buffer(for 3D mesh).
	 */
	protected FloatBuffer vertices;
	
	/**
	 * Creates a new mesh with a given vertices buffer.
	 * @param ver		vertices buffer
	 */
	public Mesh(FloatBuffer ver) {
		this.vertices = ver;
	}
	
	/**
	 * Creates a new mesh with the given vertices.
	 * @param vec		list of vertices
	 */
	public Mesh(List<Vector3f> vec) {
		vertices = memAllocFloat(vec.size() * 3 * Float.BYTES);
		
		for(int i = 0; i < vec.size(); i++) 
			vertices.put(vec.get(i).x).put(vec.get(i).y).put(vec.get(i).z);
		
		vertices.flip();
	}

	/**
	 * 
	 * @return		the vertices buffer
	 */
	public FloatBuffer getVertices() {
		return vertices;
	}
}
