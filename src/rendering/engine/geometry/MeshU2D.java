package rendering.engine.geometry;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

import org.joml.Vector2f;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;

import core.result.VulkanException;

/**
 * Unindexed 2D mesh.
 * 
 * @author Cezary Chodun
 *
 */
public class MeshU2D implements Mesh {
	
	/** Length of a vertex description(in bytes). */
	private static int STRIDE = 2 * 4; // Two floats(4 bytes) per vertex.
	
	private VkDevice device;

	/** Vertices buffer handle. */
	private long verticesBuffer;
	/** Number of vertices in the buffer. */
	private int verticesCount;
	
	/**
	 * Creates a new un-indexed two-dimensional(only x and y coordinates) mesh.
	 * 
	 * @param physicalDevice	A physical device that will be using the mesh.
	 * @param device			A device that will be using the mesh.
	 * @param vertices			A list containing vertices position(x, y).
	 * 
	 * @throws VulkanException
	 */
	public MeshU2D(VkPhysicalDevice physicalDevice, VkDevice device, List<Vector2f> vertices) throws VulkanException {
		this.device = device;
		
		createMesh(physicalDevice, vertices);
	}
	
	/**
	 * Converts vertices data to a Vulkan buffer.
	 * 
	 * @param physicalDevice	A physical device that will be using the vertices data.
	 * @param vertices			A list containing vertices position(x, y).
	 * 
	 * @throws VulkanException
	 */
	private void createMesh(VkPhysicalDevice physicalDevice, List<Vector2f> vertices) throws VulkanException {
		
		verticesCount = vertices.size();
		ByteBuffer vertexBuffer = memAlloc(verticesCount * 2 * 4);		

		//Fills the byte buffer with data.
		FloatBuffer fb = vertexBuffer.asFloatBuffer();
		for (int i = 0; i < verticesCount; i++)
			fb.put(vertices.get(i).x).put(vertices.get(i).y);
		fb.flip();
		
		//Creates a Vulkan buffer with vertices data.
		verticesBuffer = Util.createVerticesBuffer(physicalDevice, device, vertexBuffer);
		
		memFree(vertexBuffer);
	}

	@Override
	public void destroy() {
		vkDestroyBuffer(device, verticesBuffer, null);
	}

	@Override
	public long getVerticesHandle() {
		return verticesBuffer;
	}

	@Override
	public int getStride() {
		return STRIDE;
	}

	@Override
	public int verticesCount() {
		return verticesCount;
	}	
	
}
