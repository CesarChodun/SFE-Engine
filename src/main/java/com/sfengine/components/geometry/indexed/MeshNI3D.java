package com.sfengine.components.geometry.indexed;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import com.sfengine.components.geometry.Util;
import com.sfengine.core.result.VulkanException;
import org.joml.Vector3f;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;

public class MeshNI3D implements IndexedMesh{
    
    /** Length of a vertex description(in bytes). */
    private static int STRIDE = 3 * 4 * 2; // Three(xyz) floats(4 bytes) per vertex. x2

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
     * Creates a new un-indexed three-dimensional(only x and y coordinates) mesh.
     *
     * @param physicalDevice A physical device that will be using the mesh.
     * @param device A device that will be using the mesh.
     */
    public MeshNI3D(VkPhysicalDevice physicalDevice,
                    VkDevice device,
                    List<Vector3f> positions,
                    List<Vector3f> normals,
                    List<Integer> indices) throws VulkanException {
        
        this.device = device;
        createMesh(physicalDevice, positions, normals, indices);
    }
    
    private void createVerticesBuffer(
            VkPhysicalDevice physicalDevice,
            List<Vector3f> positions,
            List<Vector3f> normals) throws VulkanException {
        
        verticesCount = positions.size();
        ByteBuffer vertexBuffer = memAlloc(verticesCount * STRIDE);

        // Fills the byte buffer with data.
        FloatBuffer fb = vertexBuffer.asFloatBuffer();
        for (int i = 0; i < verticesCount; i++) {
            fb.put(positions.get(i).x).put(positions.get(i).y).put(positions.get(i).z);
            fb.put(normals.get(i).x).put(normals.get(i).y).put(normals.get(i).z);
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
            List<Vector3f> positions,
            List<Vector3f> normals,
            List<Integer> indices) throws VulkanException {
        
        createVerticesBuffer(physicalDevice, positions, normals);
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
