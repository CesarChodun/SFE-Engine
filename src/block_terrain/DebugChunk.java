package block_terrain;

import static org.lwjgl.system.MemoryUtil.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Vector3f;

import geometry.IndexedMesh;

public class DebugChunk extends ConnectedChunk{
 
	private Vector3f pos;
	
	
	public DebugChunk(Vector3f pos) {
		this.pos = pos;
	}
	
	public IndexedMesh generateMesh() {
		FloatBuffer ver = memAllocFloat(8 * Float.BYTES);
		IntBuffer ind = memAllocInt(6 * 6 * Integer.BYTES);
		
		ver.put(1).put(1).put(1);	//0
		ver.put(1).put(1).put(-1);	//1
		ver.put(1).put(-1).put(-1);	//2
		ver.put(1).put(-1).put(1);	//3
		ver.put(-1).put(1).put(1);	//4
		ver.put(-1).put(1).put(-1);	//5
		ver.put(-1).put(-1).put(-1);//6
		ver.put(-1).put(-1).put(1);	//7
		
		//right
		ind.put(0).put(1).put(2);
		ind.put(0).put(2).put(3);
		
		//left
		ind.put(4).put(5).put(6);
		ind.put(4).put(6).put(7);
		
		//top
		ind.put(0).put(1).put(2);
		ind.put(0).put(2).put(3);
		
		ind.put(0).put(1).put(2);
		ind.put(0).put(2).put(3);
		
		ind.put(0).put(1).put(2);
		ind.put(0).put(2).put(3);
		
		ind.put(0).put(1).put(2);
		ind.put(0).put(2).put(3);
		
		
		return new IndexedMesh(ver, ind);
	}


	public Vector3f getPos() {
		return pos;
	}
}
