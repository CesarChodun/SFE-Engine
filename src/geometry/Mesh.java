package geometry;

import static org.lwjgl.system.MemoryUtil.*;

import java.nio.FloatBuffer;
import java.util.List;

import org.joml.Vector3f;

public class Mesh {

	
	private FloatBuffer vertices;
	
	public Mesh(FloatBuffer ver) {
		this.vertices = ver;
	}
	
	public Mesh(List<Vector3f> vec) {
		vertices = memAllocFloat(vec.size() * 3 * Float.BYTES);
		
		for(int i = 0; i < vec.size(); i++) 
			vertices.put(vec.get(i).x).put(vec.get(i).y).put(vec.get(i).z);
		
		vertices.flip();
	}

	public FloatBuffer getVertices() {
		return vertices;
	}
}
