package components.geometry;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;

import core.result.VulkanException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import org.joml.Vector3f;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;

/**
 * Indexed 3D mesh.
 * 
 * @author Cezary Chodun
 * @since 6.03.2020
 */
public class MeshI3D implements IndexedMesh {
    
    /** Length of a vertex description(in bytes). */
    private static int STRIDE = 3 * 4; // Three(xyz) floats(4 bytes) per vertex.

    /** Length of a index description(in bytes). */
    private static int INDICES_STRIDE = 1 * 4;
    
    private VkDevice device;

    /** Vertices buffer handle. */
    private long verticesBuffer;
    /** Number of vertices in the buffer. */
    private int verticesCount;
    
    /** Indices buffer handle. */
    private long indicesBuffer;
    /** Number of indices in the buffer. */
    private int indicesCount;
    
    /**
     * Creates a new un-indexed two-dimensional(only x and y coordinates) mesh.
     *
     * @param physicalDevice A physical device that will be using the mesh.
     * @param device A device that will be using the mesh.
     * @param vertices A list containing vertices position(x, y).
     * @throws VulkanException
     */
    public MeshI3D(VkPhysicalDevice physicalDevice,
                    VkDevice device,
                    List<Vector3f> vertices,
                    List<Integer> indices) throws VulkanException {
        
        this.device = device;
        createMesh(physicalDevice, vertices, indices);
    }
    
    private void createVerticesBuffer(
            VkPhysicalDevice physicalDevice, 
            List<Vector3f> vertices) throws VulkanException {
        
        verticesCount = vertices.size();
        ByteBuffer vertexBuffer = memAlloc(verticesCount * STRIDE);

        // Fills the byte buffer with data.
        FloatBuffer fb = vertexBuffer.asFloatBuffer();
        for (int i = 0; i < verticesCount; i++) {
            fb.put(vertices.get(i).x).put(vertices.get(i).y).put(vertices.get(i).z);
        }
        fb.flip();

        // Creates a Vulkan buffer with vertices data.
        verticesBuffer = Util.createVerticesBuffer(physicalDevice, device, vertexBuffer);
        memFree(vertexBuffer);
    }
    
    private void createIndicesBuffer(
            VkPhysicalDevice physicalDevice, 
            List<Integer> indices) throws VulkanException {

        indicesCount = indices.size();
        ByteBuffer indexBuffer = memAlloc(indicesCount * INDICES_STRIDE);
        
        IntBuffer ib = indexBuffer.asIntBuffer();
        for (int i = 0; i < indicesCount; i++) {
            ib.put(indices.get(i));
        }
        ib.flip();
        
        indicesBuffer = Util.createIndicesBuffer(physicalDevice, device, indexBuffer);
        memFree(indexBuffer);
    }
    
    private void createMesh(
            VkPhysicalDevice physicalDevice, 
            List<Vector3f> vertices,
            List<Integer> indices) throws VulkanException {
        
        createVerticesBuffer(physicalDevice, vertices);
        createIndicesBuffer(physicalDevice, indices);
    }

    @Override
    public void destroy() {
        vkDestroyBuffer(device, verticesBuffer, null);
        vkDestroyBuffer(device, indicesBuffer, null);
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

    @Override
    public long getIndicesHandle() {
        return indicesBuffer;
    }

    @Override
    public int getIndicesStride() {
        return INDICES_STRIDE;
    }

    @Override
    public int indicesCount() {
        return indicesCount;
    }

}
